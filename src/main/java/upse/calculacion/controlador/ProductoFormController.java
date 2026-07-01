package upse.calculacion.controlador;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import upse.calculacion.Mad.Mad_producto;
import upse.calculacion.modelo.Producto;

public class ProductoFormController implements Initializable {

    @FXML private TextField txt_codigo;
    @FXML private TextField txt_nombre;
    @FXML private TextField txt_precioCompra;
    @FXML private TextField txt_pvpMenor;
    @FXML private TextField txt_pvpMayor;
    @FXML private TextField txt_stock;
    @FXML private CheckBox  chk_iva;
    @FXML private ImageView imgPreview;
    @FXML private Label     lbl_nombreImagen;
    @FXML private Button    btn_quitarImagen;

    private static final Path CARPETA_IMAGENES = Path.of(
            System.getenv("APPDATA"), "facturacion", "imagenes");

    private final Mad_producto madProducto = new Mad_producto();
    private Producto producto;
    private String imagenNombre;

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
            chk_iva.setSelected(producto.isAplicaIva());
            imagenNombre = producto.getImagen();
            if (imagenNombre != null && !imagenNombre.isBlank()) {
                mostrarPreview(CARPETA_IMAGENES.resolve(imagenNombre));
            }
        }
    }

    @FXML
    private void acc_seleccionarImagen(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar imagen del producto");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File archivo = chooser.showOpenDialog(txt_codigo.getScene().getWindow());
        if (archivo == null) return;

        try {
            Files.createDirectories(CARPETA_IMAGENES);
            String nombre = archivo.getName();
            Path destino = CARPETA_IMAGENES.resolve(nombre);
            Files.copy(archivo.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
            imagenNombre = nombre;
            mostrarPreview(destino);
        } catch (IOException e) {
            mostrarError("No se pudo copiar la imagen: " + e.getMessage());
        }
    }

    @FXML
    private void acc_quitarImagen(ActionEvent event) {
        imagenNombre = null;
        imgPreview.setImage(null);
        lbl_nombreImagen.setText("");
        btn_quitarImagen.setVisible(false);
        btn_quitarImagen.setManaged(false);
    }

    private void mostrarPreview(Path ruta) {
        if (!Files.exists(ruta)) return;
        imgPreview.setImage(new Image(ruta.toUri().toString(), 118, 118, true, true));
        lbl_nombreImagen.setText(ruta.getFileName().toString());
        btn_quitarImagen.setVisible(true);
        btn_quitarImagen.setManaged(true);
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
            obj.setImagen(imagenNombre);
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
        ((Stage) txt_codigo.getScene().getWindow()).close();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
