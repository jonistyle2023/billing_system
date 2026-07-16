package upse.calculacion.modelo;

/** Configuración de facturación electrónica SRI (fila única en dbo.ConfiguracionSri). */
public class ConfiguracionSri {

    public static final String AMBIENTE_PRUEBAS = "1";
    public static final String AMBIENTE_PRODUCCION = "2";
    public static final String TIPO_EMISION_NORMAL = "1";

    private String ambiente;
    private String establecimiento;
    private String puntoEmision;
    private String tipoEmision;

    public String getAmbiente() {
        return ambiente;
    }

    public void setAmbiente(String ambiente) {
        this.ambiente = ambiente;
    }

    public String getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(String establecimiento) {
        this.establecimiento = establecimiento;
    }

    public String getPuntoEmision() {
        return puntoEmision;
    }

    public void setPuntoEmision(String puntoEmision) {
        this.puntoEmision = puntoEmision;
    }

    public String getTipoEmision() {
        return tipoEmision;
    }

    public void setTipoEmision(String tipoEmision) {
        this.tipoEmision = tipoEmision;
    }

    public boolean esProduccion() {
        return AMBIENTE_PRODUCCION.equals(ambiente);
    }
}
