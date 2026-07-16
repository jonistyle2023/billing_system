package upse.calculacion.modelo;

/** Resultado de emitir una factura: el número siempre se genera; el estado SRI puede quedar pendiente. */
public class ResultadoEmisionFactura {

    private final String numeroFactura;
    private final String estadoSri;
    private final String mensajeSri;

    public ResultadoEmisionFactura(String numeroFactura, String estadoSri, String mensajeSri) {
        this.numeroFactura = numeroFactura;
        this.estadoSri = estadoSri;
        this.mensajeSri = mensajeSri;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public String getEstadoSri() {
        return estadoSri;
    }

    public String getMensajeSri() {
        return mensajeSri;
    }
}
