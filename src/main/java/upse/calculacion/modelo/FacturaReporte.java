package upse.calculacion.modelo;

/** Fila del reporte "Listado de facturas". Los valores ya vienen formateados para mostrar/exportar. */
public class FacturaReporte {
    private final String numero;
    private final String fecha;
    private final String cliente;
    private final String subtotal;
    private final String subtotalCero;
    private final String iva;
    private final String total;
    private final String estado;
    private final String metodoPago;
    private final String estadoSri;

    public FacturaReporte(String numero, String fecha, String cliente, String subtotal,
                           String subtotalCero, String iva, String total, String estado, String metodoPago,
                           String estadoSri) {
        this.numero = numero;
        this.fecha = fecha;
        this.cliente = cliente;
        this.subtotal = subtotal;
        this.subtotalCero = subtotalCero;
        this.iva = iva;
        this.total = total;
        this.estado = estado;
        this.metodoPago = metodoPago;
        this.estadoSri = estadoSri;
    }

    public String getNumero() { return numero; }
    public String getFecha() { return fecha; }
    public String getCliente() { return cliente; }
    public String getSubtotal() { return subtotal; }
    public String getSubtotalCero() { return subtotalCero; }
    public String getIva() { return iva; }
    public String getTotal() { return total; }
    public String getEstado() { return estado; }
    public String getMetodoPago() { return metodoPago; }
    public String getEstadoSri() { return estadoSri; }
}
