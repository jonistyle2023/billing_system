package upse.calculacion.sri;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import upse.calculacion.general.Mod_log;
import upse.calculacion.modelo.Cliente;
import upse.calculacion.modelo.ConfiguracionSri;
import upse.calculacion.modelo.DetFactura;
import upse.calculacion.modelo.Empresa;

/**
 * Arma el XML de la factura electrónica (esquema SRI v1.1.0: infoTributaria, infoFactura,
 * detalles, infoAdicional) a partir de los mismos datos que ya calcula {@code Mad_factura}
 * al emitir la venta.
 *
 * <p><b>Aviso:</b> generado según la estructura pública documentada del SRI; antes de emitir
 * facturas reales debe validarse contra el ambiente de pruebas del SRI (ver README).
 */
public class FacturaXmlBuilder {

    private static final String COD_DOC_FACTURA = "01";
    private static final String TIPO_ID_RUC = "04";
    private static final String TIPO_ID_CEDULA = "05";
    private static final String TIPO_ID_CONSUMIDOR_FINAL = "07";
    private static final String CONSUMIDOR_FINAL_ID = "9999999999999";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private FacturaXmlBuilder() {
    }

    public static String construir(String claveAcceso, String secuencial,
                                    Empresa empresa, ConfiguracionSri configuracion,
                                    LocalDate fecha, Cliente cliente,
                                    float subtotal, float baseCero, float iva, float total,
                                    float tasaIvaPorcentaje, List<DetFactura> detalles,
                                    String pagoMetodo, float pagoMonto) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();

            Element factura = doc.createElement("factura");
            factura.setAttribute("id", "comprobante");
            factura.setAttribute("version", "1.1.0");
            doc.appendChild(factura);

            factura.appendChild(infoTributaria(doc, claveAcceso, secuencial, empresa, configuracion));
            factura.appendChild(infoFactura(doc, fecha, empresa, cliente, subtotal, baseCero, iva, total,
                    tasaIvaPorcentaje, pagoMetodo, pagoMonto));
            factura.appendChild(detalles(doc, detalles, tasaIvaPorcentaje));
            factura.appendChild(infoAdicional(doc, cliente));

            return serializar(doc);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo construir el XML de la factura electrónica", e);
        }
    }

    private static Element infoTributaria(Document doc, String claveAcceso, String secuencial,
                                           Empresa empresa, ConfiguracionSri configuracion) {
        Element info = doc.createElement("infoTributaria");
        agregar(doc, info, "ambiente", configuracion.getAmbiente());
        agregar(doc, info, "tipoEmision", configuracion.getTipoEmision());
        agregar(doc, info, "razonSocial", empresa.getNombre());
        agregar(doc, info, "ruc", empresa.getRuc());
        agregar(doc, info, "claveAcceso", claveAcceso);
        agregar(doc, info, "codDoc", COD_DOC_FACTURA);
        agregar(doc, info, "estab", configuracion.getEstablecimiento());
        agregar(doc, info, "ptoEmi", configuracion.getPuntoEmision());
        agregar(doc, info, "secuencial", String.format("%09d", Long.parseLong(secuencial)));
        agregar(doc, info, "dirMatriz", empresa.getDireccion());
        return info;
    }

    private static Element infoFactura(Document doc, LocalDate fecha, Empresa empresa, Cliente cliente,
                                        float subtotal, float baseCero, float iva, float total,
                                        float tasaIvaPorcentaje, String pagoMetodo, float pagoMonto) {
        Element info = doc.createElement("infoFactura");
        agregar(doc, info, "fechaEmision", fecha.format(FORMATO_FECHA));
        agregar(doc, info, "obligadoContabilidad", empresa.isObligadoContabilidad() ? "SI" : "NO");

        boolean hayCliente = cliente != null && cliente.getCedula() != null && !cliente.getCedula().isBlank();
        agregar(doc, info, "tipoIdentificacionComprador",
                hayCliente ? tipoIdentificacion(cliente.getCedula()) : TIPO_ID_CONSUMIDOR_FINAL);
        agregar(doc, info, "razonSocialComprador", hayCliente ? cliente.getNombres() : "CONSUMIDOR FINAL");
        agregar(doc, info, "identificacionComprador", hayCliente ? cliente.getCedula() : CONSUMIDOR_FINAL_ID);

        agregar(doc, info, "totalSinImpuestos", moneda(subtotal + baseCero));
        agregar(doc, info, "totalDescuento", moneda(0));

        Element totalConImpuestos = doc.createElement("totalConImpuestos");
        if (subtotal > 0) {
            totalConImpuestos.appendChild(totalImpuesto(doc, tasaIvaPorcentaje, subtotal, iva));
        }
        if (baseCero > 0) {
            totalConImpuestos.appendChild(totalImpuesto(doc, 0f, baseCero, 0f));
        }
        info.appendChild(totalConImpuestos);

        agregar(doc, info, "propina", moneda(0));
        agregar(doc, info, "importeTotal", moneda(total));
        agregar(doc, info, "moneda", "DOLAR");

        Element pagos = doc.createElement("pagos");
        Element pago = doc.createElement("pago");
        agregar(doc, pago, "formaPago", codigoFormaPago(pagoMetodo));
        agregar(doc, pago, "total", moneda(pagoMonto > 0 ? pagoMonto : total));
        pagos.appendChild(pago);
        info.appendChild(pagos);

        return info;
    }

    private static Element totalImpuesto(Document doc, float tasaPorcentaje, float baseImponible, float valor) {
        Element totalImpuesto = doc.createElement("totalImpuesto");
        agregar(doc, totalImpuesto, "codigo", "2"); // 2 = IVA
        agregar(doc, totalImpuesto, "codigoPorcentaje", codigoPorcentajeIva(tasaPorcentaje));
        agregar(doc, totalImpuesto, "baseImponible", moneda(baseImponible));
        agregar(doc, totalImpuesto, "valor", moneda(valor));
        return totalImpuesto;
    }

    private static Element detalles(Document doc, List<DetFactura> lineas, float tasaIvaPorcentaje) {
        Element detalles = doc.createElement("detalles");
        for (DetFactura linea : lineas) {
            Element detalle = doc.createElement("detalle");
            agregar(doc, detalle, "codigoPrincipal", linea.getProd_cod());
            agregar(doc, detalle, "descripcion", linea.getProd_nombre());
            agregar(doc, detalle, "cantidad", cantidad(linea.getCantidad()));
            agregar(doc, detalle, "precioUnitario", moneda(linea.getPrecio()));
            agregar(doc, detalle, "descuento", moneda(0));
            agregar(doc, detalle, "precioTotalSinImpuesto", moneda(linea.getTotal()));

            float tarifa = linea.isAplicaIva() ? tasaIvaPorcentaje : 0f;
            float valorIva = linea.isAplicaIva() ? linea.getTotal() * (tasaIvaPorcentaje / 100f) : 0f;

            Element impuestos = doc.createElement("impuestos");
            Element impuesto = doc.createElement("impuesto");
            agregar(doc, impuesto, "codigo", "2");
            agregar(doc, impuesto, "codigoPorcentaje", codigoPorcentajeIva(tarifa));
            agregar(doc, impuesto, "tarifa", moneda(tarifa));
            agregar(doc, impuesto, "baseImponible", moneda(linea.getTotal()));
            agregar(doc, impuesto, "valor", moneda(valorIva));
            impuestos.appendChild(impuesto);
            detalle.appendChild(impuestos);

            detalles.appendChild(detalle);
        }
        return detalles;
    }

    private static Element infoAdicional(Document doc, Cliente cliente) {
        Element info = doc.createElement("infoAdicional");
        if (cliente != null) {
            if (cliente.getTelefono() != null && !cliente.getTelefono().isBlank()) {
                info.appendChild(campoAdicional(doc, "Telefono", cliente.getTelefono()));
            }
            if (cliente.getCorreo() != null && !cliente.getCorreo().isBlank()) {
                info.appendChild(campoAdicional(doc, "Email", cliente.getCorreo()));
            }
            if (cliente.getDireccion() != null && !cliente.getDireccion().isBlank()) {
                info.appendChild(campoAdicional(doc, "Direccion", cliente.getDireccion()));
            }
        }
        return info;
    }

    private static Element campoAdicional(Document doc, String nombre, String valor) {
        Element campo = doc.createElement("campoAdicional");
        campo.setAttribute("nombre", nombre);
        campo.setTextContent(valor);
        return campo;
    }

    private static String tipoIdentificacion(String cedula) {
        return cedula != null && cedula.length() == 13 ? TIPO_ID_RUC : TIPO_ID_CEDULA;
    }

    /** Códigos del catálogo SRI para codigoPorcentaje de IVA (el 15% vigente usa "4"). */
    private static String codigoPorcentajeIva(float tarifaPorcentaje) {
        int redondeado = Math.round(tarifaPorcentaje);
        switch (redondeado) {
            case 0: return "0";
            case 5: return "5";
            case 12: return "2";
            case 14: return "3";
            case 15: return "4";
            default:
                Mod_log.warning("Tarifa de IVA " + tarifaPorcentaje + "% sin código SRI conocido; usando \"4\" por defecto");
                return "4";
        }
    }

    /** Códigos del catálogo SRI para formaPago (01=efectivo, 16=tarjeta de crédito, 20=transferencia). */
    private static String codigoFormaPago(String pagoMetodo) {
        if (pagoMetodo == null) return "01";
        return switch (pagoMetodo) {
            case "TARJETA" -> "16";
            case "TRANSFERENCIA" -> "20";
            default -> "01";
        };
    }

    private static String moneda(float valor) {
        return String.format(Locale.US, "%.2f", valor);
    }

    private static String cantidad(float valor) {
        return String.format(Locale.US, "%.2f", valor);
    }

    private static void agregar(Document doc, Element padre, String etiqueta, String valor) {
        Element elemento = doc.createElement(etiqueta);
        elemento.setTextContent(valor == null ? "" : valor);
        padre.appendChild(elemento);
    }

    private static String serializar(Document doc) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }
}
