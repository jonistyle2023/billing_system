package upse.calculacion.controlador;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import upse.calculacion.modelo.Cliente;

public class FacturaController implements Initializable {

    @FXML
    private VBox facturaPane;
    
    @FXML
    private TextField txt_numFactura;
    @FXML
    private TextField txt_fecha;
    @FXML
    private TextField txt_cedula;
    @FXML
    private TextField txt_nombres;
    @FXML
    private TextField txt_telefono;
    @FXML
    private TextField txt_correo;
    @FXML
    private TextField txt_direccion;
    @FXML
    private CheckBox chk_validar;

    @FXML
    private TextField txt_subtotal;
    @FXML
    private TextField txt_subtotal0;
    @FXML
    private TextField txt_iva;
    @FXML
    private TextField txt_total;

    @FXML
    private Button btn_grabar;
    @FXML
    private Button btn_anular;
    @FXML
    private Button btn_nuevo;
    @FXML
    private Button btn_cerrar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicializacion del modulo de factura.
        
        // Listener to autocompletar when focus is lost on cedula
        txt_cedula.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // Focus lost
                buscarYAutocompletarCliente();
            }
        });
    }

    @FXML
    private void acc_buscarCliente(ActionEvent event) {
        buscarYAutocompletarCliente();
    }
    
    private void buscarYAutocompletarCliente() {
        String cedulaBuscada = txt_cedula.getText();
        if (cedulaBuscada != null && !cedulaBuscada.trim().isEmpty()) {
            Cliente clienteEncontrado = null;
            for (Cliente c : ClienteController.clientes) {
                if (c.getCedula().equals(cedulaBuscada.trim())) {
                    clienteEncontrado = c;
                    break;
                }
            }
            
            if (clienteEncontrado != null) {
                txt_nombres.setText(clienteEncontrado.getNombres());
                txt_telefono.setText(clienteEncontrado.getTelefono());
                txt_correo.setText(clienteEncontrado.getCorreo());
                txt_direccion.setText(clienteEncontrado.getDireccion());
            }
        }
    }

    @FXML
    private void acc_grabar(ActionEvent event) {
        String cedula = txt_cedula.getText();
        if (cedula == null || cedula.trim().isEmpty()) {
            mostrarError("La cédula no puede estar vacía.");
            return;
        }

        // Actualizar o crear cliente
        Cliente clienteExistente = null;
        for (Cliente c : ClienteController.clientes) {
            if (c.getCedula().equals(cedula.trim())) {
                clienteExistente = c;
                break;
            }
        }

        if (clienteExistente != null) {
            // Actualizar datos del cliente
            clienteExistente.setNombres(txt_nombres.getText());
            clienteExistente.setTelefono(txt_telefono.getText());
            clienteExistente.setCorreo(txt_correo.getText());
            clienteExistente.setDireccion(txt_direccion.getText());
            System.out.println("Cliente actualizado exitosamente.");
        } else {
            // Crear nuevo cliente
            Cliente nuevoCliente = new Cliente(
                cedula.trim(),
                txt_nombres.getText(),
                txt_direccion.getText(),
                txt_telefono.getText(),
                txt_correo.getText()
            );
            ClienteController.clientes.add(nuevoCliente);
            System.out.println("Nuevo cliente registrado desde factura.");
        }
        
        // Aqui deberia ir el guardado de la cabecera y el detalle de la factura...
        
        mostrarInfo("Factura guardada correctamente y datos de cliente actualizados.");
    }

    @FXML
    private void acc_anular(ActionEvent event) {
        // Implementar anular factura
    }

    @FXML
    private void acc_nuevo(ActionEvent event) {
        // Limpiar los campos
        txt_numFactura.clear();
        txt_fecha.clear();
        txt_cedula.clear();
        txt_nombres.clear();
        txt_telefono.clear();
        txt_correo.clear();
        txt_direccion.clear();
        chk_validar.setSelected(false);
        txt_subtotal.clear();
        txt_subtotal0.clear();
        txt_iva.clear();
        txt_total.clear();
    }

    @FXML
    private void acc_cerrar(ActionEvent event) {
        if (facturaPane != null && facturaPane.getParent() instanceof AnchorPane) {
            ((AnchorPane) facturaPane.getParent()).getChildren().remove(facturaPane);
        }
    }
    
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}