package upse.calculacion.controlador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.scene.image.Image;
import upse.calculacion.general.Mod_general;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static ResourceBundle bundle;

    @Override
    public void start(Stage stage) throws IOException {
        cambiarIdioma(new Locale("es")); // Default language
        scene = new Scene(loadFXML("Login"), 640, 480);
        stage.setScene(scene);
        stage.setTitle(bundle.getString("login.titulo"));
        try {
            stage.getIcons().add(new Image(App.class.getResourceAsStream("/upse/calculacion/vistas/recursos/fax.png")));
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono: " + e.getMessage());
        }
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
    
    public static ResourceBundle getBundle() {
        return bundle;
    }

    public static void main(String[] args) {
        launch();
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