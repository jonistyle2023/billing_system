package upse.calculacion.modelo;

import upse.calculacion.general.Mod_VariablesGlobales;

public class DetFactura {
    private int prod_id;
    private String prod_cod;
    private String prod_nombre;
    private float cantidad;
    private float precio;
    private boolean aplicaIva;
    private float total;

    public DetFactura(String prod_cod, String prod_nombre, float cantidad, float precio, boolean aplicaIva, float total) {
        this.prod_id = 0;
        this.prod_cod = prod_cod;
        this.prod_nombre = prod_nombre;
        this.cantidad = cantidad;
        this.precio = precio;
        this.aplicaIva = aplicaIva;
        this.total = total;
    }

    public int getProd_id() { return prod_id; }
    public void setProd_id(int prod_id) { this.prod_id = prod_id; }

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

    public float getTotalConIva() {
        return aplicaIva ? total * (1f + Mod_VariablesGlobales.getTasaIva()) : total;
    }
}
