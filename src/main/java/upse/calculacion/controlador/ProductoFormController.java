package upse.calculacion.controlador;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import upse.calculacion.Mad.Mad_producto;
import upse.calculacion.modelo.Producto;

public class ProductoFormController implements Initializable {

    @FXML
    private TextField txt_codigo;

    @FXML
    private TextField txt_nombre;

    @FXML
    private TextField txt_precioCompra;

    @FXML
    private TextField txt_pvpMenor;

    @FXML
    private TextField txt_pvpMayor;

    @FXML
    private TextField txt_stock;

    @FXML
    private TextField txt_imagen;

    @FXML
    private CheckBox chk_iva;

    private final Mad_producto madProducto = new Mad_producto();
    private Producto producto;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
        if (producto != null) {
            txt_codigo.setText(producto.getCodigo());
            txt_nombre.setText(producto.getNombre());
            txt_precioCompra.setText(String.valueOf(producto.getPrecioCompra()));
            txt_pvpMenor.setText(String.valueOf(producto.getPvpMenor()));
            txt_pvpMayor.setText(String.valueOf(producto.getPvpMayor()));
            txt_stock.setText(String.valueOf(producto.getStock()));
            txt_imagen.setText(producto.getImagen());
            chk_iva.setSelected(producto.isAplicaIva());
        }
    }

    @FXML
    private void acc_guardar(ActionEvent event) {
        try {
            Producto obj = producto != null ? producto : new Producto();
            obj.setCodigo(txt_codigo.getText());
            obj.setNombre(txt_nombre.getText());
            obj.setPrecioCompra(parseFloat(txt_precioCompra, "precio de compra"));
            obj.setPvpMenor(parseFloat(txt_pvpMenor, "PVP menor"));
            obj.setPvpMayor(parseFloat(txt_pvpMayor, "PVP mayor"));
            obj.setStock(parseFloat(txt_stock, "stock"));
            obj.setImagen(txt_imagen.getText());
            obj.setAplicaIva(chk_iva.isSelected());

            if (obj.getCodigo() == null || obj.getCodigo().trim().isEmpty()) {
                mostrarError("El código no puede estar vacío.");
                return;
            }
            if (obj.getNombre() == null || obj.getNombre().trim().isEmpty()) {
                mostrarError("El nombre no puede estar vacío.");
                return;
            }

            if (!madProducto.guardarProducto(obj)) {
                mostrarError("No se pudo guardar el producto.");
                return;
            }
            cerrar();
        } catch (Exception e) {
            mostrarError("No se pudo guardar el producto: " + e.getMessage());
        }
    }

    @FXML
    private void acc_cancelar(ActionEvent event) {
        cerrar();
    }

    private float parseFloat(TextField field, String nombreCampo) {
        String valor = field.getText();
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " es obligatorio.");
        }
        return Float.parseFloat(valor.trim());
    }

    private void cerrar() {
        Stage stage = (Stage) txt_codigo.getScene().getWindow();
        stage.close();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
