package upse.calculacion.controlador;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.Locale;
import javafx.util.converter.FloatStringConverter;
import upse.calculacion.Mad.Mad_cliente;
import upse.calculacion.Mad.Mad_factura;
import upse.calculacion.Mad.Mad_producto;
import upse.calculacion.general.Mod_VariablesGlobales;
import upse.calculacion.modelo.Cliente;
import upse.calculacion.modelo.DetFactura;
import upse.calculacion.modelo.Producto;
import static upse.calculacion.general.Mod_general.DIRVISTAS;

public class FacturacionController implements Initializable {

    @FXML private VBox facturaPane;
    @FXML private TextField txt_numFactura;
    @FXML private TextField txt_fecha;
    @FXML private TextField txt_cedula;
    @FXML private TextField txt_nombres;
    @FXML private TextField txt_telefono;
    @FXML private TextField txt_correo;
    @FXML private javafx.scene.control.TextArea txt_direccion;
    @FXML private CheckBox chk_validar;

    @FXML private TableView<DetFactura> tblDetalles;
    @FXML private TableColumn<DetFactura, String>  colCodigo;
    @FXML private TableColumn<DetFactura, String>  colDescripcion;
    @FXML private TableColumn<DetFactura, Float>   colCant;
    @FXML private TableColumn<DetFactura, Float>   colVUnit;
    @FXML private TableColumn<DetFactura, Float>   colSubtotal;
    @FXML private TableColumn<DetFactura, Boolean> colAplicaIva;
    @FXML private TableColumn<DetFactura, Float>   colTotal;
    @FXML private TableColumn<DetFactura, Void>    colBuscar;

    @FXML private TextField txt_subtotal;
    @FXML private TextField txt_subtotal0;
    @FXML private TextField txt_iva;
    @FXML private TextField txt_total;

    // Pago
    @FXML private ToggleGroup tgMetodoPago;
    @FXML private RadioButton rb_efectivo;
    @FXML private RadioButton rb_tarjeta;
    @FXML private RadioButton rb_transferencia;
    @FXML private TextField   txt_montoRecibido;
    @FXML private Label       lbl_cambio;

    @FXML private Button btn_grabar;
    @FXML private Button btn_anular;
    @FXML private Button btn_nuevo;
    @FXML private Button btn_eliminarLinea;

    private final ObservableList<DetFactura> detalleList = FXCollections.observableArrayList();
    private final Mad_cliente  madCliente  = new Mad_cliente();
    private final Mad_producto madProducto = new Mad_producto();
    private final Mad_factura  madFactura  = new Mad_factura();

    private String numFacturaEmitida = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarNuevaFactura();
        configurarTablaDetalles();
        configurarPago();
        agregarFilaVacia();

        txt_cedula.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) buscarYAutocompletarCliente();
        });
    }

    // ─── Pago ─────────────────────────────────────────────────────────────

    private void configurarPago() {
        tgMetodoPago.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            boolean esEfectivo = newT == rb_efectivo;
            txt_montoRecibido.setDisable(!esEfectivo);
            if (!esEfectivo) {
                txt_montoRecibido.clear();
                lbl_cambio.setText("0.00");
                lbl_cambio.setStyle("-fx-font-weight: bold;");
            } else {
                calcularCambio();
            }
        });

        txt_montoRecibido.textProperty().addListener((obs, oldV, newV) -> calcularCambio());
    }

    private void calcularCambio() {
        if (tgMetodoPago.getSelectedToggle() != rb_efectivo) return;
        String montoStr = txt_montoRecibido.getText().trim();
        if (montoStr.isEmpty()) {
            lbl_cambio.setText("0.00");
            lbl_cambio.setStyle("-fx-font-weight: bold;");
            return;
        }
        try {
            float total  = Float.parseFloat(txt_total.getText().trim());
            float monto  = Float.parseFloat(montoStr);
            float cambio = monto - total;
            lbl_cambio.setText(String.format(Locale.US, "%.2f", cambio));
            lbl_cambio.setStyle(cambio < 0
                    ? "-fx-font-weight: bold; -fx-text-fill: #e74c3c;"
                    : "-fx-font-weight: bold; -fx-text-fill: #27ae60;");
        } catch (NumberFormatException e) {
            lbl_cambio.setText("0.00");
            lbl_cambio.setStyle("-fx-font-weight: bold;");
        }
    }

    private String getMetodoPago() {
        Toggle sel = tgMetodoPago.getSelectedToggle();
        if (sel == rb_efectivo)      return "EFECTIVO";
        if (sel == rb_tarjeta)       return "TARJETA";
        if (sel == rb_transferencia) return "TRANSFERENCIA";
        return null;
    }

    private void resetearPago() {
        tgMetodoPago.selectToggle(null);
        txt_montoRecibido.clear();
        txt_montoRecibido.setDisable(true);
        lbl_cambio.setText("0.00");
        lbl_cambio.setStyle("-fx-font-weight: bold;");
    }

    // ─── Tabla de detalles ────────────────────────────────────────────────

    private void configurarTablaDetalles() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("prod_cod"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("prod_nombre"));
        colCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colVUnit.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalConIva"));
        colAplicaIva.setCellValueFactory(cell -> {
            SimpleBooleanProperty prop = new SimpleBooleanProperty(cell.getValue().isAplicaIva());
            prop.addListener((obs, oldV, newV) -> {
                cell.getValue().setAplicaIva(newV);
                actualizarTotales();
            });
            return prop;
        });
        colAplicaIva.setCellFactory(tc -> new CheckBoxTableCell<>());

        colBuscar.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Buscar");
            {
                btn.setOnAction(ev -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < detalleList.size()) {
                        abrirBuscarProducto(detalleList.get(idx), idx);
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tblDetalles.setEditable(true);
        colCodigo.setCellFactory(TextFieldTableCell.forTableColumn());
        colCant.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));

        colCodigo.setOnEditCommit(ev -> {
            DetFactura df = ev.getRowValue();
            String codigo = ev.getNewValue() == null ? "" : ev.getNewValue().trim().toUpperCase();
            df.setProd_cod(codigo);

            if (codigo.isEmpty()) {
                limpiarFila(df);
                tblDetalles.refresh();
                return;
            }

            try {
                Producto producto = madProducto.buscarPorCodigo(codigo);
                if (producto != null) {
                    df.setProd_id(producto.getId());
                    df.setProd_nombre(producto.getNombre());
                    df.setPrecio(producto.getPvpMenor());
                    df.setAplicaIva(producto.isAplicaIva());
                    df.setTotal(df.getCantidad() * df.getPrecio());
                    tblDetalles.refresh();
                    actualizarTotales();

                    if (detalleList.indexOf(df) == detalleList.size() - 1) {
                        agregarFilaVacia();
                    }

                    int idx = detalleList.indexOf(df);
                    Platform.runLater(() -> {
                        tblDetalles.requestFocus();
                        tblDetalles.getSelectionModel().select(idx);
                        tblDetalles.edit(idx, colCant);
                    });
                } else {
                    mostrarError("Producto '" + codigo + "' no encontrado en el catálogo.");
                    limpiarFila(df);
                    tblDetalles.refresh();
                }
            } catch (SQLException e) {
                mostrarError("Error al buscar el producto: " + e.getMessage());
                limpiarFila(df);
                tblDetalles.refresh();
            }
        });

        colCant.setOnEditCommit(ev -> {
            DetFactura df = ev.getRowValue();
            float cant = ev.getNewValue() != null && ev.getNewValue() > 0 ? ev.getNewValue() : 1f;
            df.setCantidad(cant);
            df.setTotal(df.getCantidad() * df.getPrecio());
            tblDetalles.refresh();
            actualizarTotales();

            int nextIdx = detalleList.indexOf(df) + 1;
            if (nextIdx < detalleList.size()) {
                Platform.runLater(() -> {
                    tblDetalles.requestFocus();
                    tblDetalles.getSelectionModel().select(nextIdx);
                    tblDetalles.edit(nextIdx, colCodigo);
                });
            }
        });

        tblDetalles.setItems(detalleList);
    }

    private void abrirBuscarProducto(DetFactura fila, int rowIndex) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    App.class.getResource(DIRVISTAS + "BuscarProducto.fxml"),
                    App.getBundle());
            Parent root = loader.load();
            BuscarProductoController ctrl = loader.getController();

            Stage modal = new Stage();
            modal.initModality(Modality.WINDOW_MODAL);
            modal.initOwner(facturaPane.getScene().getWindow());
            modal.setTitle("Buscar Producto");
            modal.setScene(new Scene(root));
            modal.showAndWait();

            Producto seleccionado = ctrl.getProductoSeleccionado();
            if (seleccionado != null) {
                fila.setProd_id(seleccionado.getId());
                fila.setProd_cod(seleccionado.getCodigo());
                fila.setProd_nombre(seleccionado.getNombre());
                fila.setPrecio(seleccionado.getPvpMenor());
                fila.setAplicaIva(seleccionado.isAplicaIva());
                fila.setTotal(fila.getCantidad() * fila.getPrecio());
                tblDetalles.refresh();
                actualizarTotales();

                if (rowIndex == detalleList.size() - 1) {
                    agregarFilaVacia();
                }
            }
        } catch (IOException e) {
            mostrarError("No se pudo abrir el buscador de productos: " + e.getMessage());
        }
    }

    private void limpiarFila(DetFactura df) {
        df.setProd_id(0);
        df.setProd_cod("");
        df.setProd_nombre("");
        df.setPrecio(0f);
        df.setAplicaIva(false);
        df.setTotal(0f);
    }

    private void agregarFilaVacia() {
        detalleList.add(new DetFactura("", "", 1f, 0f, false, 0f));
    }

    // ─── Totales ──────────────────────────────────────────────────────────

    private void actualizarTotales() {
        float subtotal = 0f;
        float subtotal0 = 0f;
        for (DetFactura d : detalleList) {
            if (d.getProd_cod() == null || d.getProd_cod().isEmpty()) continue;
            if (d.isAplicaIva()) subtotal += d.getTotal();
            else subtotal0 += d.getTotal();
        }
        float iva   = subtotal * Mod_VariablesGlobales.getTasaIva();
        float total = subtotal + subtotal0 + iva;
        txt_subtotal.setText(String.format(Locale.US, "%.2f", subtotal));
        txt_subtotal0.setText(String.format(Locale.US, "%.2f", subtotal0));
        txt_iva.setText(String.format(Locale.US, "%.2f", iva));
        txt_total.setText(String.format(Locale.US, "%.2f", total));
        calcularCambio();
    }

    // ─── Cliente ──────────────────────────────────────────────────────────

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
                Platform.runLater(() -> {
                    tblDetalles.requestFocus();
                    tblDetalles.getSelectionModel().select(0);
                    tblDetalles.edit(0, colCodigo);
                });
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

    // ─── Acciones de tabla ────────────────────────────────────────────────

    @FXML
    private void acc_eliminarLinea(ActionEvent event) {
        DetFactura seleccionada = tblDetalles.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Seleccione una línea para eliminar.");
            return;
        }
        if (detalleList.size() == 1) {
            limpiarFila(seleccionada);
            tblDetalles.refresh();
            actualizarTotales();
            return;
        }
        detalleList.remove(seleccionada);
        DetFactura ultima = detalleList.get(detalleList.size() - 1);
        if (ultima.getProd_cod() != null && !ultima.getProd_cod().isEmpty()) {
            agregarFilaVacia();
        }
        actualizarTotales();
    }

    // ─── Factura ──────────────────────────────────────────────────────────

    private void configurarNuevaFactura() {
        txt_fecha.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txt_fecha.setEditable(false);
        txt_numFactura.setText("PENDIENTE");
        txt_numFactura.setEditable(false);
        numFacturaEmitida = null;
        actualizarEstadoBotones();
    }

    private void actualizarEstadoBotones() {
        boolean emitida = numFacturaEmitida != null;
        btn_grabar.setDisable(emitida);
        btn_anular.setDisable(!emitida);
    }

    private boolean hayDatosSinEmitir() {
        if (numFacturaEmitida != null) return false;
        boolean tieneCedula    = txt_cedula.getText() != null && !txt_cedula.getText().trim().isEmpty();
        boolean tieneProductos = detalleList.stream()
                .anyMatch(d -> d.getProd_cod() != null && !d.getProd_cod().isEmpty());
        return tieneCedula || tieneProductos;
    }

    @FXML
    private void acc_grabar(ActionEvent event) {
        // ── Validar cliente ───────────────────────────────────────────────
        String cedula = txt_cedula.getText() == null ? "" : txt_cedula.getText().trim();
        if (cedula.isEmpty()) {
            mostrarError("La cédula del cliente no puede estar vacía.");
            return;
        }

        // ── Validar productos ─────────────────────────────────────────────
        List<DetFactura> lineasValidas = detalleList.stream()
                .filter(d -> d.getProd_cod() != null && !d.getProd_cod().isEmpty())
                .collect(Collectors.toList());
        if (lineasValidas.isEmpty()) {
            mostrarError("Debe agregar al menos un producto a la factura.");
            return;
        }

        // ── Validar método de pago ────────────────────────────────────────
        String metodo = getMetodoPago();
        if (metodo == null) {
            mostrarError("Seleccione un método de pago antes de emitir la factura.");
            return;
        }

        float totalFactura;
        try {
            totalFactura = Float.parseFloat(txt_total.getText().trim());
        } catch (NumberFormatException e) {
            mostrarError("Error al leer el total de la factura.");
            return;
        }

        float pagoMonto;
        float pagoCambio;

        if ("EFECTIVO".equals(metodo)) {
            String montoStr = txt_montoRecibido.getText().trim();
            if (montoStr.isEmpty()) {
                mostrarError("Ingrese el monto recibido en efectivo.");
                txt_montoRecibido.requestFocus();
                return;
            }
            try {
                pagoMonto = Float.parseFloat(montoStr);
            } catch (NumberFormatException e) {
                mostrarError("El monto recibido no es un número válido.");
                txt_montoRecibido.requestFocus();
                return;
            }
            if (pagoMonto < totalFactura) {
                mostrarError(String.format(Locale.US,
                        "El monto recibido ($ %.2f) es menor al total de la factura ($ %.2f).",
                        pagoMonto, totalFactura));
                txt_montoRecibido.requestFocus();
                return;
            }
            pagoCambio = pagoMonto - totalFactura;
        } else {
            pagoMonto  = totalFactura;
            pagoCambio = 0f;
        }

        try {
            // ── Validar stock ─────────────────────────────────────────────
            List<String> sinStock = new ArrayList<>();
            for (DetFactura det : lineasValidas) {
                Producto p = madProducto.buscarPorCodigo(det.getProd_cod());
                if (p == null) {
                    sinStock.add("  • " + det.getProd_cod() + " — producto no encontrado en catálogo.");
                } else if (p.getStock() < det.getCantidad()) {
                    sinStock.add(String.format("  • %s  (disponible: %.0f, solicitado: %.0f)",
                            det.getProd_nombre(), p.getStock(), det.getCantidad()));
                }
            }
            if (!sinStock.isEmpty()) {
                mostrarError("No se puede emitir la factura. Stock insuficiente:\n\n"
                        + String.join("\n", sinStock));
                return;
            }

            // ── Gestionar cliente ─────────────────────────────────────────
            Cliente clienteEnBD = madCliente.buscarPorCedula(cedula);
            boolean esNuevo = clienteEnBD == null;

            if (esNuevo) {
                String nombres = txt_nombres.getText() == null ? "" : txt_nombres.getText().trim();
                if (nombres.isEmpty()) {
                    mostrarError("El cliente con cédula \"" + cedula + "\" no está registrado.\n"
                            + "Complete el campo Nombres para poder registrarlo.");
                    txt_nombres.requestFocus();
                    return;
                }
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Nuevo Cliente");
                confirm.setHeaderText(null);
                confirm.setContentText(
                        "El cliente con cédula \"" + cedula + "\" no está registrado.\n"
                        + "Se creará un nuevo registro con los datos ingresados.\n\n¿Desea continuar?");
                Optional<ButtonType> respuesta = confirm.showAndWait();
                if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) return;

                Cliente nuevo = new Cliente();
                nuevo.setCedula(cedula);
                nuevo.setNombres(nombres);
                nuevo.setTelefono(txt_telefono.getText());
                nuevo.setCorreo(txt_correo.getText());
                nuevo.setDireccion(txt_direccion.getText());
                madCliente.guardarCliente(nuevo);

            } else if (chk_validar.isSelected()) {
                String nombres = txt_nombres.getText() == null ? "" : txt_nombres.getText().trim();
                if (nombres.isEmpty()) {
                    mostrarError("El campo Nombres no puede quedar vacío al actualizar el cliente.");
                    txt_nombres.requestFocus();
                    return;
                }
                clienteEnBD.setNombres(nombres);
                clienteEnBD.setTelefono(txt_telefono.getText());
                clienteEnBD.setCorreo(txt_correo.getText());
                clienteEnBD.setDireccion(txt_direccion.getText());
                madCliente.guardarCliente(clienteEnBD);
            }

            // ── Calcular totales ──────────────────────────────────────────
            float subtotal = 0f, baseCero = 0f;
            for (DetFactura d : lineasValidas) {
                if (d.isAplicaIva()) subtotal += d.getTotal();
                else baseCero += d.getTotal();
            }
            float iva   = subtotal * Mod_VariablesGlobales.getTasaIva();
            float total = subtotal + baseCero + iva;

            // ── Emitir ────────────────────────────────────────────────────
            int usrId = App.getUsuarioActual() != null ? App.getUsuarioActual().getId() : 0;
            String numFac = madFactura.emitirFactura(
                    cedula, LocalDate.now(),
                    subtotal, baseCero, iva, total,
                    usrId, lineasValidas,
                    metodo, pagoMonto, pagoCambio);

            numFacturaEmitida = numFac;
            txt_numFactura.setText(numFac);
            actualizarEstadoBotones();

            String infoCliente = esNuevo
                    ? "Cliente registrado. "
                    : (chk_validar.isSelected() ? "Datos del cliente actualizados. " : "");

            String infoPago = "EFECTIVO".equals(metodo)
                    ? String.format(Locale.US, "\nPago: Efectivo  |  Recibido: $ %.2f  |  Cambio: $ %.2f", pagoMonto, pagoCambio)
                    : "\nPago: " + metodo;

            mostrarInfo(infoCliente + "Factura emitida correctamente.\nNro.: " + numFac + infoPago);

        } catch (Exception e) {
            mostrarError("No se pudo emitir la factura: " + e.getMessage());
        }
    }

    @FXML
    private void acc_anular(ActionEvent event) {
        if (numFacturaEmitida == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Anular Factura");
        confirm.setHeaderText(null);
        confirm.setContentText(
                "¿Está seguro de anular la factura " + numFacturaEmitida + "?\n"
                + "Esta acción registra la anulación y no se puede deshacer.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            if (madFactura.anularFactura(numFacturaEmitida)) {
                mostrarInfo("Factura " + numFacturaEmitida + " anulada correctamente.");
                limpiarFormulario();
            } else {
                mostrarError("No se pudo anular la factura. Es posible que ya esté anulada.");
            }
        } catch (SQLException e) {
            mostrarError("Error al anular la factura: " + e.getMessage());
        }
    }

    @FXML
    private void acc_nuevo(ActionEvent event) {
        if (hayDatosSinEmitir()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Nueva Factura");
            confirm.setHeaderText(null);
            confirm.setContentText("Hay datos sin emitir. ¿Descartar y comenzar una nueva factura?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) return;
        }
        limpiarFormulario();
    }

    private void limpiarFormulario() {
        txt_cedula.clear();
        txt_nombres.clear();
        txt_telefono.clear();
        txt_correo.clear();
        txt_direccion.clear();
        chk_validar.setSelected(false);
        detalleList.clear();
        agregarFilaVacia();
        actualizarTotales();
        resetearPago();
        configurarNuevaFactura();
        txt_cedula.requestFocus();
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
