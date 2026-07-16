package upse.calculacion.general;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import upse.calculacion.Mad.Mad_empresa;
import upse.calculacion.modelo.Empresa;

/**
 * Generación de reportes con JasperReports a partir de plantillas .jrxml diseñadas
 * en Jaspersoft Studio (carpeta {@code upse/calculacion/reportes/} en resources).
 */
public class Mod_jasperReporte {

    private static final String CARPETA_PLANTILLAS = "/upse/calculacion/reportes/";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Map<String, JasperReport> CACHE_REPORTES = new ConcurrentHashMap<>();

    private Mod_jasperReporte() {
    }

    public static void generarPDF(String plantilla, String titulo, String subtitulo, Collection<?> datos, File destino)
            throws JRException {
        JasperPrint impreso = llenarReporte(plantilla, titulo, subtitulo, datos);
        JasperExportManager.exportReportToPdfFile(impreso, destino.getAbsolutePath());
    }

    public static void generarXLSX(String plantilla, String titulo, String subtitulo, Collection<?> datos, File destino)
            throws JRException {
        JasperPrint impreso = llenarReporte(plantilla, titulo, subtitulo, datos);

        JRXlsxExporter exportador = new JRXlsxExporter();
        exportador.setExporterInput(new SimpleExporterInput(impreso));
        exportador.setExporterOutput(new SimpleOutputStreamExporterOutput(destino));

        SimpleXlsxReportConfiguration configuracion = new SimpleXlsxReportConfiguration();
        configuracion.setOnePagePerSheet(false);
        configuracion.setRemoveEmptySpaceBetweenRows(true);
        configuracion.setWhitePageBackground(false);
        configuracion.setDetectCellType(true);
        exportador.setConfiguration(configuracion);

        exportador.exportReport();
    }

    private static JasperPrint llenarReporte(String plantilla, String titulo, String subtitulo, Collection<?> datos)
            throws JRException {
        JasperReport reporte = compilar(plantilla);
        Map<String, Object> parametros = armarParametros(titulo, subtitulo);
        return JasperFillManager.fillReport(reporte, parametros, new JRBeanCollectionDataSource(datos));
    }

    /**
     * Genera el PDF de la factura (RIDE) a partir de sus propios parámetros (encabezado de
     * empresa/cliente/factura) y el detalle de líneas — a diferencia de los reportes genéricos,
     * el llamador arma los parámetros porque la forma de los datos es específica de la factura.
     */
    public static void generarFacturaPDF(Map<String, Object> parametros, Collection<?> detalles, File destino)
            throws JRException {
        JasperPrint impreso = JasperFillManager.fillReport(
                compilar("factura_ride"), parametros, new JRBeanCollectionDataSource(detalles));
        JasperExportManager.exportReportToPdfFile(impreso, destino.getAbsolutePath());
    }

    /** Envía la factura (RIDE) directo a una impresora, mostrando el diálogo de selección del sistema. */
    public static void imprimirFactura(Map<String, Object> parametros, Collection<?> detalles) throws JRException {
        JasperPrint impreso = JasperFillManager.fillReport(
                compilar("factura_ride"), parametros, new JRBeanCollectionDataSource(detalles));
        JasperPrintManager.printReport(impreso, true);
    }

    private static synchronized JasperReport compilar(String plantilla) throws JRException {
        JasperReport reporte = CACHE_REPORTES.get(plantilla);
        if (reporte != null) {
            return reporte;
        }
        String ruta = CARPETA_PLANTILLAS + plantilla + ".jrxml";
        try (InputStream entrada = Mod_jasperReporte.class.getResourceAsStream(ruta)) {
            if (entrada == null) {
                throw new JRException("No se encontró la plantilla de reporte: " + ruta);
            }
            reporte = JasperCompileManager.compileReport(entrada);
            CACHE_REPORTES.put(plantilla, reporte);
            return reporte;
        } catch (java.io.IOException e) {
            throw new JRException("No se pudo leer la plantilla de reporte: " + ruta, e);
        }
    }

    private static Map<String, Object> armarParametros(String titulo, String subtitulo) {
        Map<String, Object> parametros = new HashMap<>();
        Empresa empresa = obtenerEmpresa();
        parametros.put("empresaNombre", empresa != null ? nullASeguro(empresa.getNombre()) : "");
        parametros.put("empresaDireccion", empresa != null ? nullASeguro(empresa.getDireccion()) : "");
        parametros.put("empresaTelefono", empresa != null ? nullASeguro(empresa.getTelefono()) : "");
        parametros.put("titulo", nullASeguro(titulo));
        parametros.put("subtitulo", nullASeguro(subtitulo));
        parametros.put("fechaGeneracion", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        return parametros;
    }

    private static Empresa obtenerEmpresa() {
        try {
            return new Mad_empresa().obtenerEmpresaActiva();
        } catch (Exception e) {
            return null;
        }
    }

    private static String nullASeguro(String valor) {
        return valor == null ? "" : valor;
    }
}
