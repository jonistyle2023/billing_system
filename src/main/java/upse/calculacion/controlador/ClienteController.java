package upse.calculacion.controlador;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import upse.calculacion.Mad.Mad_cliente;
import upse.calculacion.modelo.Cliente;

public class ClienteController implements Initializable {

    @FXML
    private Button btn_cerrar;
    @FXML
    private Button btn_grabar;
    @FXML
    private TextField txt_cedula;
    @FXML
    private TextField txt_nombres;
    @FXML
    private TextArea txt_direccion;
    @FXML
    private TextField txt_telefono;
    @FXML
    private TextField txt_correo;
    @FXML
    private CheckBox chk_validar;
    private final Mad_cliente madCliente = new Mad_cliente();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    /** Precarga el formulario para modificar un cliente existente. Con null, queda en blanco para uno nuevo. */
    public void setCliente(Cliente cliente) {
        if (cliente == null) {
            return;
        }
        txt_cedula.setText(cliente.getCedula());
        txt_cedula.setEditable(false); // la cedula es la clave primaria, no se debe cambiar al editar
        txt_nombres.setText(cliente.getNombres());
        txt_direccion.setText(cliente.getDireccion());
        txt_telefono.setText(cliente.getTelefono());
        txt_correo.setText(cliente.getCorreo());
    }

    @FXML
    private void acc_cerrar(ActionEvent event) {
        this.cerrarFormulario();
    }

    private void cerrarFormulario(){
        try {
            Stage stage = (Stage) this.btn_cerrar.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            fun_mensajeError(e.getMessage());
        }
    }

    @FXML
    private void acc_grabar(ActionEvent event) {
        try {
            Cliente obj = new Cliente(this.txt_cedula.getText(),
                    this.txt_nombres.getText(),
                    this.txt_direccion.getText(),
                    this.txt_telefono.getText(),
                    this.txt_correo.getText()
            );
            if (obj.getCedula() == null || obj.getCedula().trim().isEmpty()) {
                fun_mensajeError("La cédula no puede estar vacía.");
                return;
            }
            madCliente.guardarCliente(obj);
            this.cerrarFormulario();
        } catch (Exception e) {
             fun_mensajeError(e.getMessage());
        }
    }

    private void fun_mensajeError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
