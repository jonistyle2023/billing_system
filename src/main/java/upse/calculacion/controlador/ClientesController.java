package upse.calculacion.controlador;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import upse.calculacion.controlador.ClienteController;
import upse.calculacion.modelo.Cliente;
import static upse.calculacion.general.Mod_general.DIRVISTAS;

public class ClientesController implements Initializable {

    @FXML
    private VBox dataPaneCliente;

    @FXML
    private TableView<Cliente> tblClientes;

    @FXML
    private TableColumn<Cliente, String> colCedula;

    @FXML
    private TableColumn<Cliente, String> colNombres;

    @FXML
    private TableColumn<Cliente, String> colDireccion;

    @FXML
    private TableColumn<Cliente, String> colTelefono;

    

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
            // Después de cerrar la ventana de cliente, refrescar la tabla
            refreshTable();
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configurar columnas
        if (colCedula != null) colCedula.setCellValueFactory(new PropertyValueFactory<>("cedula"));
        if (colNombres != null) colNombres.setCellValueFactory(new PropertyValueFactory<>("nombres"));
        if (colDireccion != null) colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        if (colTelefono != null) colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        // Cargar datos iniciales
        refreshTable();
    }

    private void refreshTable() {
        try {
            ObservableList<Cliente> data = FXCollections.observableArrayList(ClienteController.clientes);
            if (tblClientes != null) {
                tblClientes.setItems(data);
                tblClientes.refresh();
            }
        } catch (Exception e) {
            // Mostrar error si hay problema al refrescar
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error al actualizar la lista de clientes: " + e.getMessage());
            alert.showAndWait();
        }
    }
}