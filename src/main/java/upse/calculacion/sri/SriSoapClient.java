package upse.calculacion.sri;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import upse.calculacion.general.Mod_log;
import upse.calculacion.modelo.ConfiguracionSri;

/**
 * Cliente SOAP (sin dependencias nuevas, con {@link HttpClient} del JDK) para los web
 * services de recepción y autorización de comprobantes electrónicos del SRI.
 *
 * <p><b>Aviso:</b> las URLs y el formato de los envelopes SOAP corresponden a la
 * documentación pública del esquema offline del SRI; no ha sido posible probar
 * contra el servicio real en este entorno (sin certificado ni RUC de pruebas).
 */
public class SriSoapClient {

    private static final String URL_PRUEBAS_RECEPCION =
            "https://celcer.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantesOffline";
    private static final String URL_PRUEBAS_AUTORIZACION =
            "https://celcer.sri.gob.ec/comprobantes-electronicos-ws/AutorizacionComprobantesOffline";
    private static final String URL_PRODUCCION_RECEPCION =
            "https://cel.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantesOffline";
    private static final String URL_PRODUCCION_AUTORIZACION =
            "https://cel.sri.gob.ec/comprobantes-electronicos-ws/AutorizacionComprobantesOffline";

    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();

    public ResultadoRecepcion enviarRecepcion(String xmlFirmado, ConfiguracionSri configuracion) {
        String url = configuracion.esProduccion() ? URL_PRODUCCION_RECEPCION : URL_PRUEBAS_RECEPCION;
        String xmlBase64 = Base64.getEncoder().encodeToString(xmlFirmado.getBytes(StandardCharsets.UTF_8));
        String cuerpo = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                + "xmlns:ec=\"http://ec.gob.sri.ws.recepcion\">"
                + "<soapenv:Header/><soapenv:Body><ec:validarComprobante><xml>"
                + xmlBase64 + "</xml></ec:validarComprobante></soapenv:Body></soapenv:Envelope>";

        try {
            Document respuesta = enviarSoap(url, cuerpo);
            String estado = textoDeEtiqueta(respuesta, "estado");
            boolean recibida = "RECIBIDA".equalsIgnoreCase(estado);
            String mensaje = recibida ? null : primerMensaje(respuesta);
            return new ResultadoRecepcion(recibida, mensaje);
        } catch (Exception e) {
            Mod_log.error("Error al enviar el comprobante a RecepcionComprobantesOffline", e);
            return new ResultadoRecepcion(false, "No se pudo contactar al SRI: " + e.getMessage());
        }
    }

    public ResultadoAutorizacion consultarAutorizacion(String claveAcceso, ConfiguracionSri configuracion) {
        String url = configuracion.esProduccion() ? URL_PRODUCCION_AUTORIZACION : URL_PRUEBAS_AUTORIZACION;
        String cuerpo = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                + "xmlns:ec=\"http://ec.gob.sri.ws.autorizacion\">"
                + "<soapenv:Header/><soapenv:Body><ec:autorizacionComprobante><claveAccesoComprobante>"
                + claveAcceso + "</claveAccesoComprobante></ec:autorizacionComprobante></soapenv:Body></soapenv:Envelope>";

        try {
            Document respuesta = enviarSoap(url, cuerpo);
            NodeList autorizaciones = respuesta.getElementsByTagNameNS("*", "autorizacion");
            if (autorizaciones.getLength() == 0) {
                return new ResultadoAutorizacion(ResultadoAutorizacion.Estado.PENDIENTE, null, null,
                        "El SRI todavía no procesa este comprobante");
            }

            Element autorizacion = (Element) autorizaciones.item(0);
            String estado = textoDeHijo(autorizacion, "estado");
            boolean autorizado = "AUTORIZADO".equalsIgnoreCase(estado);
            String numeroAutorizacion = textoDeHijo(autorizacion, "numeroAutorizacion");
            String fechaTexto = textoDeHijo(autorizacion, "fechaAutorizacion");
            OffsetDateTime fecha = parsearFecha(fechaTexto);
            String mensaje = autorizado ? null : primerMensaje(respuesta);

            return new ResultadoAutorizacion(
                    autorizado ? ResultadoAutorizacion.Estado.AUTORIZADO : ResultadoAutorizacion.Estado.RECHAZADO,
                    numeroAutorizacion, fecha, mensaje);
        } catch (Exception e) {
            Mod_log.error("Error al consultar AutorizacionComprobantesOffline", e);
            return new ResultadoAutorizacion(ResultadoAutorizacion.Estado.PENDIENTE, null, null,
                    "No se pudo contactar al SRI: " + e.getMessage());
        }
    }

    private Document enviarSoap(String url, String cuerpoSoap) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .header("Content-Type", "text/xml; charset=UTF-8")
                .header("SOAPAction", "")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpoSoap, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("El SRI respondió HTTP " + response.statusCode());
        }

        var dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(
                new org.xml.sax.InputSource(new java.io.StringReader(response.body())));
    }

    private String textoDeEtiqueta(Document doc, String etiqueta) {
        NodeList lista = doc.getElementsByTagNameNS("*", etiqueta);
        return lista.getLength() > 0 ? lista.item(0).getTextContent() : null;
    }

    private String textoDeHijo(Element padre, String etiqueta) {
        NodeList lista = padre.getElementsByTagNameNS("*", etiqueta);
        return lista.getLength() > 0 ? lista.item(0).getTextContent() : null;
    }

    private String primerMensaje(Document doc) {
        NodeList mensajes = doc.getElementsByTagNameNS("*", "mensaje");
        if (mensajes.getLength() == 0) return null;
        Element mensaje = (Element) mensajes.item(0);
        String identificador = textoDeHijo(mensaje, "identificador");
        String texto = textoDeHijo(mensaje, "mensaje");
        String infoAdicional = textoDeHijo(mensaje, "informacionAdicional");
        StringBuilder sb = new StringBuilder();
        if (identificador != null) sb.append('[').append(identificador).append("] ");
        if (texto != null) sb.append(texto);
        if (infoAdicional != null) sb.append(" - ").append(infoAdicional);
        return sb.length() > 0 ? sb.toString() : null;
    }

    private OffsetDateTime parsearFecha(String texto) {
        if (texto == null) return null;
        try {
            return OffsetDateTime.parse(texto);
        } catch (Exception e) {
            return null;
        }
    }
}
