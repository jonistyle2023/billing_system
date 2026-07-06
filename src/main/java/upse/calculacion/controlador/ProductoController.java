package upse.calculacion.controlador;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import upse.calculacion.Mad.Mad_producto;
import upse.calculacion.modelo.Producto;
import static upse.calculacion.general.Mod_general.DIRVISTAS;

public class ProductoController implements Initializable {

    @FXML
    private VBox productoPane;

    @FXML
    private TextField txt_buscar;

    @FXML
    private TableView<Producto> tblProductos;

    @FXML
    private TableColumn<Producto, String> colCodigo;

    @FXML
    private TableColumn<Producto, String> colDescripcion;

    @FXML
    private TableColumn<Producto, Float> colPrecio;

    @FXML
    private TableColumn<Producto, Float> colPvpMenor;

    @FXML
    private TableColumn<Producto, Float> colStock;

    @FXML
    private TableColumn<Producto, Boolean> colIva;

    @FXML
    private Button btn_nuevo;

    @FXML
    private Button btn_modificar;

    @FXML
    private Button btn_eliminar;

    private final Mad_producto madProducto = new Mad_producto();

    @FXML
    private void acc_buscar(ActionEvent event) {
        cargarProductos(true);
    }

    @FXML
    private void acc_nuevo(ActionEvent event) {
        abrirFormulario(null);
    }

    @FXML
    private void acc_modificar(ActionEvent event) {
        Producto seleccionado = tblProductos != null ? tblProductos.getSelectionModel().getSelectedItem() : null;
        if (seleccionado == null) {
            mostrarError("Seleccione un producto para modificar.");
            return;
        }
        abrirFormulario(seleccionado);
    }

    @FXML
    private void acc_eliminar(ActionEvent event) {
        Producto seleccionado = tblProductos != null ? tblProductos.getSelectionModel().getSelectedItem() : null;
        if (seleccionado == null) {
            mostrarError("Seleccione un producto para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Desea eliminar el producto seleccionado?");
        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                if (!madProducto.eliminarProducto(seleccionado.getId())) {
                    mostrarError("No se pudo eliminar el producto.");
                    return;
                }
                cargarProductos(false);
            } catch (Exception e) {
                mostrarError("No se pudo eliminar el producto: " + e.getMessage());
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (colCodigo != null) colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        if (colDescripcion != null) colDescripcion.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        if (colPrecio != null) colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioCompra"));
        if (colPvpMenor != null) colPvpMenor.setCellValueFactory(new PropertyValueFactory<>("pvpMenor"));
        if (colStock != null) colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        if (colIva != null) colIva.setCellValueFactory(new PropertyValueFactory<>("aplicaIva"));

        cargarProductos();
    }

    private void cargarProductos() {
        cargarProductos(false);
    }

    private void cargarProductos(boolean conFiltro) {
        try {
            String criterio = txt_buscar != null ? txt_buscar.getText() : "";
            ObservableList<Producto> productos = conFiltro
                    ? madProducto.buscarProductos(criterio)
                    : madProducto.listarProductos();
            if (tblProductos != null) {
                tblProductos.setItems(productos);
                tblProductos.refresh();
            }
        } catch (Exception e) {
            mostrarError("No se pudieron cargar los productos: " + e.getMessage());
        }
    }

    private void abrirFormulario(Producto producto) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(DIRVISTAS + "ProductoForm.fxml"));
            loader.setResources(App.getBundle());
            Parent root = loader.load();
            ProductoFormController controller = loader.getController();
            controller.setProducto(producto);

            Stage stage = new Stage();
            stage.setTitle(producto == null ? "Nuevo producto" : "Modificar producto");
            stage.initModality(Modality.WINDOW_MODAL);
            if (productoPane != null && productoPane.getScene() != null) {
                stage.initOwner(productoPane.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            stage.showAndWait();
            cargarProductos(false);
        } catch (IOException ex) {
            mostrarError("No se pudo abrir el formulario de producto.");
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}