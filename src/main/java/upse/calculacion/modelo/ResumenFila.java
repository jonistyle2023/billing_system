package upse.calculacion.modelo;

/** Fila genérica "concepto/valor" usada para mostrar el reporte "Resumen de ventas" en una tabla. */
public class ResumenFila {
    private final String concepto;
    private final String valor;

    public ResumenFila(String concepto, String valor) {
        this.concepto = concepto;
        this.valor = valor;
    }

    public String getConcepto() { return concepto; }
    public String getValor() { return valor; }
}
