package upse.calculacion.sri;

/** Respuesta de RecepcionComprobantesOffline: si el XML fue aceptado para su posterior autorización. */
public class ResultadoRecepcion {

    private final boolean recibida;
    private final String mensaje;

    public ResultadoRecepcion(boolean recibida, String mensaje) {
        this.recibida = recibida;
        this.mensaje = mensaje;
    }

    public boolean isRecibida() {
        return recibida;
    }

    public String getMensaje() {
        return mensaje;
    }
}
