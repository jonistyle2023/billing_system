package upse.calculacion.controlador;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import upse.calculacion.Mad.Mad_cliente;
import upse.calculacion.Mad.Mad_factura;
import upse.calculacion.Mad.Mad_producto;
import upse.calculacion.Mad.Mad_reporte;
import upse.calculacion.general.Mod_jasperReporte;
import upse.calculacion.modelo.Cliente;
import upse.calculacion.modelo.FacturaReporte;
import upse.calculacion.modelo.Producto;
import upse.calculacion.modelo.ProductoRanking;
import upse.calculacion.modelo.ResultadoEmisionFactura;
import upse.calculacion.modelo.ResumenFila;
import upse.calculacion.modelo.ResumenVentas;

public class ReportesController implements Initializable {

    private static final String TIPO_FACTURAS  = "Listado de facturas";
    private static final String TIPO_RESUMEN    = "Resumen de ventas";
    private static final String TIPO_RANKING    = "Ranking de productos";
    private static final String TIPO_CLIENTES   = "Listado de clientes";
    private static final String TIPO_PRODUCTOS  = "Listado de productos";

    private static final String ESTADO_TODAS    = "Todas";
    private static final String ESTADO_ACTIVAS  = "Activas";
    private static final String ESTADO_ANULADAS = "Anuladas";

    private static final int TOP_PRODUCTOS = 20;

    @FXML private ComboBox<String> cmb_tipoReporte;
    @FXML private DatePicker dp_desde;
    @FXML private DatePicker dp_hasta;
    @FXML private Label lbl_estado;
    @FXML private ComboBox<String> cmb_estado;

    @FXML private TableView<FacturaReporte> tblFacturas;
    @FXML private TableColumn<FacturaReporte, String> colNumero;
    @FXML private TableColumn<FacturaReporte, String> colFecha;
    @FXML private TableColumn<FacturaReporte, String> colCliente;
    @FXML private TableColumn<FacturaReporte, String> colSubtotal;
    @FXML private TableColumn<FacturaReporte, String> colSubtotalCero;
    @FXML private TableColumn<FacturaReporte, String> colIva;
    @FXML private TableColumn<FacturaReporte, String> colTotal;
    @FXML private TableColumn<FacturaReporte, String> colEstado;
    @FXML private TableColumn<FacturaReporte, String> colMetodoPago;
    @FXML private TableColumn<FacturaReporte, String> colEstadoSri;
    @FXML private Button btn_reintentarSri;

    @FXML private TableView<ResumenFila> tblResumen;
    @FXML private TableColumn<ResumenFila, String> colConcepto;
    @FXML private TableColumn<ResumenFila, String> colValor;

    @FXML private TableView<ProductoRanking> tblRanking;
    @FXML private TableColumn<ProductoRanking, String> colRankCodigo;
    @FXML private TableColumn<ProductoRanking, String> colRankNombre;
    @FXML private TableColumn<ProductoRanking, String> colRankCantidad;
    @FXML private TableColumn<ProductoRanking, String> colRankMonto;

    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente, String> colCliCedula;
    @FXML private TableColumn<Cliente, String> colCliNombres;
    @FXML private TableColumn<Cliente, String> colCliTelefono;
    @FXML private TableColumn<Cliente, String> colCliCorreo;
    @FXML private TableColumn<Cliente, String> colCliDireccion;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colProdCodigo;
    @FXML private TableColumn<Producto, String> colProdNombre;
    @FXML private TableColumn<Producto, Number> colProdPrecioCompra;
    @FXML private TableColumn<Producto, Number> colProdPvpMenor;
    @FXML private TableColumn<Producto, Number> colProdPvpMayor;
    @FXML private TableColumn<Producto, Number> colProdStock;
    @FXML private TableColumn<Producto, Boolean> colProdIva;

    private final Mad_reporte madReporte = new Mad_reporte();
    private final Mad_cliente madCliente = new Mad_cliente();
    private final Mad_producto madProducto = new Mad_producto();
    private final Mad_factura madFactura = new Mad_factura();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colSubtotalCero.setCellValueFactory(new PropertyValueFactory<>("subtotalCero"));
        colIva.setCellValueFactory(new PropertyValueFactory<>("iva"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colMetodoPago.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));
        colEstadoSri.setCellValueFactory(new PropertyValueFactory<>("estadoSri"));

        colConcepto.setCellValueFactory(new PropertyValueFactory<>("concepto"));
        colValor.setCellValueFactory(new PropertyValueFactory<>("valor"));

        colRankCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colRankNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colRankCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colRankMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));

        colCliCedula.setCellValueFactory(new PropertyValueFactory<>("cedula"));
        colCliNombres.setCellValueFactory(new PropertyValueFactory<>("nombres"));
        colCliTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colCliCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colCliDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));

        colProdCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colProdNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colProdPrecioCompra.setCellValueFactory(new PropertyValueFactory<>("precioCompra"));
        colProdPvpMenor.setCellValueFactory(new PropertyValueFactory<>("pvpMenor"));
        colProdPvpMayor.setCellValueFactory(new PropertyValueFactory<>("pvpMayor"));
        colProdStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colProdIva.setCellValueFactory(new PropertyValueFactory<>("aplicaIva"));

        cmb_tipoReporte.setItems(FXCollections.observableArrayList(
                TIPO_FACTURAS, TIPO_RESUMEN, TIPO_RANKING, TIPO_CLIENTES, TIPO_PRODUCTOS));
        cmb_tipoReporte.getSelectionModel().select(TIPO_FACTURAS);
        cmb_tipoReporte.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldV, newV) -> actualizarVisibilidad());

        cmb_estado.setItems(FXCollections.observableArrayList(ESTADO_TODAS, ESTADO_ACTIVAS, ESTADO_ANULADAS));
        cmb_estado.getSelectionModel().select(ESTADO_TODAS);

        dp_desde.setValue(LocalDate.now().withDayOfMonth(1));
        dp_hasta.setValue(LocalDate.now());

        actualizarVisibilidad();
        acc_generar(null);
    }

    private void actualizarVisibilidad() {
        String tipo = cmb_tipoReporte.getSelectionModel().getSelectedItem();
        boolean esFacturas  = TIPO_FACTURAS.equals(tipo);
        boolean esResumen   = TIPO_RESUMEN.equals(tipo);
        boolean esRanking   = TIPO_RANKING.equals(tipo);
        boolean esClientes  = TIPO_CLIENTES.equals(tipo);
        boolean esProductos = TIPO_PRODUCTOS.equals(tipo);
        boolean usaRangoFechas = esFacturas || esResumen || esRanking;

        tblFacturas.setVisible(esFacturas);
        tblFacturas.setManaged(esFacturas);
        tblResumen.setVisible(esResumen);
        tblResumen.setManaged(esResumen);
        tblRanking.setVisible(esRanking);
        tblRanking.setManaged(esRanking);
        tblClientes.setVisible(esClientes);
        tblClientes.setManaged(esClientes);
        tblProductos.setVisible(esProductos);
        tblProductos.setManaged(esProductos);

        lbl_estado.setVisible(esFacturas);
        lbl_estado.setManaged(esFacturas);
        cmb_estado.setVisible(esFacturas);
        cmb_estado.setManaged(esFacturas);

        dp_desde.setVisible(usaRangoFechas);
        dp_desde.setManaged(usaRangoFechas);
        dp_hasta.setVisible(usaRangoFechas);
        dp_hasta.setManaged(usaRangoFechas);

        btn_reintentarSri.setVisible(esFacturas);
        btn_reintentarSri.setManaged(esFacturas);
    }

    @FXML
    private void acc_reintentarSri(ActionEvent event) {
        FacturaReporte seleccionada = tblFacturas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Seleccione una factura del listado para reintentar su autorización SRI.");
            return;
        }

        btn_reintentarSri.setDisable(true);
        Task<ResultadoEmisionFactura> tarea = new Task<>() {
            @Override
            protected ResultadoEmisionFactura call() throws Exception {
                return madFactura.reintentarAutorizacionSri(seleccionada.getNumero());
            }
        };
        tarea.setOnSucceeded(ev -> {
            btn_reintentarSri.setDisable(false);
            ResultadoEmisionFactura resultado = tarea.getValue();
            mostrarInfo("Factura " + resultado.getNumeroFactura() + "\nEstado SRI: " + resultado.getEstadoSri()
                    + (resultado.getMensajeSri() != null ? "\n" + resultado.getMensajeSri() : ""));
            acc_generar(null);
        });
        tarea.setOnFailed(ev -> {
            btn_reintentarSri.setDisable(false);
            mostrarError("No se pudo reintentar la autorización SRI: " + tarea.getException().getMessage());
        });
        new Thread(tarea, "reintento-sri").start();
    }

    @FXML
    private void acc_generar(ActionEvent event) {
        String tipo = cmb_tipoReporte.getSelectionModel().getSelectedItem();
        try {
            if (TIPO_CLIENTES.equals(tipo)) {
                tblClientes.setItems(madCliente.listarClientes());
                return;
            }
            if (TIPO_PRODUCTOS.equals(tipo)) {
                tblProductos.setItems(madProducto.listarProductos());
                return;
            }

            LocalDate desde = dp_desde.getValue();
            LocalDate hasta = dp_hasta.getValue();
            if (desde == null || hasta == null) {
                mostrarError("Seleccione un rango de fechas válido.");
                return;
            }
            if (desde.isAfter(hasta)) {
                mostrarError("La fecha \"Desde\" no puede ser posterior a \"Hasta\".");
                return;
            }

            if (TIPO_FACTURAS.equals(tipo)) {
                String estadoSeleccionado = cmb_estado.getSelectionModel().getSelectedItem();
                String estado = ESTADO_ACTIVAS.equals(estadoSeleccionado) ? "A"
                        : ESTADO_ANULADAS.equals(estadoSeleccionado) ? "E" : null;
                tblFacturas.setItems(madReporte.listarFacturas(desde, hasta, estado));
            } else if (TIPO_RESUMEN.equals(tipo)) {
                tblResumen.setItems(armarFilasResumen(madReporte.obtenerResumenVentas(desde, hasta)));
            } else {
                tblRanking.setItems(madReporte.rankingProductos(desde, hasta, TOP_PRODUCTOS));
            }
        } catch (Exception e) {
            mostrarError("No se pudo generar el reporte: " + e.getMessage());
        }
    }

    private ObservableList<ResumenFila> armarFilasResumen(ResumenVentas r) {
        return FXCollections.observableArrayList(
                new ResumenFila("Facturas emitidas (activas)", String.valueOf(r.getNumFacturasActivas())),
                new ResumenFila("Facturas anuladas", String.valueOf(r.getNumFacturasAnuladas())),
                new ResumenFila("Subtotal 15%", formatearMonto(r.getSubtotal())),
                new ResumenFila("Subtotal 0%", formatearMonto(r.getSubtotalCero())),
                new ResumenFila("IVA", formatearMonto(r.getIva())),
                new ResumenFila("Total vendido", formatearMonto(r.getTotal())),
                new ResumenFila("Total en efectivo", formatearMonto(r.getTotalEfectivo())),
                new ResumenFila("Total en tarjeta", formatearMonto(r.getTotalTarjeta())),
                new ResumenFila("Total en transferencia", formatearMonto(r.getTotalTransferencia())));
    }

    private String formatearMonto(float monto) {
        return String.format(Locale.US, "$ %.2f", monto);
    }

    @FXML
    private void acc_exportarPdf(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar reporte a PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        chooser.setInitialFileName(nombreArchivoSugerido("pdf"));
        File destino = chooser.showSaveDialog(ventana());
        if (destino == null) return;

        try {
            Mod_jasperReporte.generarPDF(plantillaActual(), tituloReporteActual(), subtituloRango(),
                    tablaActual().getItems(), destino);
            mostrarInfo("Reporte exportado a:\n" + destino.getAbsolutePath());
        } catch (Exception e) {
            mostrarError("No se pudo exportar el PDF: " + e.getMessage());
        }
    }

    @FXML
    private void acc_exportarExcel(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar reporte a Excel");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        chooser.setInitialFileName(nombreArchivoSugerido("xlsx"));
        File destino = chooser.showSaveDialog(ventana());
        if (destino == null) return;

        try {
            Mod_jasperReporte.generarXLSX(plantillaActual(), tituloReporteActual(), subtituloRango(),
                    tablaActual().getItems(), destino);
            mostrarInfo("Reporte exportado a:\n" + destino.getAbsolutePath());
        } catch (Exception e) {
            mostrarError("No se pudo exportar el Excel: " + e.getMessage());
        }
    }

    private TableView<?> tablaActual() {
        String tipo = cmb_tipoReporte.getSelectionModel().getSelectedItem();
        if (TIPO_RESUMEN.equals(tipo)) return tblResumen;
        if (TIPO_RANKING.equals(tipo)) return tblRanking;
        if (TIPO_CLIENTES.equals(tipo)) return tblClientes;
        if (TIPO_PRODUCTOS.equals(tipo)) return tblProductos;
        return tblFacturas;
    }

    private String plantillaActual() {
        String tipo = cmb_tipoReporte.getSelectionModel().getSelectedItem();
        if (TIPO_RESUMEN.equals(tipo)) return "reporte_resumenVentas";
        if (TIPO_RANKING.equals(tipo)) return "reporte_rankingProductos";
        if (TIPO_CLIENTES.equals(tipo)) return "reporte_clientes";
        if (TIPO_PRODUCTOS.equals(tipo)) return "reporte_productos";
        return "reporte_facturas";
    }

    private String tituloReporteActual() {
        return cmb_tipoReporte.getSelectionModel().getSelectedItem();
    }

    private String subtituloRango() {
        String tipo = cmb_tipoReporte.getSelectionModel().getSelectedItem();
        if (TIPO_CLIENTES.equals(tipo) || TIPO_PRODUCTOS.equals(tipo)) {
            return "";
        }
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return "Del " + dp_desde.getValue().format(f) + " al " + dp_hasta.getValue().format(f);
    }

    private String nombreArchivoSugerido(String extension) {
        String tipo = cmb_tipoReporte.getSelectionModel().getSelectedItem();
        String base = TIPO_RESUMEN.equals(tipo) ? "resumen_ventas"
                : TIPO_RANKING.equals(tipo) ? "ranking_productos"
                : TIPO_CLIENTES.equals(tipo) ? "listado_clientes"
                : TIPO_PRODUCTOS.equals(tipo) ? "listado_productos"
                : "listado_facturas";
        return base + "_" + LocalDate.now() + "." + extension;
    }

    private Window ventana() {
        return cmb_tipoReporte.getScene() != null ? cmb_tipoReporte.getScene().getWindow() : null;
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
