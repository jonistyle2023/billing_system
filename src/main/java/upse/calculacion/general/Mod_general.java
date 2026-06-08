/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package upse.calculacion.general;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;

/**
 *
 * @author COREI9
 */
public class Mod_general {
    public static final String DIRVISTAS="/upse/calculacion/vistas/";
    public static int gestorBD = 2;
    public static String str_nombreBD = "";

    public static void fun_mensajeInformacion(String mensaje){
        Alert alertInfo=new Alert(Alert.AlertType.INFORMATION);
        alertInfo.setTitle("Mensaje del Sistema");
        alertInfo.setContentText(mensaje);
        alertInfo.showAndWait();
    }
    public static void fun_mensajeError(String mensaje){
        Alert alertInfo=new Alert(Alert.AlertType.ERROR);
        alertInfo.setTitle("Mensaje del Sistema");
        alertInfo.setContentText(mensaje);
        alertInfo.showAndWait();
    }


    public static void detectarTecla(Node nodo, KeyCode tecla, Node nodoAFoco) {
        nodo.setOnKeyPressed(event -> {
            if (event.getCode() == tecla) {
                // System.out.println(tecla + " detectado en " + nodo.getId());
                // Acción al presionar la tecla
                if (nodoAFoco != null) {
                    nodoAFoco.requestFocus();
                }
                // Si es Tab, normalmente consumimos el evento para controlar el foco manualmente
                if (tecla == KeyCode.TAB) {
                    event.consume();
                }
            }
        });
    }

}
