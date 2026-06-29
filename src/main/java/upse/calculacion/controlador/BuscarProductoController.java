package upse.calculacion.controlador;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import upse.calculacion.Mad.Mad_producto;
import upse.calculacion.modelo.Producto;

public class BuscarProductoController implements Initializable {

    @FXML private TextField txt_buscar;
    @FXML private TableView<Producto> tblProductos;

    // Columna imagen: placeholder para implementación futura.
    // Cuando se defina el almacenamiento (ruta local, URL, BD), reemplazar
    // la CellFactory de colImg para mostrar un ImageView cargado desde prod.getImagen().
    @FXML private TableColumn<Producto, String> colImg;

    @FXML private TableColumn<Producto, String> colCod;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Float>  colPrecio;
    @FXML private TableColumn<Producto, Float>  colStock;
    @FXML private TableColumn<Producto, String> colIva;

    private final Mad_producto madProducto = new Mad_producto();
    private final ObservableList<Producto> productoList = FXCollections.observableArrayList();
    private Producto productoSeleccionado = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        cargarProductos("");

        // Doble clic equivale a "Agregar"
        tblProductos.setOnMouseClicked(ev -> {
            if (ev.getClickCount() == 2 && tblProductos.getSelectionModel().getSelectedItem() != null) {
                confirmarSeleccion();
            }
        });
    }

    private void configurarTabla() {
        colCod.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("pvpMenor"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colIva.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().isAplicaIva() ? "Sí" : "No"));

        // Imagen: CellFactory vacía hasta que se defina el almacenamiento
        colImg.setCellValueFactory(new PropertyValueFactory<>("imagen"));
        colImg.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(null);
                // TODO: cuando se defina storage, reemplazar por ImageView:
                // if (!empty && item != null && !item.isBlank()) {
                //     ImageView iv = new ImageView(new Image(item, 50, 50, true, true));
                //     setGraphic(iv);
                // }
            }
        });

        tblProductos.setItems(productoList);
    }

    private void cargarProductos(String criterio) {
        try {
            productoList.setAll(madProducto.buscarProductos(criterio));
        } catch (SQLException e) {
            mostrarError("Error al cargar productos: " + e.getMessage());
        }
    }

    @FXML
    private void acc_buscar(ActionEvent event) {
        cargarProductos(txt_buscar.getText());
    }

    @FXML
    private void acc_agregar(ActionEvent event) {
        confirmarSeleccion();
    }

    @FXML
    private void acc_cancelar(ActionEvent event) {
        productoSeleccionado = null;
        cerrarVentana();
    }

    private void confirmarSeleccion() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Seleccione un producto de la lista.");
            return;
        }
        productoSeleccionado = seleccionado;
        cerrarVentana();
    }

    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }

    private void cerrarVentana() {
        ((Stage) tblProductos.getScene().getWindow()).close();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
