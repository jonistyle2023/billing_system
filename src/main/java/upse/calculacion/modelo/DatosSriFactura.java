package upse.calculacion.modelo;

/** Datos SRI guardados de una factura ya emitida (para reimprimir el RIDE). */
public class DatosSriFactura {

    private final String claveAcceso;
    private final String numeroAutorizacion;
    private final String fechaAutorizacion;
    private final String estadoSri;

    public DatosSriFactura(String claveAcceso, String numeroAutorizacion, String fechaAutorizacion, String estadoSri) {
        this.claveAcceso = claveAcceso;
        this.numeroAutorizacion = numeroAutorizacion;
        this.fechaAutorizacion = fechaAutorizacion;
        this.estadoSri = estadoSri;
    }

    public String getClaveAcceso() {
        return claveAcceso;
    }

    public String getNumeroAutorizacion() {
        return numeroAutorizacion;
    }

    public String getFechaAutorizacion() {
        return fechaAutorizacion;
    }

    public String getEstadoSri() {
        return estadoSri;
    }
}
