package upse.calculacion.modelo;

/** Fila del reporte "Ranking de productos más vendidos". Los valores ya vienen formateados. */
public class ProductoRanking {
    private final String codigo;
    private final String nombre;
    private final String cantidad;
    private final String monto;

    public ProductoRanking(String codigo, String nombre, String cantidad, String monto) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.monto = monto;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getCantidad() { return cantidad; }
    public String getMonto() { return monto; }
}
