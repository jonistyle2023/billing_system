package upse.calculacion.Mad;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import upse.calculacion.general.Mod_DB;
import upse.calculacion.modelo.FacturaReporte;
import upse.calculacion.modelo.ProductoRanking;
import upse.calculacion.modelo.ResumenVentas;

public class Mad_reporte {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Mod_DB bd;

    public Mad_reporte() {
        this(new Mod_DB());
    }

    public Mad_reporte(Mod_DB bd) {
        this.bd = bd;
    }

    /**
     * Listado de facturas emitidas en el rango [desde, hasta] (inclusive).
     * @param estado 'A' (activas), 'E' (anuladas) o null/"" para todas.
     */
    public ObservableList<FacturaReporte> listarFacturas(LocalDate desde, LocalDate hasta, String estado) throws SQLException {
        ObservableList<FacturaReporte> resultado = FXCollections.observableArrayList();
        boolean filtrarEstado = estado != null && !estado.isEmpty();

        StringBuilder sql = new StringBuilder(
                "SELECT f.fac_numero, f.fac_fecha, COALESCE(c.cli_nombres, '(Sin cliente)') AS cliente, "
                + "f.fac_subtotal, f.fac_subtotalcero, f.fac_iva, f.fac_total, f.fac_estado, f.fac_pago_metodo, "
                + "f.fe_estado "
                + "FROM dbo.Factura f "
                + "LEFT JOIN dbo.Cliente c ON c.cli_id = f.cli_id "
                + "WHERE f.fac_fecha >= ? AND f.fac_fecha < DATEADD(day, 1, ?) ");
        if (filtrarEstado) {
            sql.append("AND f.fac_estado = ? ");
        }
        sql.append("ORDER BY f.fac_fecha DESC, f.fac_numero DESC");

        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement ps = bd.getConexion().prepareStatement(sql.toString())) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            if (filtrarEstado) {
                ps.setString(3, estado);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(new FacturaReporte(
                            rs.getString("fac_numero"),
                            formatearFecha(rs.getDate("fac_fecha")),
                            rs.getString("cliente"),
                            formatearMonto(rs.getFloat("fac_subtotal")),
                            formatearMonto(rs.getFloat("fac_subtotalcero")),
                            formatearMonto(rs.getFloat("fac_iva")),
                            formatearMonto(rs.getFloat("fac_total")),
                            "A".equals(rs.getString("fac_estado")) ? "Activa" : "Anulada",
                            rs.getString("fac_pago_metodo"),
                            rs.getString("fe_estado") != null ? rs.getString("fe_estado") : "N/A"));
                }
            }
        } finally {
            bd.desconectarBD();
        }

        return resultado;
    }

    /** Totales de ventas (solo facturas activas) en el rango [desde, hasta] (inclusive). */
    public ResumenVentas obtenerResumenVentas(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT "
                + "COUNT(CASE WHEN fac_estado = 'A' THEN 1 END) AS num_activas, "
                + "COUNT(CASE WHEN fac_estado = 'E' THEN 1 END) AS num_anuladas, "
                + "SUM(CASE WHEN fac_estado = 'A' THEN fac_subtotal ELSE 0 END) AS subtotal, "
                + "SUM(CASE WHEN fac_estado = 'A' THEN fac_subtotalcero ELSE 0 END) AS subtotalcero, "
                + "SUM(CASE WHEN fac_estado = 'A' THEN fac_iva ELSE 0 END) AS iva, "
                + "SUM(CASE WHEN fac_estado = 'A' THEN fac_total ELSE 0 END) AS total, "
                + "SUM(CASE WHEN fac_estado = 'A' AND fac_pago_metodo = 'EFECTIVO' THEN fac_total ELSE 0 END) AS total_efectivo, "
                + "SUM(CASE WHEN fac_estado = 'A' AND fac_pago_metodo = 'TARJETA' THEN fac_total ELSE 0 END) AS total_tarjeta, "
                + "SUM(CASE WHEN fac_estado = 'A' AND fac_pago_metodo = 'TRANSFERENCIA' THEN fac_total ELSE 0 END) AS total_transferencia "
                + "FROM dbo.Factura "
                + "WHERE fac_fecha >= ? AND fac_fecha < DATEADD(day, 1, ?)";

        ResumenVentas resumen = new ResumenVentas();

        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement ps = bd.getConexion().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resumen.setNumFacturasActivas(rs.getInt("num_activas"));
                    resumen.setNumFacturasAnuladas(rs.getInt("num_anuladas"));
                    resumen.setSubtotal(rs.getFloat("subtotal"));
                    resumen.setSubtotalCero(rs.getFloat("subtotalcero"));
                    resumen.setIva(rs.getFloat("iva"));
                    resumen.setTotal(rs.getFloat("total"));
                    resumen.setTotalEfectivo(rs.getFloat("total_efectivo"));
                    resumen.setTotalTarjeta(rs.getFloat("total_tarjeta"));
                    resumen.setTotalTransferencia(rs.getFloat("total_transferencia"));
                }
            }
        } finally {
            bd.desconectarBD();
        }

        return resumen;
    }

    /** Productos con más cantidad vendida (solo facturas activas) en el rango [desde, hasta] (inclusive). */
    public ObservableList<ProductoRanking> rankingProductos(LocalDate desde, LocalDate hasta, int topN) throws SQLException {
        ObservableList<ProductoRanking> resultado = FXCollections.observableArrayList();

        String sql = "SELECT TOP (?) d.prod_id, MAX(d.prod_nombre) AS prod_nombre, MAX(p.prod_cod) AS prod_cod, "
                + "SUM(d.cantidad) AS cantidad_total, SUM(d.total) AS monto_total "
                + "FROM dbo.DetalleFactura d "
                + "INNER JOIN dbo.Factura f ON f.fac_id = d.fac_id "
                + "LEFT JOIN dbo.Producto p ON p.prod_id = d.prod_id "
                + "WHERE f.fac_estado = 'A' AND f.fac_fecha >= ? AND f.fac_fecha < DATEADD(day, 1, ?) "
                + "GROUP BY d.prod_id "
                + "ORDER BY cantidad_total DESC";

        if (!bd.conectarBD()) {
            throw new IllegalStateException("No se pudo conectar a la base de datos.");
        }

        try (PreparedStatement ps = bd.getConexion().prepareStatement(sql)) {
            ps.setInt(1, topN);
            ps.setDate(2, Date.valueOf(desde));
            ps.setDate(3, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(new ProductoRanking(
                            rs.getString("prod_cod"),
                            rs.getString("prod_nombre"),
                            formatearCantidad(rs.getFloat("cantidad_total")),
                            formatearMonto(rs.getFloat("monto_total"))));
                }
            }
        } finally {
            bd.desconectarBD();
        }

        return resultado;
    }

    private String formatearFecha(Date fecha) {
        return fecha == null ? "" : fecha.toLocalDate().format(FORMATO_FECHA);
    }

    private String formatearMonto(float monto) {
        return String.format(Locale.US, "%.2f", monto);
    }

    private String formatearCantidad(float cantidad) {
        return cantidad == Math.floor(cantidad)
                ? String.valueOf((long) cantidad)
                : String.format(Locale.US, "%.2f", cantidad);
    }
}
