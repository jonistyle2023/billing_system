module upse.calculacion {
    requires javafx.controls;
    requires javafx.fxml;

    opens upse.calculacion.controlador to javafx.fxml;
    exports upse.calculacion.controlador;
    exports upse.calculacion.modelo;
}