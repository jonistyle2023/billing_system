package upse.calculacion.controlador;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import static upse.calculacion.general.Mod_general.DIRVISTAS;

public class ClientesController implements Initializable {

    @FXML
    private VBox dataPaneCliente;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicializacion
    }

    @FXML
    private void acc_nuevoCliente(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(DIRVISTAS + "Cliente.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Nuevo Cliente");
            stage.initModality(Modality.WINDOW_MODAL);
            if (dataPaneCliente != null && dataPaneCliente.getScene() != null) {
                stage.initOwner(dataPaneCliente.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No se pudo abrir la ventana de Cliente.");
            alert.showAndWait();
        }
    }

    @FXML
    private void acc_cerrar(ActionEvent event) {
        if (dataPaneCliente != null && dataPaneCliente.getParent() instanceof AnchorPane) {
            ((AnchorPane) dataPaneCliente.getParent()).getChildren().remove(dataPaneCliente);
        }
    }
}