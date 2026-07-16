package upse.calculacion.controlador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.scene.image.Image;
import upse.calculacion.Mad.Mad_factura;
import upse.calculacion.Mad.Mad_parametro;
import upse.calculacion.general.Mod_general;
import upse.calculacion.general.Mod_log;
import upse.calculacion.general.Mod_VariablesGlobales;
import upse.calculacion.modelo.Usuario;

/**
 * JavaFX App
 */
public class App extends Application {

    private static final int INTERVALO_REINTENTO_SRI_MINUTOS = 5;
    private static final int ANTIGUEDAD_MAXIMA_REINTENTO_HORAS = 24;

    private static Scene scene;
    private static ResourceBundle bundle;
    private static Usuario usuarioActual;
    private static ScheduledExecutorService reintentosSri;

    @Override
    public void start(Stage stage) throws IOException {
        cambiarIdioma(Locale.of("es")); // Default language
        cargarParametrosGenerales();
        iniciarReintentosSriEnSegundoPlano();
        scene = new Scene(loadFXML("Login"), 640, 460);

        // Cargar hoja de estilos global
        try {
            scene.getStylesheets().add(App.class.getResource("/upse/calculacion/vistas/styles.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("No se pudo cargar la hoja de estilos: " + e.getMessage());
        }
        stage.setScene(scene);
        stage.setTitle(bundle.getString("login.titulo"));
        try {
            stage.getIcons().add(new Image(App.class.getResourceAsStream("/upse/calculacion/vistas/recursos/fax.png")));
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono: " + e.getMessage());
        }
        stage.setMinWidth(620);
        stage.setMinHeight(440);
        stage.setOnCloseRequest(event -> {
            if (!confirmarSalida("¿Está seguro que desea cerrar el sistema?")) {
                event.consume();
            }
        });
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }
    
    public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(Mod_general.DIRVISTAS + fxml + ".fxml"));
        fxmlLoader.setResources(bundle);
        return fxmlLoader.load();
    }

    public static void cambiarIdioma(Locale locale) {
        bundle = ResourceBundle.getBundle("upse/calculacion/idiomas/mensajes", locale);
    }

    /** Carga parámetros configurables (ej. IVA) desde dbo.ParametroGeneral. Si la BD no responde, se mantienen los valores por defecto. */
    private static void cargarParametrosGenerales() {
        try {
            Mod_VariablesGlobales.porcentajeIva =
                    new Mad_parametro().obtenerValorNumerico("IVA", Mod_VariablesGlobales.porcentajeIva);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el parámetro IVA, se usará el valor por defecto: " + e.getMessage());
        }
    }

    /**
     * Reintenta cada {@link #INTERVALO_REINTENTO_SRI_MINUTOS} minutos la autorización SRI de las
     * facturas que quedaron en estado PENDIENTE (contingencia: el SRI no respondió a tiempo al emitir).
     * No afecta la venta ya registrada; solo actualiza fe_estado/fe_autorizacion cuando el SRI conteste.
     */
    private static void iniciarReintentosSriEnSegundoPlano() {
        reintentosSri = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread hilo = new Thread(r, "reintentos-sri-segundo-plano");
            hilo.setDaemon(true);
            return hilo;
        });
        reintentosSri.scheduleWithFixedDelay(() -> {
            try {
                Mad_factura madFactura = new Mad_factura();
                List<String> pendientes = madFactura.listarPendientesSri(ANTIGUEDAD_MAXIMA_REINTENTO_HORAS);
                for (String numFac : pendientes) {
                    madFactura.reintentarAutorizacionSri(numFac);
                }
            } catch (Exception e) {
                Mod_log.warning("Reintento automático de autorización SRI falló: " + e.getMessage());
            }
        }, INTERVALO_REINTENTO_SRI_MINUTOS, INTERVALO_REINTENTO_SRI_MINUTOS, TimeUnit.MINUTES);
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static void setUsuarioActual(Usuario usuarioActual) {
        App.usuarioActual = usuarioActual;
    }

    public static void main(String[] args) {
        launch();
    }

    public static Stage getStage() {
        return scene != null ? (Stage) scene.getWindow() : null;
    }

    public static boolean confirmarSalida(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar salida");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

}
