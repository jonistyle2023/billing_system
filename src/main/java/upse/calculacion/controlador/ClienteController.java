package upse.calculacion.controlador;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
    private int bandera = 0;
    private final Mad_cliente madCliente = new Mad_cliente();

    private static List<Cliente> clientes = new ArrayList<>();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            cargarClientesDesdeBD();
        } catch (Exception e) {
            fun_mensajeError("No se pudieron cargar los clientes desde la base de datos: " + e.getMessage());
        }
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
            cargarClientesDesdeBD();
            this.cerrarFormulario();
        } catch (Exception e) {
             fun_mensajeError(e.getMessage());
        }
    }
    
    public void recuperarCliente(String id){
        if(id == null || id.isEmpty()){
            bandera=0;//nuevo "Insert"
        }else{
            bandera=1;//modificar "update"
        }
      
        if(bandera==0){
            //limpiar la pantalla
            limpiarPantalla();
        }else{
            //recuperar el cliente
            recuperarcliente(id);
        }
    }

    private void cargarClientesDesdeBD() throws Exception {
        clientes = madCliente.listarClientes();
    }

    private void limpiarPantalla() {
        this.txt_cedula.setText("");
        this.txt_nombres.setText("");
        this.txt_direccion.setText("");
        this.txt_telefono.setText("");
        this.txt_correo.setText("");
    }
    
    private void recuperarcliente(String id){
        if (clientes.isEmpty()) {
            try {
                cargarClientesDesdeBD();
            } catch (Exception e) {
                fun_mensajeError("No se pudieron cargar los clientes desde la base de datos: " + e.getMessage());
                return;
            }
        }
        Cliente objCliente = fun_retornaCliente(id);
        if (objCliente!=null){
            //cargar los datos
            this.txt_cedula.setText(objCliente.getCedula());
            this.txt_nombres.setText(objCliente.getNombres());
            this.txt_direccion.setText(objCliente.getDireccion());
            this.txt_telefono.setText(objCliente.getTelefono());
            this.txt_correo.setText(objCliente.getCorreo());
        }
    }

    private Cliente fun_retornaCliente(String id){
        for (Cliente objCliente : clientes) {
            if(objCliente.getCedula().equals(id)){
                return objCliente;
            }       
        }
        return null;
    }

    private void fun_mensajeError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}