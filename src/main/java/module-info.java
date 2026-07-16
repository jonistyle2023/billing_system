module upse.calculacion {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.sql;
    requires java.desktop;
    requires java.xml;
    requires java.xml.crypto;
    requires java.net.http;
    requires jasperreports;

    opens upse.calculacion.controlador to javafx.fxml;
    exports upse.calculacion.controlador;
    exports upse.calculacion.modelo;
}