package upse.calculacion.controlador;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import static upse.calculacion.general.Mod_general.DIRVISTAS;
import upse.calculacion.Mad.Mad_empresa;
import upse.calculacion.modelo.Empresa;

public class PrincipalController implements Initializable {

    @FXML
    private AnchorPane dataPane;

    @FXML
    private ComboBox<String> cmb_idioma;

    @FXML
    private Label lbl_usuario;

    @FXML
    private Label lbl_empresaNombre;

    @FXML
    private Label lbl_empresaDireccion;

    @FXML
    private Label lbl_empresaTelefono;

    @FXML
    private void acc_abrirProductos(ActionEvent event) {
        try {
            Node pantalla = cargarVista("Producto");
            setDataPane(pantalla);
        } catch (IOException ex) {
            mostrarError("No se pudo abrir el modulo de productos.");
        }
    }

    @FXML
    private void acc_abrirClientes(ActionEvent event) {
        try {
            Node pantalla = cargarVista("Clientes");
            setDataPane(pantalla);
        } catch (IOException ex) {
            mostrarError("No se pudo abrir el modulo de clientes.");
        }
    }
    
    @FXML
    private void acc_abrirFactura(ActionEvent event) {
        try {
            Node pantalla = cargarVista("Factura");
            setDataPane(pantalla);
        } catch (IOException ex) {
            ex.printStackTrace();
            mostrarError("No se pudo abrir el modulo de facturas.\nError: " + ex.getMessage());
        }
    }

    @FXML
    private void acc_abrirClienteModal(ActionEvent event) {
        abrirModal("Cliente", App.getBundle().getString("cliente.titulo"));
    }

    @FXML
    private void acc_cerrarSesion(ActionEvent event) {
        if (!App.confirmarSalida("¿Está seguro que desea cerrar sesion?")) {
            return;
        }

        try {
            App.setRoot("Login");
            Stage stage = App.getStage();
            if (stage != null) {
                stage.setMinWidth(620);
                stage.setMinHeight(440);
                stage.setWidth(640);
                stage.setHeight(460);
                stage.centerOnScreen();
            }
        } catch (IOException ex) {
            mostrarError("No se pudo volver al login.");
        }
    }

    @FXML
    private void acc_salir(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    private void acc_cambiarIdioma(ActionEvent event) {
        String seleccion = cmb_idioma.getSelectionModel().getSelectedItem();
        if (seleccion != null) {
            if (seleccion.equals("English")) {
                App.cambiarIdioma(new Locale("en"));
            } else {
                App.cambiarIdioma(new Locale("es"));
            }
            try {
                // Recargar la vista principal para aplicar el idioma
                App.setRoot("Principal");
            } catch (IOException e) {
                mostrarError("No se pudo cambiar el idioma.");
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (cmb_idioma != null) {
            cmb_idioma.getItems().addAll("Español", "English");
            if (App.getBundle().getLocale().getLanguage().equals("en")) {
                cmb_idioma.getSelectionModel().select("English");
            } else {
                cmb_idioma.getSelectionModel().select("Español");
            }
        }

        if (lbl_usuario != null && App.getUsuarioActual() != null) {
            lbl_usuario.setText(App.getUsuarioActual().getNombres());
        }

        cargarEmpresa();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    public void setDataPane(Node node) {
        dataPane.getChildren().setAll(node);

        AnchorPane.setTopAnchor(node, 0.0);
        AnchorPane.setRightAnchor(node, 0.0);
        AnchorPane.setBottomAnchor(node, 0.0);
        AnchorPane.setLeftAnchor(node, 0.0);

        if (node instanceof Region) {
            ((Region) node).setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }
    }

    private Node cargarVista(String nombreFxml) throws IOException {
        return App.loadFXML(nombreFxml);
    }

    private void cargarEmpresa() {
        try {
            Empresa empresa = new Mad_empresa().obtenerEmpresaActiva();
            if (empresa == null) {
                if (lbl_empresaNombre != null) lbl_empresaNombre.setText("Sin datos de empresa");
                if (lbl_empresaDireccion != null) lbl_empresaDireccion.setText("");
                if (lbl_empresaTelefono != null) lbl_empresaTelefono.setText("");
                return;
            }
            if (lbl_empresaNombre != null) lbl_empresaNombre.setText(empresa.getNombre());
            if (lbl_empresaDireccion != null) lbl_empresaDireccion.setText(empresa.getDireccion());
            if (lbl_empresaTelefono != null) lbl_empresaTelefono.setText(empresa.getTelefono());
        } catch (Exception ex) {
            if (lbl_empresaNombre != null) lbl_empresaNombre.setText("No se pudo cargar la empresa");
            if (lbl_empresaDireccion != null) lbl_empresaDireccion.setText(ex.getMessage());
            if (lbl_empresaTelefono != null) lbl_empresaTelefono.setText("");
        }
    }
    
    private void abrirModal(String nombreFxml, String titulo) {
        try {
            Parent root = App.loadFXML(nombreFxml);
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.initModality(Modality.WINDOW_MODAL);
            if (dataPane != null && dataPane.getScene() != null) {
                stage.initOwner(dataPane.getScene().getWindow());
            }
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException ex) {
            mostrarError("No se pudo abrir la ventana.");
        }
    }
}
