package upse.calculacion.Mad;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import upse.calculacion.general.Mod_DB;
import upse.calculacion.general.Mod_VariablesGlobales;
import upse.calculacion.modelo.DetFactura;

public class Mad_factura {

    private static final String ESTABLECIMIENTO = "001";
    private static final String PUNTO_EMISION   = "001";

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
     */
    public String emitirFactura(String cliCedula, LocalDate fecha,
                                float subtotal, float baseCero, float iva, float total,
                                int usrId, List<DetFactura> detalles,
                                String pagoMetodo, float pagoMonto, float pagoCambio) throws SQLException {
        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        try {
            bd.iniciarTransaccion();

            // 1. Buscar cli_id
            int cliId = 0;
            String sqlCli = "SELECT cli_id FROM dbo.Cliente WHERE cli_cedula = ? "
                    + "AND (cli_estado = 'A' OR cli_estado IS NULL)";
            try (PreparedStatement ps = bd.getConexion().prepareStatement(sqlCli)) {
                ps.setString(1, cliCedula);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) cliId = rs.getInt("cli_id");
                }
            }

            // 2. Generar número
            String numFac = generarNumero();

            // 3. Insertar cabecera en Factura
            String sqlFac = "INSERT INTO dbo.Factura "
                    + "(fac_numero, fac_fecha, cli_id, fac_subtotal, fac_subtotalcero, "
                    + " fac_iva, fac_descuento, fac_total, aud_usr_id_crea, fac_estado, "
                    + " fac_pago_metodo, fac_pago_monto, fac_pago_cambio) "
                    + "VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?, 'A', ?, ?, ?)";
            int facId;
            try (PreparedStatement ps = bd.getConexion().prepareStatement(sqlFac, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, numFac);
                ps.setDate(2, java.sql.Date.valueOf(fecha));
                if (cliId > 0) ps.setInt(3, cliId);
                else           ps.setNull(3, java.sql.Types.INTEGER);
                ps.setFloat(4, subtotal);
                ps.setFloat(5, baseCero);
                ps.setFloat(6, iva);
                ps.setFloat(7, total);
                ps.setInt(8, usrId);
                ps.setString(9, pagoMetodo);
                ps.setFloat(10, pagoMonto);
                ps.setFloat(11, pagoCambio);
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
            String sqlStock = "UPDATE dbo.Producto SET prod_stock = prod_stock - ? WHERE prod_id = ?";

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
                    ps.executeUpdate();
                }
            }

            bd.commit();
            return numFac;

        } catch (SQLException e) {
            bd.rollback();
            throw e;
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

    private String generarNumero() throws SQLException {
        String prefijo = ESTABLECIMIENTO + "-" + PUNTO_EMISION + "-";
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
