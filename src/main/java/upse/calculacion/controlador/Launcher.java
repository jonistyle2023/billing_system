package upse.calculacion.controlador;

/**
 * Punto de entrada real del jar empaquetado. No extiende {@link javafx.application.Application}
 * a propósito: cuando el Main-Class de un jar sí lo extiende, "java -jar" exige módulo JavaFX en
 * el module-path y falla con "JavaFX runtime components are missing" aunque los jars de JavaFX
 * estén en el classpath (como ocurre en el jar sombreado generado por maven-shade-plugin).
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
