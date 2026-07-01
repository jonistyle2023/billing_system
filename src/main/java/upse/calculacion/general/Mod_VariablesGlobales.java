package upse.calculacion.general;

public class Mod_VariablesGlobales {
    public static int num_fac=0;
    public static String formatoFechaEsp="dd/MM/yyyy";
    public static String formatoFechaIng="yyyy/MM/dd";

    /** Porcentaje de IVA vigente (ej. 15 = 15%). Se carga desde dbo.ParametroGeneral al iniciar la app. */
    public static float porcentajeIva = 15f;

    /** Tasa de IVA como fracción, lista para multiplicar (ej. 0.15f). */
    public static float getTasaIva() {
        return porcentajeIva / 100f;
    }
}

