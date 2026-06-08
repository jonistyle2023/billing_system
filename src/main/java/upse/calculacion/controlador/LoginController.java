/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package upse.calculacion.controlador;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import upse.calculacion.Mad.Mad_seguridad;
import upse.calculacion.modelo.Usuario;

/**
 * FXML Controller class
 *
 * @author COREI9
 */
public class LoginController implements Initializable {

    private final Mad_seguridad seguridad = new Mad_seguridad();

    @FXML
    private TextField txt_usuario;

    @FXML
    private PasswordField txt_password;

    @FXML
    private void acc_validar(ActionEvent event) {
        String usuario = txt_usuario.getText() == null ? "" : txt_usuario.getText().trim();
        String clave = txt_password.getText() == null ? "" : txt_password.getText();

        try {
            Usuario usuarioAutenticado = fun_validar(usuario, clave);
            if (usuarioAutenticado != null) {
                App.setUsuarioActual(usuarioAutenticado);
                App.setRoot("Principal");
                return;
            }

            txt_password.clear();
            mostrarError(App.getBundle().getString("login.error.credenciales"));
        } catch (SQLException | IllegalStateException ex) {
            mostrarError(App.getBundle().getString("login.error.bd"));
        } catch (IOException ex) {
            mostrarError(App.getBundle().getString("login.error.principal"));
        }
    }

    @FXML
    private void acc_cerrar(ActionEvent event) {
        Platform.exit();
    }
    
    public Usuario fun_validar(String usuario,String clave) throws SQLException {
        if (usuario.isBlank() || clave.isBlank()) {
            return null;
        }
        return seguridad.login(usuario, clave);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txt_usuario.requestFocus();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(App.getBundle().getString("login.error.titulo"));
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

}
