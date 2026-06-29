package upse.calculacion.Mad;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import upse.calculacion.general.Mod_DB;
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
     * 1. Genera el siguiente número secuencial.
     * 2. Inserta cab_Factura (estado 'A').
     * 3. Por cada línea: inserta det_Factura y descuenta prod_stock.
     * Si cualquier paso falla → rollback completo. Nada queda a medias.
     */
    public String emitirFactura(String cliId, LocalDate fecha,
                                float subtotal, float baseCero, float iva, float total,
                                int usrId, List<DetFactura> detalles) throws SQLException {
        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        try {
            bd.iniciarTransaccion();

            String numFac = generarNumero();

            String sqlCab = "INSERT INTO dbo.cab_Factura "
                    + "(cab_numFac, cab_fechaFac, cli_id, cab_subtotal, cab_basecero, cab_iva, cab_total, cab_estado, usr_id) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, 'A', ?)";
            try (PreparedStatement ps = bd.getConexion().prepareStatement(sqlCab)) {
                ps.setString(1, numFac);
                ps.setDate(2, java.sql.Date.valueOf(fecha));
                ps.setString(3, cliId);
                ps.setFloat(4, subtotal);
                ps.setFloat(5, baseCero);
                ps.setFloat(6, iva);
                ps.setFloat(7, total);
                ps.setInt(8, usrId);
                ps.executeUpdate();
            }

            String sqlDet   = "INSERT INTO dbo.det_Factura "
                    + "(cab_numFac, pro_id, det_cantidad, det_iva, det_pvp, det_total) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            String sqlStock = "UPDATE dbo.Producto SET prod_stock = prod_stock - ? WHERE prod_cod = ?";

            for (DetFactura det : detalles) {
                if (det.getProd_cod() == null || det.getProd_cod().isEmpty()) continue;

                try (PreparedStatement ps = bd.getConexion().prepareStatement(sqlDet)) {
                    ps.setString(1, numFac);
                    ps.setString(2, det.getProd_cod());
                    ps.setFloat(3, det.getCantidad());
                    ps.setFloat(4, det.isAplicaIva() ? det.getTotal() * 0.15f : 0f);
                    ps.setFloat(5, det.getPrecio());
                    ps.setFloat(6, det.getTotal());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = bd.getConexion().prepareStatement(sqlStock)) {
                    ps.setFloat(1, det.getCantidad());
                    ps.setString(2, det.getProd_cod());
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
     * 1. Lee las líneas de det_Factura y acumula cantidades por producto.
     * 2. Restaura prod_stock para cada producto involucrado.
     * 3. Marca cab_Factura con estado 'E'.
     * Si la factura ya estaba anulada o no existe → rollback, devuelve false.
     */
    public boolean anularFactura(String numFac) throws SQLException {
        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        try {
            bd.iniciarTransaccion();

            // 1. Leer cantidades vendidas; acumular por si el mismo producto aparece varias veces
            Map<String, Float> cantidades = new LinkedHashMap<>();
            String sqlLineas = "SELECT pro_id, det_cantidad FROM dbo.det_Factura WHERE cab_numFac = ?";
            try (PreparedStatement ps = bd.getConexion().prepareStatement(sqlLineas)) {
                ps.setString(1, numFac);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        cantidades.merge(rs.getString("pro_id"), rs.getFloat("det_cantidad"), Float::sum);
                    }
                }
            }

            // 2. Restaurar stock (RS ya cerrado; no hay conflicto de DataReader)
            String sqlRestaurar = "UPDATE dbo.Producto SET prod_stock = prod_stock + ? WHERE prod_cod = ?";
            for (Map.Entry<String, Float> entrada : cantidades.entrySet()) {
                try (PreparedStatement ps = bd.getConexion().prepareStatement(sqlRestaurar)) {
                    ps.setFloat(1, entrada.getValue());
                    ps.setString(2, entrada.getKey());
                    ps.executeUpdate();
                }
            }

            // 3. Marcar factura como anulada
            String sqlAnular = "UPDATE dbo.cab_Factura SET cab_estado = 'E' "
                    + "WHERE cab_numFac = ? AND cab_estado = 'A'";
            int filas;
            try (PreparedStatement ps = bd.getConexion().prepareStatement(sqlAnular)) {
                ps.setString(1, numFac);
                filas = ps.executeUpdate();
            }

            if (filas == 0) {
                bd.rollback();
                return false;
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

    // Genera el siguiente número secuencial dentro de la transacción abierta.
    private String generarNumero() throws SQLException {
        String prefijo = ESTABLECIMIENTO + "-" + PUNTO_EMISION + "-";
        String sql = "SELECT MAX(cab_numFac) FROM dbo.cab_Factura WHERE cab_numFac LIKE ?";

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
