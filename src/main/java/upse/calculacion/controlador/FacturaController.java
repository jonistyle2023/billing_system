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

public class FacturaController implements Initializable {

    @FXML
    private VBox facturaPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicializacion del modulo de factura.
    }

    @FXML
    private void acc_cerrar(ActionEvent event) {
        if (facturaPane != null && facturaPane.getParent() instanceof AnchorPane) {
            ((AnchorPane) facturaPane.getParent()).getChildren().remove(facturaPane);
        }
    }
}
