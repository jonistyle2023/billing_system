package upse.calculacion.Mad;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import upse.calculacion.general.Mod_DB;
import upse.calculacion.general.Mod_VariablesGlobales;
import upse.calculacion.general.Mod_log;
import upse.calculacion.modelo.Cliente;
import upse.calculacion.modelo.ConfiguracionSri;
import upse.calculacion.modelo.DetFactura;
import upse.calculacion.modelo.Empresa;
import upse.calculacion.modelo.ResultadoEmisionFactura;
import upse.calculacion.sri.ClaveAccesoGenerador;
import upse.calculacion.sri.FacturaXmlBuilder;
import upse.calculacion.sri.FirmaXadesBes;
import upse.calculacion.sri.ResultadoAutorizacion;
import upse.calculacion.sri.ResultadoRecepcion;
import upse.calculacion.sri.SriSoapClient;

public class Mad_factura {

    public static final String ESTADO_SRI_PENDIENTE  = "PENDIENTE";
    public static final String ESTADO_SRI_AUTORIZADO = "AUTORIZADO";
    public static final String ESTADO_SRI_RECHAZADO  = "RECHAZADO";

    private static final int INTENTOS_AUTORIZACION = 5;
    private static final long ESPERA_ENTRE_INTENTOS_MS = 2000;

    private final Mod_DB bd;

    public Mad_factura() {
        this(new Mod_DB());
    }

    public Mad_factura(Mod_DB bd) {
        this.bd = bd;
    }

    /**
     * Emite la factura en una sola transacción:
     * 1. Busca cli_id del cliente por cédula.
     * 2. Genera el siguiente número secuencial (fac_numero).
     * 3. Inserta en dbo.Factura (estado 'A') y obtiene el fac_id generado.
     * 4. Por cada línea: inserta en dbo.DetalleFactura y descuenta prod_stock.
     * Si cualquier paso falla → rollback completo.
     *
     * <p>Una vez confirmada la venta (fuera de la transacción anterior), se firma y envía
     * el comprobante al SRI de forma síncrona con contingencia: si el SRI no autoriza en
     * unos segundos, la venta queda registrada igual y el comprobante en estado
     * {@link #ESTADO_SRI_PENDIENTE} para reintentar después (ver {@link #reintentarAutorizacionSri}).
     */
    public ResultadoEmisionFactura emitirFactura(String cliCedula, LocalDate fecha,
                                float subtotal, float baseCero, float iva, float total,
                                int usrId, List<DetFactura> detalles,
                                String pagoMetodo, float pagoMonto, float pagoCambio) throws SQLException {
        ConfiguracionSri configuracion = new Mad_configuracionSri().obtener();

        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        String numFac;
        try {
            bd.iniciarTransaccion();

            // 1. Buscar cli_id (la cedula es el propio cli_id)
            String cliId = null;
            String sqlCli = "SELECT cli_id FROM dbo.Cliente WHERE cli_id = ? "
                    + "AND (cli_estado = 'A' OR cli_estado IS NULL)";
            try (PreparedStatement ps = bd.getConexion().prepareStatement(sqlCli)) {
                ps.setString(1, cliCedula);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) cliId = rs.getString("cli_id");
                }
            }

            // 2. Generar número
            numFac = generarNumero(configuracion);

            // 3. Insertar cabecera en Factura
            String sqlFac = "INSERT INTO dbo.Factura "
                    + "(fac_numero, fac_fecha, cli_id, fac_subtotal, fac_subtotalcero, "
                    + " fac_iva, fac_descuento, fac_total, aud_usr_id_crea, fac_estado, "
                    + " fac_pago_metodo, fac_pago_monto, fac_pago_cambio, fe_estado) "
                    + "VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?, 'A', ?, ?, ?, ?)";
            int facId;
            try (PreparedStatement ps = bd.getConexion().prepareStatement(sqlFac, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, numFac);
                ps.setDate(2, java.sql.Date.valueOf(fecha));
                if (cliId != null) ps.setString(3, cliId);
                else                ps.setNull(3, java.sql.Types.VARCHAR);
                ps.setFloat(4, subtotal);
                ps.setFloat(5, baseCero);
                ps.setFloat(6, iva);
                ps.setFloat(7, total);
                ps.setInt(8, usrId);
                ps.setString(9, pagoMetodo);
                ps.setFloat(10, pagoMonto);
                ps.setFloat(11, pagoCambio);
                ps.setString(12, ESTADO_SRI_PENDIENTE);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("No se pudo obtener el ID de la factura generada.");
                    }
                    facId = rs.getInt(1);
                }
            }

            // 4. Insertar líneas de detalle y descontar stock
            String sqlDet   = "INSERT INTO dbo.DetalleFactura "
                    + "(fac_id, prod_id, prod_nombre, cantidad, prod_pvp, iva, total) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            // "AND prod_stock >= ?" hace el chequeo atómico con el descuento: si dos cajas
            // facturan el mismo producto a la vez, solo una gana la fila y la otra ve 0 filas
            // afectadas (en vez de que ambas descuenten y el stock quede negativo).
            String sqlStock = "UPDATE dbo.Producto SET prod_stock = prod_stock - ? WHERE prod_id = ? AND prod_stock >= ?";

            for (DetFactura det : detalles) {
                if (det.getProd_cod() == null || det.getProd_cod().isEmpty()) continue;

                try (PreparedStatement ps = bd.getConexion().prepareStatement(sqlDet)) {
                    ps.setInt(1, facId);
                    ps.setInt(2, det.getProd_id());
                    ps.setString(3, det.getProd_nombre());
                    ps.setFloat(4, det.getCantidad());
                    ps.setFloat(5, det.getPrecio());
                    ps.setFloat(6, det.isAplicaIva() ? det.getTotal() * Mod_VariablesGlobales.getTasaIva() : 0f);
                    ps.setFloat(7, det.getTotal());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = bd.getConexion().prepareStatement(sqlStock)) {
                    ps.setFloat(1, det.getCantidad());
                    ps.setInt(2, det.getProd_id());
                    ps.setFloat(3, det.getCantidad());
                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("Stock insuficiente para \"" + det.getProd_nombre()
                                + "\" (código " + det.getProd_cod() + "); la venta fue cancelada.");
                    }
                }
            }

            bd.commit();

        } catch (SQLException e) {
            bd.rollback();
            throw e;
        } finally {
            bd.desconectarBD();
        }

        // La venta ya quedó registrada; el SRI se procesa aparte y nunca revierte la venta.
        ResultadoEmisionFactura resultado = procesarSri(numFac, cliCedula, fecha, subtotal, baseCero, iva, total,
                detalles, pagoMetodo, pagoMonto, configuracion);
        return resultado;
    }

    private ResultadoEmisionFactura procesarSri(String numFac, String cliCedula, LocalDate fecha,
                                                 float subtotal, float baseCero, float iva, float total,
                                                 List<DetFactura> detalles, String pagoMetodo, float pagoMonto,
                                                 ConfiguracionSri configuracion) {
        try {
            Empresa empresa = new Mad_empresa().obtenerEmpresaActiva();
            if (empresa == null || empresa.getRuc() == null || empresa.getRuc().isBlank()) {
                return marcarPendiente(numFac, null, null,
                        "No se puede emitir electrónicamente: falta configurar el RUC de la empresa (dbo.Empresa).");
            }

            Cliente cliente = new Mad_cliente().buscarPorCedula(cliCedula);

            String secuencial = numFac.substring(numFac.lastIndexOf('-') + 1);
            String claveAcceso = ClaveAccesoGenerador.generar(fecha, empresa.getRuc(), configuracion.getAmbiente(),
                    configuracion.getEstablecimiento(), configuracion.getPuntoEmision(), secuencial,
                    configuracion.getTipoEmision());

            String xml = FacturaXmlBuilder.construir(claveAcceso, secuencial, empresa, configuracion, fecha, cliente,
                    subtotal, baseCero, iva, total, Mod_VariablesGlobales.getTasaIva() * 100f, detalles,
                    pagoMetodo, pagoMonto);

            String rutaCertificado = upse.calculacion.general.Mod_configuracion.get("sri.rutaCertificado", "");
            String claveCertificado = upse.calculacion.general.Mod_configuracion.get("sri.claveCertificado", "");
            if (rutaCertificado.isBlank()) {
                return marcarPendiente(numFac, claveAcceso, null,
                        "No se puede firmar: falta configurar la ruta del certificado (sri.rutaCertificado en config.properties).");
            }

            String xmlFirmado = FirmaXadesBes.firmar(xml, rutaCertificado, claveCertificado);

            SriSoapClient soapClient = new SriSoapClient();
            ResultadoRecepcion recepcion = soapClient.enviarRecepcion(xmlFirmado, configuracion);
            if (!recepcion.isRecibida()) {
                return marcarEstado(numFac, claveAcceso, null, null, ESTADO_SRI_RECHAZADO, recepcion.getMensaje());
            }

            for (int intento = 1; intento <= INTENTOS_AUTORIZACION; intento++) {
                try {
                    Thread.sleep(ESPERA_ENTRE_INTENTOS_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
                ResultadoAutorizacion autorizacion = soapClient.consultarAutorizacion(claveAcceso, configuracion);
                if (autorizacion.getEstado() == ResultadoAutorizacion.Estado.PENDIENTE) {
                    continue; // el SRI aún no lo procesa; reintentar
                }
                String fechaAut = autorizacion.getFechaAutorizacion() != null
                        ? autorizacion.getFechaAutorizacion().toString() : null;
                String estado = autorizacion.getEstado() == ResultadoAutorizacion.Estado.AUTORIZADO
                        ? ESTADO_SRI_AUTORIZADO : ESTADO_SRI_RECHAZADO;
                return marcarEstado(numFac, claveAcceso, autorizacion.getNumeroAutorizacion(), fechaAut,
                        estado, autorizacion.getMensaje());
            }

            return marcarPendiente(numFac, claveAcceso, null,
                    "El SRI no autorizó el comprobante en el tiempo de espera; reintentar más tarde.");
        } catch (Exception e) {
            Mod_log.error("Error procesando la facturación electrónica de " + numFac, e);
            return marcarPendiente(numFac, null, null, "Error interno al procesar el SRI: " + e.getMessage());
        }
    }

    private ResultadoEmisionFactura marcarPendiente(String numFac, String claveAcceso, String fechaAut, String mensaje) {
        return marcarEstado(numFac, claveAcceso, null, fechaAut, ESTADO_SRI_PENDIENTE, mensaje);
    }

    private ResultadoEmisionFactura marcarEstado(String numFac, String claveAcceso, String numeroAutorizacion,
                                                  String fechaAut, String estado, String mensaje) {
        String sql = "UPDATE dbo.Factura SET fe_clave = ?, fe_autorizacion = ?, fe_fecha_aut = ?, "
                + "fe_estado = ?, fe_mensaje = ? WHERE fac_numero = ?";
        try {
            if (!bd.conectarBD()) {
                Mod_log.warning("No se pudo guardar el estado SRI de " + numFac + " (sin conexión a BD)");
                return new ResultadoEmisionFactura(numFac, estado, mensaje);
            }
            try (PreparedStatement ps = bd.getConexion().prepareStatement(sql)) {
                ps.setString(1, claveAcceso);
                ps.setString(2, numeroAutorizacion);
                if (fechaAut != null) {
                    ps.setTimestamp(3, java.sql.Timestamp.valueOf(OffsetDateTime.parse(fechaAut).toLocalDateTime()));
                } else {
                    ps.setNull(3, java.sql.Types.TIMESTAMP);
                }
                ps.setString(4, estado);
                ps.setString(5, mensaje);
                ps.setString(6, numFac);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            Mod_log.error("No se pudo guardar el estado SRI de " + numFac, e);
        } finally {
            bd.desconectarBD();
        }
        return new ResultadoEmisionFactura(numFac, estado, mensaje);
    }

    /** Reintenta la autorización SRI de una factura que quedó PENDIENTE (botón manual o reintento en segundo plano). */
    public ResultadoEmisionFactura reintentarAutorizacionSri(String numFac) throws SQLException {
        ConfiguracionSri configuracion = new Mad_configuracionSri().obtener();
        String claveAcceso = obtenerClaveAcceso(numFac);
        if (claveAcceso == null || claveAcceso.isBlank()) {
            return new ResultadoEmisionFactura(numFac, ESTADO_SRI_PENDIENTE,
                    "No hay una clave de acceso generada todavía para esta factura.");
        }

        SriSoapClient soapClient = new SriSoapClient();
        ResultadoAutorizacion autorizacion = soapClient.consultarAutorizacion(claveAcceso, configuracion);
        if (autorizacion.getEstado() == ResultadoAutorizacion.Estado.PENDIENTE) {
            return new ResultadoEmisionFactura(numFac, ESTADO_SRI_PENDIENTE, autorizacion.getMensaje());
        }

        String fechaAut = autorizacion.getFechaAutorizacion() != null ? autorizacion.getFechaAutorizacion().toString() : null;
        String estado = autorizacion.getEstado() == ResultadoAutorizacion.Estado.AUTORIZADO
                ? ESTADO_SRI_AUTORIZADO : ESTADO_SRI_RECHAZADO;
        return marcarEstado(numFac, claveAcceso, autorizacion.getNumeroAutorizacion(), fechaAut, estado, autorizacion.getMensaje());
    }

    /** Datos SRI ya guardados de una factura (para imprimir/reimprimir el RIDE). */
    public upse.calculacion.modelo.DatosSriFactura obtenerDatosSri(String numFac) throws SQLException {
        String sql = "SELECT fe_clave, fe_autorizacion, fe_fecha_aut, fe_estado FROM dbo.Factura WHERE fac_numero = ?";
        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }
        try (PreparedStatement ps = bd.getConexion().prepareStatement(sql)) {
            ps.setString(1, numFac);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return new upse.calculacion.modelo.DatosSriFactura(null, null, null, ESTADO_SRI_PENDIENTE);
                }
                java.sql.Timestamp fechaAut = rs.getTimestamp("fe_fecha_aut");
                return new upse.calculacion.modelo.DatosSriFactura(
                        rs.getString("fe_clave"),
                        rs.getString("fe_autorizacion"),
                        fechaAut != null ? fechaAut.toLocalDateTime().toString() : null,
                        rs.getString("fe_estado"));
            }
        } finally {
            bd.desconectarBD();
        }
    }

    /** Números de factura con estado SRI PENDIENTE emitidas en las últimas {@code horas} horas. */
    public List<String> listarPendientesSri(int horas) throws SQLException {
        List<String> pendientes = new java.util.ArrayList<>();
        String sql = "SELECT fac_numero FROM dbo.Factura "
                + "WHERE fe_estado = ? AND fac_fecha >= DATEADD(hour, -?, GETDATE())";

        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }
        try (PreparedStatement ps = bd.getConexion().prepareStatement(sql)) {
            ps.setString(1, ESTADO_SRI_PENDIENTE);
            ps.setInt(2, horas);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pendientes.add(rs.getString("fac_numero"));
                }
            }
        } finally {
            bd.desconectarBD();
        }
        return pendientes;
    }

    private String obtenerClaveAcceso(String numFac) throws SQLException {
        String sql = "SELECT fe_clave FROM dbo.Factura WHERE fac_numero = ?";
        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }
        try (PreparedStatement ps = bd.getConexion().prepareStatement(sql)) {
            ps.setString(1, numFac);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("fe_clave") : null;
            }
        } finally {
            bd.desconectarBD();
        }
    }

    /**
     * Anula la factura en una sola transacción:
     * 1. Busca fac_id a partir del fac_numero.
     * 2. Lee DetalleFactura y acumula cantidades por prod_id.
     * 3. Restaura prod_stock para cada producto.
     * 4. Marca Factura con estado 'E'.
     */
    public boolean anularFactura(String numFac) throws SQLException {
        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        try {
            bd.iniciarTransaccion();

            // 1. Obtener fac_id
            int facId = 0;
            String sqlId = "SELECT fac_id FROM dbo.Factura WHERE fac_numero = ? AND fac_estado = 'A'";
            try (PreparedStatement ps = bd.getConexion().prepareStatement(sqlId)) {
                ps.setString(1, numFac);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        bd.rollback();
                        return false;
                    }
                    facId = rs.getInt("fac_id");
                }
            }

            // 2. Leer cantidades por prod_id
            Map<Integer, Float> cantidades = new LinkedHashMap<>();
            String sqlLineas = "SELECT prod_id, cantidad FROM dbo.DetalleFactura WHERE fac_id = ?";
            try (PreparedStatement ps = bd.getConexion().prepareStatement(sqlLineas)) {
                ps.setInt(1, facId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        cantidades.merge(rs.getInt("prod_id"), rs.getFloat("cantidad"), Float::sum);
                    }
                }
            }

            // 3. Restaurar stock
            String sqlRestaurar = "UPDATE dbo.Producto SET prod_stock = prod_stock + ? WHERE prod_id = ?";
            for (Map.Entry<Integer, Float> entrada : cantidades.entrySet()) {
                try (PreparedStatement ps = bd.getConexion().prepareStatement(sqlRestaurar)) {
                    ps.setFloat(1, entrada.getValue());
                    ps.setInt(2, entrada.getKey());
                    ps.executeUpdate();
                }
            }

            // 4. Marcar como anulada
            String sqlAnular = "UPDATE dbo.Factura SET fac_estado = 'E' WHERE fac_id = ?";
            try (PreparedStatement ps = bd.getConexion().prepareStatement(sqlAnular)) {
                ps.setInt(1, facId);
                ps.executeUpdate();
            }

            bd.commit();
            return true;

        } catch (SQLException e) {
            bd.rollback();
            throw e;
        } finally {
            bd.desconectarBD();
        }
    }

    private String generarNumero(ConfiguracionSri configuracion) throws SQLException {
        String prefijo = configuracion.getEstablecimiento() + "-" + configuracion.getPuntoEmision() + "-";
        String sql = "SELECT MAX(fac_numero) FROM dbo.Factura WHERE fac_numero LIKE ?";

        try (PreparedStatement ps = bd.getConexion().prepareStatement(sql)) {
            ps.setString(1, prefijo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                long siguiente = 1;
                if (rs.next()) {
                    String maxVal = rs.getString(1);
                    if (maxVal != null && maxVal.contains("-")) {
                        try {
                            siguiente = Long.parseLong(maxVal.substring(maxVal.lastIndexOf('-') + 1)) + 1;
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                return String.format("%s%09d", prefijo, siguiente);
            }
        }
    }
}
