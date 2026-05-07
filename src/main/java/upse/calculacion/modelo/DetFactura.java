package upse.calculacion.modelo;

public class DetFactura {
    private String prod_cod;
    private String prod_nombre;
    private float cantidad;
    private float precio;
    private boolean aplicaIva;
    private float total;

    public DetFactura(String prod_cod, String prod_nombre, float cantidad, float precio, boolean aplicaIva, float total) {
        this.prod_cod = prod_cod;
        this.prod_nombre = prod_nombre;
        this.cantidad = cantidad;
        this.precio = precio;
        this.aplicaIva = aplicaIva;
        this.total = total;
    }

    public String getProd_cod() {
        return prod_cod;
    }

    public void setProd_cod(String prod_cod) {
        this.prod_cod = prod_cod;
    }

    public String getProd_nombre() {
        return prod_nombre;
    }

    public void setProd_nombre(String prod_nombre) {
        this.prod_nombre = prod_nombre;
    }

    public float getCantidad() {
        return cantidad;
    }

    public void setCantidad(float cantidad) {
        this.cantidad = cantidad;
    }

    public float getPrecio() {
        return precio;
    }

    public void setPrecio(float precio) {
        this.precio = precio;
    }

    public boolean isAplicaIva() {
        return aplicaIva;
    }

    public void setAplicaIva(boolean aplicaIva) {
        this.aplicaIva = aplicaIva;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }
}
