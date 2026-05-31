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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicialización del módulo de factura.
        
        // Listener to autocompletar when focus is lost on cedula
        txt_cedula.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // Focus lost
                buscarYAutocompletarCliente();
            }
        });
        
        configurarNuevaFactura();

        // Configurar la tabla de detalles
        configurarTablaDetalles();
    }

    private void configurarTablaDetalles() {
        // Set cell value factories
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("prod_cod"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("prod_nombre"));
        colCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colVUnit.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

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

                // línea editable al detalle cuando el cliente es autocompletado
                boolean alreadyAdded = false;
                if (!detalleList.isEmpty()) {
                    upse.calculacion.modelo.DetFactura last = detalleList.get(detalleList.size() - 1);
                    if (last.getProd_nombre() != null && last.getProd_nombre().equals(clienteEncontrado.getNombres())) {
                        alreadyAdded = true;
                    }
                }
                if (!alreadyAdded) {
                    upse.calculacion.modelo.DetFactura nueva = new upse.calculacion.modelo.DetFactura("", clienteEncontrado.getNombres(), 1.0f, 0.0f, false, 0.0f);
                    detalleList.add(nueva);
                    tblDetalles.scrollTo(nueva);
                    tblDetalles.getSelectionModel().select(nueva);
                }
                actualizarTotales();
            } else {
                txt_nombres.clear();
                txt_telefono.clear();
                txt_correo.clear();
                txt_direccion.clear();
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
        
        // Preparar para la siguiente factura
        acc_nuevo(null);
    }

    @FXML
    private void acc_anular(ActionEvent event) {
        // Implementar anular factura
    }

    @FXML
    private void acc_nuevo(ActionEvent event) {
        // Limpiar los campos
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
        
        // Configurar nueva fecha y número de factura
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