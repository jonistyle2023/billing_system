/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package upse.calculacion.controlador;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author COREI9
 */
public class LoginController implements Initializable {

    @FXML
    private TextField txt_usuario;

    @FXML
    private PasswordField txt_password;

    @FXML
    private void acc_validar(ActionEvent event) {
        String usuario = txt_usuario.getText() == null ? "" : txt_usuario.getText().trim();
        String clave = txt_password.getText() == null ? "" : txt_password.getText();

        if (fun_validar(usuario, clave)) {
            try {
                App.setRoot("Principal");
            } catch (IOException ex) {
                mostrarError("No se pudo cargar la vista principal.");
            }
            return;
        }

        txt_password.clear();
        mostrarError("Usuario o clave incorrectos.");
    }

    @FXML
    private void acc_cerrar(ActionEvent event) {
        Platform.exit();
    }
    
    public boolean fun_validar(String usuario,String clave) {
        return "admin".equals(usuario) && "123".equals(clave);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txt_usuario.requestFocus();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Acceso denegado");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

}
