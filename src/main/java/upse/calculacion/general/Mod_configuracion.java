package upse.calculacion.general;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Configuración propia de cada instalación (servidor de BD, credenciales, ruta del
 * certificado de firma electrónica), externa al código fuente/compilado para que un
 * mismo instalador sirva a distintos clientes sin recompilar. Vive en
 * %APPDATA%\facturacion\config.properties y se crea con valores por defecto (los que
 * antes estaban hardcodeados) si todavía no existe.
 */
public class Mod_configuracion {

    private static final Path ARCHIVO = Path.of(System.getenv("APPDATA"), "facturacion", "config.properties");

    private static final Properties PROPIEDADES = cargar();

    private Mod_configuracion() {
    }

    private static Properties cargar() {
        Properties props = new Properties();
        try {
            if (!Files.exists(ARCHIVO)) {
                crearConValoresPorDefecto(props);
            }
            try (InputStream entrada = Files.newInputStream(ARCHIVO)) {
                props.load(entrada);
            }
        } catch (IOException e) {
            Mod_log.error("No se pudo cargar " + ARCHIVO + ", se usarán valores por defecto en memoria", e);
            valoresPorDefecto(props);
        }
        return props;
    }

    private static void crearConValoresPorDefecto(Properties props) throws IOException {
        valoresPorDefecto(props);
        Files.createDirectories(ARCHIVO.getParent());
        try (OutputStream salida = Files.newOutputStream(ARCHIVO)) {
            props.store(salida, "Configuración de Facturación - editar según la instalación de cada cliente");
        }
    }

    private static void valoresPorDefecto(Properties props) {
        props.setProperty("db.mariadb.servidor", "localhost");
        props.setProperty("db.mariadb.baseDatos", "base20261");
        props.setProperty("db.mariadb.usuario", "root");
        props.setProperty("db.mariadb.clave", "");

        props.setProperty("db.sqlserver.servidor", "localhost");
        props.setProperty("db.sqlserver.baseDatos", "BD2026_1");
        props.setProperty("db.sqlserver.usuario", "sa");
        props.setProperty("db.sqlserver.clave", "Admin.");

        props.setProperty("sri.rutaCertificado", "");
        props.setProperty("sri.claveCertificado", "");
    }

    public static String get(String clave, String porDefecto) {
        return PROPIEDADES.getProperty(clave, porDefecto);
    }
}
