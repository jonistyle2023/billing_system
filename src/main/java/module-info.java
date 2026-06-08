module upse.calculacion {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.sql;
    requires java.desktop;

    opens upse.calculacion.controlador to javafx.fxml;
    exports upse.calculacion.controlador;
    exports upse.calculacion.modelo;
}