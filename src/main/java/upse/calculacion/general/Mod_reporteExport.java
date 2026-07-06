package upse.calculacion.general;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import upse.calculacion.Mad.Mad_empresa;
import upse.calculacion.modelo.Empresa;

/**
 * Exportador genérico de reportes: sirve para cualquier {@link TableView} porque lee
 * encabezados y valores de celda de forma reflexiva (no depende del tipo de fila).
 */
public class Mod_reporteExport {

    private Mod_reporteExport() {
    }

    public static void exportarCSV(TableView<?> tabla, File destino) throws IOException {
        List<? extends TableColumn<?, ?>> columnas = tabla.getColumns();
        try (Writer w = new OutputStreamWriter(new FileOutputStream(destino), StandardCharsets.UTF_8)) {
            w.write('﻿'); // BOM para que Excel detecte UTF-8 automáticamente
            String[] encabezados = new String[columnas.size()];
            for (int i = 0; i < columnas.size(); i++) {
                encabezados[i] = columnas.get(i).getText();
            }
            escribirFilaCSV(w, encabezados);

            for (Object fila : tabla.getItems()) {
                String[] valores = new String[columnas.size()];
                for (int i = 0; i < columnas.size(); i++) {
                    valores[i] = valorCelda(columnas.get(i), fila);
                }
                escribirFilaCSV(w, valores);
            }
        }
    }

    public static void exportarPDF(String titulo, String subtitulo, TableView<?> tabla, File destino)
            throws IOException, DocumentException {
        List<? extends TableColumn<?, ?>> columnas = tabla.getColumns();
        Document documento = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
        PdfWriter.getInstance(documento, new FileOutputStream(destino));
        documento.open();

        Empresa empresa = obtenerEmpresa();
        if (empresa != null) {
            documento.add(new Paragraph(nullASeguro(empresa.getNombre()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            String infoEmpresa = (nullASeguro(empresa.getDireccion()) + " - " + nullASeguro(empresa.getTelefono()))
                    .replaceAll("^( - )+|( - )+$", "");
            if (!infoEmpresa.isBlank()) {
                documento.add(new Paragraph(infoEmpresa, FontFactory.getFont(FontFactory.HELVETICA, 9)));
            }
        }
        documento.add(new Paragraph(titulo, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        if (subtitulo != null && !subtitulo.isBlank()) {
            documento.add(new Paragraph(subtitulo, FontFactory.getFont(FontFactory.HELVETICA, 9)));
        }
        documento.add(new Paragraph(" "));

        PdfPTable pdfTabla = new PdfPTable(Math.max(columnas.size(), 1));
        pdfTabla.setWidthPercentage(100);
        for (TableColumn<?, ?> columna : columnas) {
            PdfPCell celda = new PdfPCell(new Phrase(columna.getText(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE)));
            celda.setBackgroundColor(new Color(0x2c, 0x3e, 0x50));
            celda.setPadding(5f);
            pdfTabla.addCell(celda);
        }
        for (Object fila : tabla.getItems()) {
            for (TableColumn<?, ?> columna : columnas) {
                PdfPCell celda = new PdfPCell(new Phrase(valorCelda(columna, fila), FontFactory.getFont(FontFactory.HELVETICA, 9)));
                celda.setPadding(4f);
                pdfTabla.addCell(celda);
            }
        }
        documento.add(pdfTabla);

        documento.add(new Paragraph(" "));
        documento.add(new Paragraph(
                "Generado el " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8)));

        documento.close();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String valorCelda(TableColumn<?, ?> columna, Object fila) {
        TableColumn cruda = columna;
        Object valor = cruda.getCellObservableValue(fila) != null ? cruda.getCellObservableValue(fila).getValue() : null;
        return valor == null ? "" : String.valueOf(valor);
    }

    private static void escribirFilaCSV(Writer w, String[] valores) throws IOException {
        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) linea.append(',');
            linea.append(escaparCSV(valores[i]));
        }
        linea.append("\r\n");
        w.write(linea.toString());
    }

    private static String escaparCSV(String valor) {
        if (valor == null) return "";
        boolean necesitaComillas = valor.contains(",") || valor.contains("\"") || valor.contains("\n") || valor.contains("\r");
        String escapado = valor.replace("\"", "\"\"");
        return necesitaComillas ? "\"" + escapado + "\"" : escapado;
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
