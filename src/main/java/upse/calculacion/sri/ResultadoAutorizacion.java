package upse.calculacion.sri;

import java.time.OffsetDateTime;

/** Respuesta de AutorizacionComprobantesOffline para una clave de acceso dada. */
public class ResultadoAutorizacion {

    public enum Estado { AUTORIZADO, RECHAZADO, PENDIENTE }

    private final Estado estado;
    private final String numeroAutorizacion;
    private final OffsetDateTime fechaAutorizacion;
    private final String mensaje;

    public ResultadoAutorizacion(Estado estado, String numeroAutorizacion, OffsetDateTime fechaAutorizacion, String mensaje) {
        this.estado = estado;
        this.numeroAutorizacion = numeroAutorizacion;
        this.fechaAutorizacion = fechaAutorizacion;
        this.mensaje = mensaje;
    }

    public Estado getEstado() {
        return estado;
    }

    public String getNumeroAutorizacion() {
        return numeroAutorizacion;
    }

    public OffsetDateTime getFechaAutorizacion() {
        return fechaAutorizacion;
    }

    public String getMensaje() {
        return mensaje;
    }
}
