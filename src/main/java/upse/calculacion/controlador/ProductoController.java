package upse.calculacion.controlador;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class ProductoController implements Initializable {

    @FXML
    private VBox productoPane;

    @FXML
    private void acc_volver(ActionEvent event) {
        if (productoPane != null && productoPane.getParent() instanceof AnchorPane) {
            ((AnchorPane) productoPane.getParent()).getChildren().remove(productoPane);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicializacion del modulo de productos.
    }
}