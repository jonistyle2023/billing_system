package upse.calculacion.general;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Logging centralizado a archivo (además de consola), para que los errores
 * sean diagnosticables en soporte aun cuando la app corre empaquetada sin
 * consola visible. Archivo rotativo en %APPDATA%\facturacion\logs\.
 */
public class Mod_log {

    private static final Path CARPETA_LOGS = Path.of(System.getenv("APPDATA"), "facturacion", "logs");
    private static final Logger LOGGER = Logger.getLogger("facturacion");

    static {
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.ALL);
        try {
            Files.createDirectories(CARPETA_LOGS);
            FileHandler fileHandler = new FileHandler(
                    CARPETA_LOGS.resolve("facturacion.log").toString(), 1_000_000, 5, true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);
            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            System.err.println("No se pudo inicializar el archivo de log: " + e.getMessage());
        }
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        LOGGER.addHandler(consoleHandler);
    }

    private Mod_log() {
    }

    public static void info(String mensaje) {
        LOGGER.info(mensaje);
    }

    public static void warning(String mensaje) {
        LOGGER.warning(mensaje);
    }

    public static void error(String mensaje, Throwable error) {
        LOGGER.log(Level.SEVERE, mensaje, error);
    }

    public static void error(String mensaje) {
        LOGGER.log(Level.SEVERE, mensaje);
    }
}
