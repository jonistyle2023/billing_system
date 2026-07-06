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
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import upse.calculacion.Mad.Mad_cliente;
import upse.calculacion.modelo.Cliente;
import static upse.calculacion.general.Mod_general.DIRVISTAS;

public class ClientesController implements Initializable {

    private final Mad_cliente madCliente = new Mad_cliente();

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
    private TextField txt_buscar;

    @FXML
    private void acc_nuevoCliente(ActionEvent event) {
        abrirFormulario(null);
    }

    @FXML
    private void acc_modificar(ActionEvent event) {
        Cliente seleccionado = tblClientes != null ? tblClientes.getSelectionModel().getSelectedItem() : null;
        if (seleccionado == null) {
            mostrarError("Seleccione un cliente para modificar.");
            return;
        }
        abrirFormulario(seleccionado);
    }

    @FXML
    private void acc_eliminar(ActionEvent event) {
        Cliente seleccionado = tblClientes != null ? tblClientes.getSelectionModel().getSelectedItem() : null;
        if (seleccionado == null) {
            mostrarError("Seleccione un cliente para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Desea eliminar el cliente seleccionado?");
        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                if (!madCliente.eliminarCliente(seleccionado.getCedula())) {
                    mostrarError("No se pudo eliminar el cliente.");
                    return;
                }
                refreshTable();
            } catch (Exception e) {
                mostrarError("No se pudo eliminar el cliente: " + e.getMessage());
            }
        }
    }

    @FXML
    private void acc_buscar(ActionEvent event) {
        buscarClientes();
    }

    /** Permite refrescar la tabla desde afuera (p.ej. tras usar el acceso directo "Nuevo Cliente" del menú principal). */
    public void refrescar() {
        refreshTable();
    }

    private void abrirFormulario(Cliente cliente) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(DIRVISTAS + "Cliente.fxml"));
            loader.setResources(App.getBundle());
            Parent root = loader.load();
            ClienteController controller = loader.getController();
            controller.setCliente(cliente);

            Stage stage = new Stage();
            stage.setTitle(cliente == null ? "Nuevo Cliente" : "Modificar Cliente");
            stage.initModality(Modality.WINDOW_MODAL);
            if (dataPaneCliente != null && dataPaneCliente.getScene() != null) {
                stage.initOwner(dataPaneCliente.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            stage.showAndWait();
            // Después de cerrar la ventana de cliente, refrescar la tabla
            refreshTable();
        } catch (IOException ex) {
            mostrarError("No se pudo abrir la ventana de Cliente.");
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
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
        cargarTablaDesdeConsulta(false);
    }

    private void buscarClientes() {
        cargarTablaDesdeConsulta(true);
    }

    private void cargarTablaDesdeConsulta(boolean usarFiltro) {
        try {
            String criterio = txt_buscar != null ? txt_buscar.getText() : "";
            ObservableList<Cliente> data = usarFiltro
                    ? madCliente.buscarClientes(criterio)
                    : madCliente.listarClientes();
            if (tblClientes != null) {
                tblClientes.setItems(data);
                tblClientes.refresh();
            }
        } catch (Exception e) {
            mostrarError("Error al actualizar la lista de clientes: " + e.getMessage());
        }
    }
}