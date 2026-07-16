package upse.calculacion.general;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hash de contraseñas con PBKDF2WithHmacSHA256 (estándar del JDK, sin dependencias
 * nuevas). Formato almacenado: "iteraciones:salt_base64:hash_base64".
 */
public class Mod_hash {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 120_000;
    private static final int LONGITUD_CLAVE_BITS = 256;
    private static final int LONGITUD_SALT_BYTES = 16;

    private Mod_hash() {
    }

    public static String hashear(String claveEnTextoPlano) {
        byte[] salt = new byte[LONGITUD_SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = derivar(claveEnTextoPlano, salt, ITERACIONES);
        return ITERACIONES + ":" + Base64.getEncoder().encodeToString(salt) + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    /** true si {@code valorAlmacenado} tiene el formato "iteraciones:salt:hash" producido por {@link #hashear}. */
    public static boolean esFormatoHash(String valorAlmacenado) {
        if (valorAlmacenado == null) return false;
        String[] partes = valorAlmacenado.split(":");
        if (partes.length != 3) return false;
        try {
            Integer.parseInt(partes[0]);
            Base64.getDecoder().decode(partes[1]);
            Base64.getDecoder().decode(partes[2]);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean verificar(String claveEnTextoPlano, String valorAlmacenado) {
        String[] partes = valorAlmacenado.split(":");
        int iteraciones = Integer.parseInt(partes[0]);
        byte[] salt = Base64.getDecoder().decode(partes[1]);
        byte[] hashEsperado = Base64.getDecoder().decode(partes[2]);
        byte[] hashCalculado = derivar(claveEnTextoPlano, salt, iteraciones);
        return java.security.MessageDigest.isEqual(hashEsperado, hashCalculado);
    }

    private static byte[] derivar(String claveEnTextoPlano, byte[] salt, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(claveEnTextoPlano.toCharArray(), salt, iteraciones, LONGITUD_CLAVE_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITMO);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        }
    }
}
