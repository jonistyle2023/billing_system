package upse.calculacion.sri;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Genera la clave de acceso de 49 dígitos que exige el SRI para comprobantes electrónicos.
 * Estructura (ficha técnica de comprobantes electrónicos, esquema offline):
 * <pre>
 *  1- 8  fecha de emisión (ddMMyyyy)
 *  9-10  tipo de comprobante ("01" = factura)
 * 11-23  RUC del emisor (13 dígitos)
 *    24  ambiente ("1" pruebas, "2" producción)
 * 25-27  establecimiento (3 dígitos)
 * 28-30  punto de emisión (3 dígitos)
 * 31-39  secuencial (9 dígitos)
 * 40-47  código numérico aleatorio (8 dígitos)
 *    48  tipo de emisión ("1" normal)
 *    49  dígito verificador (módulo 11)
 * </pre>
 */
public class ClaveAccesoGenerador {

    private static final String TIPO_COMPROBANTE_FACTURA = "01";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("ddMMyyyy");
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private ClaveAccesoGenerador() {
    }

    public static String generar(LocalDate fechaEmision, String ruc, String ambiente,
                                  String establecimiento, String puntoEmision,
                                  String secuencial, String tipoEmision) {
        if (ruc == null || ruc.length() != 13) {
            throw new IllegalArgumentException("El RUC debe tener 13 dígitos, recibido: " + ruc);
        }
        String secuencial9 = String.format("%09d", Long.parseLong(secuencial));
        String codigoNumerico = String.format("%08d", ALEATORIO.nextInt(100_000_000));

        String claveSinVerificador = fechaEmision.format(FORMATO_FECHA)
                + TIPO_COMPROBANTE_FACTURA
                + ruc
                + ambiente
                + establecimiento
                + puntoEmision
                + secuencial9
                + codigoNumerico
                + tipoEmision;

        if (claveSinVerificador.length() != 48) {
            throw new IllegalStateException(
                    "Clave de acceso mal formada (48 dígitos esperados, se generaron " + claveSinVerificador.length() + ")");
        }

        return claveSinVerificador + digitoVerificadorModulo11(claveSinVerificador);
    }

    /** Dígito verificador módulo 11 (mismo algoritmo usado para cédula/RUC en Ecuador). */
    static char digitoVerificadorModulo11(String digitos) {
        int suma = 0;
        int multiplicador = 2;
        for (int i = digitos.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(digitos.charAt(i)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }
        int residuo = suma % 11;
        int verificador = 11 - residuo;
        if (verificador == 11) verificador = 0;
        if (verificador == 10) verificador = 1;
        return Character.forDigit(verificador, 10);
    }
}
