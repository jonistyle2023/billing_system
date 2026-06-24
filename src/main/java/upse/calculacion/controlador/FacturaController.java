package upse.calculacion.controlador;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.FloatStringConverter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import upse.calculacion.Mad.Mad_cliente;
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
    private javafx.scene.control.TextArea txt_direccion;
    @FXML
    private CheckBox chk_validar;

    @FXML
    private TableView<upse.calculacion.modelo.DetFactura> tblDetalles;
    @FXML
    private TableColumn<upse.calculacion.modelo.DetFactura, String> colCodigo;
    @FXML
    private TableColumn<upse.calculacion.modelo.DetFactura, String> colDescripcion;
    @FXML
    private TableColumn<upse.calculacion.modelo.DetFactura, Float> colCant;
    @FXML
    private TableColumn<upse.calculacion.modelo.DetFactura, Float> colVUnit;
    @FXML
    private TableColumn<upse.calculacion.modelo.DetFactura, Float> colSubtotal;
    @FXML
    private TableColumn<upse.calculacion.modelo.DetFactura, Boolean> colAplicaIva;
    @FXML
    private TableColumn<upse.calculacion.modelo.DetFactura, Float> colTotal;

    private ObservableList<upse.calculacion.modelo.DetFactura> detalleList = FXCollections.observableArrayList();

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

    private static int secuenciaFactura = 0;
    private final Mad_cliente madCliente = new Mad_cliente();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txt_cedula.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                buscarYAutocompletarCliente();
            }
        });

        configurarNuevaFactura();
        configurarTablaDetalles();
    }

    private void configurarTablaDetalles() {
        // Set cell value factories
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("prod_cod"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("prod_nombre"));
        colCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colVUnit.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalConIva"));

        // Make editable
        tblDetalles.setEditable(true);
        colCodigo.setCellFactory(TextFieldTableCell.forTableColumn());
        colDescripcion.setCellFactory(TextFieldTableCell.forTableColumn());
        colCant.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
        colVUnit.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));

        // Checkbox for aplica IVA
        colAplicaIva.setCellFactory(tc -> new CheckBoxTableCell<upse.calculacion.modelo.DetFactura, Boolean>());

        // Handlers for edit commit
        colCant.setOnEditCommit(ev -> {
            upse.calculacion.modelo.DetFactura df = ev.getRowValue();
            df.setCantidad(ev.getNewValue() != null ? ev.getNewValue() : df.getCantidad());
            df.setTotal(df.getCantidad() * df.getPrecio());
            tblDetalles.refresh();
            actualizarTotales();
        });

        colVUnit.setOnEditCommit(ev -> {
            upse.calculacion.modelo.DetFactura df = ev.getRowValue();
            df.setPrecio(ev.getNewValue() != null ? ev.getNewValue() : df.getPrecio());
            df.setTotal(df.getCantidad() * df.getPrecio());
            tblDetalles.refresh();
            actualizarTotales();
        });

        // When checkbox toggled update model and totals
        colAplicaIva.setCellValueFactory(cell -> {
            SimpleBooleanProperty prop = new SimpleBooleanProperty(cell.getValue().isAplicaIva());
            prop.addListener((obs, oldV, newV) -> {
                cell.getValue().setAplicaIva(newV);
                actualizarTotales();
            });
            return prop;
        });

        tblDetalles.setItems(detalleList);
    }

    private void actualizarTotales() {
        float subtotal = 0f;
        float subtotal0 = 0f;
        for (upse.calculacion.modelo.DetFactura d : detalleList) {
            if (d.isAplicaIva()) subtotal += d.getTotal(); else subtotal0 += d.getTotal();
        }
        float iva = subtotal * 0.12f; // asumiendo 12% IVA
        float total = subtotal + subtotal0 + iva;
        txt_subtotal.setText(String.format("%.2f", subtotal));
        txt_subtotal0.setText(String.format("%.2f", subtotal0));
        txt_iva.setText(String.format("%.2f", iva));
        txt_total.setText(String.format("%.2f", total));
    }

    private void configurarNuevaFactura() {
        // Formato de fecha
        txt_fecha.setText(getFechaActualFormateada());
        txt_fecha.setEditable(false);

        // Formato de numero de factura
        secuenciaFactura++;
        txt_numFactura.setText(formatearNumeroFactura(secuenciaFactura));
        txt_numFactura.setEditable(false);
    }

    private String getFechaActualFormateada() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return LocalDate.now().format(formato);
    }

    private String formatearNumeroFactura(int numero) {
        return String.format("001-001-%09d", numero);
    }

    @FXML
    private void acc_buscarCliente(ActionEvent event) {
        buscarYAutocompletarCliente();
    }

    private void buscarYAutocompletarCliente() {
        String cedula = txt_cedula.getText();
        if (cedula == null || cedula.trim().isEmpty()) return;

        try {
            Cliente encontrado = madCliente.buscarPorCedula(cedula.trim());
            if (encontrado != null) {
                txt_nombres.setText(encontrado.getNombres());
                txt_telefono.setText(encontrado.getTelefono());
                txt_correo.setText(encontrado.getCorreo());
                txt_direccion.setText(encontrado.getDireccion());
            } else {
                txt_nombres.clear();
                txt_telefono.clear();
                txt_correo.clear();
                txt_direccion.clear();
            }
        } catch (Exception e) {
            mostrarError("No se pudo buscar el cliente: " + e.getMessage());
        }
    }

    @FXML
    private void acc_grabar(ActionEvent event) {
        try {
            String cedula = txt_cedula.getText();
            if (cedula == null || cedula.trim().isEmpty()) {
                mostrarError("La cédula no puede estar vacía.");
                return;
            }
            if (detalleList.isEmpty()) {
                mostrarError("Debe agregar al menos un producto a la factura.");
                return;
            }

            Cliente cliente = madCliente.buscarPorCedula(cedula.trim());
            if (cliente == null) {
                cliente = new Cliente();
                cliente.setCedula(cedula.trim());
            }
            cliente.setNombres(txt_nombres.getText());
            cliente.setTelefono(txt_telefono.getText());
            cliente.setCorreo(txt_correo.getText());
            cliente.setDireccion(txt_direccion.getText());
            madCliente.guardarCliente(cliente);

            // TODO: Persistir cabecera y detalle en cab_Factura / det_Factura
            mostrarInfo("Datos del cliente guardados.\n(Persistencia de factura en BD pendiente de implementación.)");
            acc_nuevo(null);
        } catch (Exception e) {
            mostrarError("No se pudo guardar: " + e.getMessage());
        }
    }

    @FXML
    private void acc_anular(ActionEvent event) {
        // Implementar anular factura
    }

    @FXML
    private void acc_nuevo(ActionEvent event) {
        txt_cedula.clear();
        txt_nombres.clear();
        txt_telefono.clear();
        txt_correo.clear();
        txt_direccion.clear();
        chk_validar.setSelected(false);
        detalleList.clear();
        actualizarTotales();
        configurarNuevaFactura();
    }

    @FXML
    private void acc_cerrar(ActionEvent event) {
        if (facturaPane != null && facturaPane.getParent() instanceof javafx.scene.layout.AnchorPane) {
            ((javafx.scene.layout.AnchorPane) facturaPane.getParent()).getChildren().remove(facturaPane);
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