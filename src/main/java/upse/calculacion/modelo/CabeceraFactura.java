package upse.calculacion.modelo;

import java.util.ArrayList;

public class CabeceraFactura {
    private int numFactura;
    private String fecha;
    private String numdocumento;
    private String nombres;
    private String direccion;
    private String telefono;
    private String correo;
    // falta el detalle de la venta
    private ArrayList<DetFactura> detalleFactura=new ArrayList<>();
    private float subtotal;
    private float subtotalcero;
    private float iva;
    private float total;

    public CabeceraFactura(int numFactura, String fecha, String numdocumento, String nombres, String direccion, String telefono, String correo, ArrayList<DetFactura> detalleFactura, float subtotal, float subtotalcero, float iva, float total) {
        this.numFactura = numFactura;
        this.fecha = fecha;
        this.numdocumento = numdocumento;
        this.nombres = nombres;
        this.direccion = direccion;
        this.telefono = telefono;
        this.correo = correo;
        this.detalleFactura = detalleFactura;
        this.subtotal = subtotal;
        this.subtotalcero = subtotalcero;
        this.iva = iva;
        this.total = total;
    }

    public int getNumFactura() {
        return numFactura;
    }

    public void setNumFactura(int numFactura) {
        this.numFactura = numFactura;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getNumdocumento() {
        return numdocumento;
    }

    public void setNumdocumento(String numdocumento) {
        this.numdocumento = numdocumento;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public ArrayList<DetFactura> getDetalleFactura() {
        return detalleFactura;
    }

    public void setDetalleFactura(ArrayList<DetFactura> detalleFactura) {
        this.detalleFactura = detalleFactura;
    }

    public float getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(float subtotal) {
        this.subtotal = subtotal;
    }

    public float getSubtotalcero() {
        return subtotalcero;
    }

    public void setSubtotalcero(float subtotalcero) {
        this.subtotalcero = subtotalcero;
    }

    public float getIva() {
        return iva;
    }

    public void setIva(float iva) {
        this.iva = iva;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }
}
