package upse.calculacion.sri;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Firma XAdES-BES del XML de la factura, usando únicamente la API estándar del JDK
 * ({@code javax.xml.crypto.dsig}, sin dependencias nuevas) sobre un keystore PKCS12
 * (.p12/.pfx) emitido por una entidad certificadora acreditada en Ecuador.
 *
 * <p><b>Aviso importante:</b> esta clase implementa la estructura XAdES-BES documentada
 * públicamente para el SRI (SignedProperties con SigningTime + SigningCertificate,
 * referenciadas desde el SignedInfo), pero no ha podido validarse contra el validador
 * real del SRI en este entorno (no hay certificado ni ambiente de pruebas disponible).
 * Antes de usar en producción, firmar un comprobante de prueba y enviarlo al ambiente
 * de pruebas del SRI para confirmar que la firma es aceptada.
 */
public class FirmaXadesBes {

    private static final String XADES_NS = "http://uri.etsi.org/01903/v1.3.2#";
    private static final String DS_NS = XMLSignature.XMLNS;

    private FirmaXadesBes() {
    }

    public static String firmar(String xmlSinFirmar, String rutaCertificado, String claveCertificado) throws Exception {
        char[] clave = claveCertificado.toCharArray();
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream entrada = new FileInputStream(rutaCertificado)) {
            keyStore.load(entrada, clave);
        }

        String alias = Collections.list(keyStore.aliases()).stream()
                .filter(a -> {
                    try { return keyStore.isKeyEntry(a); } catch (Exception e) { return false; }
                })
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("El certificado no contiene una entrada de clave privada"));

        PrivateKey clavePrivada = (PrivateKey) keyStore.getKey(alias, clave);
        X509Certificate certificado = (X509Certificate) keyStore.getCertificate(alias);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xmlSinFirmar.getBytes(StandardCharsets.UTF_8)));

        Element comprobante = doc.getDocumentElement(); // <factura id="comprobante" ...>
        comprobante.setIdAttribute("id", true);

        String signatureId = "Signature" + UUID.randomUUID();
        String certificateId = "Certificate" + UUID.randomUUID();
        String signedPropertiesId = "SignedProperties" + UUID.randomUUID();

        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

        // Referencia 1: el comprobante completo (enveloped, dentro del mismo XML que firma).
        Reference refComprobante = fac.newReference("#comprobante",
                fac.newDigestMethod(DigestMethod.SHA1, null),
                List.of(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null),
                        fac.newTransform(CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec) null)),
                null, null);

        // Referencia 2: las propiedades firmadas XAdES (SigningTime + SigningCertificate).
        Element qualifyingProperties = construirQualifyingProperties(
                doc, certificado, signatureId, signedPropertiesId);

        Reference refSignedProperties = fac.newReference("#" + signedPropertiesId,
                fac.newDigestMethod(DigestMethod.SHA1, null),
                List.of(fac.newTransform(CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec) null)),
                "http://uri.etsi.org/01903#SignedProperties", null);

        SignedInfo signedInfo = fac.newSignedInfo(
                fac.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null),
                fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                List.of(refComprobante, refSignedProperties));

        KeyInfoFactory kif = fac.getKeyInfoFactory();
        X509Data x509Data = kif.newX509Data(List.of(certificado));
        KeyInfo keyInfo = kif.newKeyInfo(List.of(x509Data), certificateId);

        XMLObject objetoXades = fac.newXMLObject(
                List.of(new DOMStructure(qualifyingProperties)), null, null, null);

        XMLSignature signature = fac.newXMLSignature(signedInfo, keyInfo, List.of(objetoXades), signatureId, null);

        DOMSignContext contexto = new DOMSignContext(clavePrivada, comprobante);
        contexto.setIdAttributeNS(comprobante, null, "id");
        contexto.setDefaultNamespacePrefix("ds");
        registrarIdEnQualifyingProperties(contexto, qualifyingProperties, signedPropertiesId);

        signature.sign(contexto);

        return serializar(doc);
    }

    private static Element construirQualifyingProperties(Document doc, X509Certificate certificado,
                                                           String signatureId, String signedPropertiesId) throws Exception {
        Element qualifyingProperties = doc.createElementNS(XADES_NS, "xades:QualifyingProperties");
        qualifyingProperties.setAttribute("Target", "#" + signatureId);

        Element signedProperties = doc.createElementNS(XADES_NS, "xades:SignedProperties");
        signedProperties.setAttribute("Id", signedPropertiesId);
        qualifyingProperties.appendChild(signedProperties);

        Element signedSignatureProperties = doc.createElementNS(XADES_NS, "xades:SignedSignatureProperties");
        signedProperties.appendChild(signedSignatureProperties);

        Element signingTime = doc.createElementNS(XADES_NS, "xades:SigningTime");
        signingTime.setTextContent(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        signedSignatureProperties.appendChild(signingTime);

        Element signingCertificate = doc.createElementNS(XADES_NS, "xades:SigningCertificate");
        Element cert = doc.createElementNS(XADES_NS, "xades:Cert");
        Element certDigest = doc.createElementNS(XADES_NS, "xades:CertDigest");

        Element digestMethod = doc.createElementNS(DS_NS, "ds:DigestMethod");
        digestMethod.setAttribute("Algorithm", DigestMethod.SHA1);
        certDigest.appendChild(digestMethod);

        Element digestValue = doc.createElementNS(DS_NS, "ds:DigestValue");
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        digestValue.setTextContent(Base64.getEncoder().encodeToString(sha1.digest(certificado.getEncoded())));
        certDigest.appendChild(digestValue);
        cert.appendChild(certDigest);

        Element issuerSerial = doc.createElementNS(XADES_NS, "xades:IssuerSerial");
        Element issuerName = doc.createElementNS(DS_NS, "ds:X509IssuerName");
        issuerName.setTextContent(certificado.getIssuerX500Principal().getName());
        Element serialNumber = doc.createElementNS(DS_NS, "ds:X509SerialNumber");
        serialNumber.setTextContent(certificado.getSerialNumber().toString());
        issuerSerial.appendChild(issuerName);
        issuerSerial.appendChild(serialNumber);
        cert.appendChild(issuerSerial);

        signingCertificate.appendChild(cert);
        signedSignatureProperties.appendChild(signingCertificate);

        return qualifyingProperties;
    }

    private static void registrarIdEnQualifyingProperties(DOMSignContext contexto, Element qualifyingProperties,
                                                           String signedPropertiesId) {
        org.w3c.dom.NodeList hijos = qualifyingProperties.getElementsByTagNameNS(XADES_NS, "SignedProperties");
        if (hijos.getLength() > 0) {
            Element signedProperties = (Element) hijos.item(0);
            signedProperties.setIdAttribute("Id", true);
            contexto.setIdAttributeNS(signedProperties, null, "Id");
        }
    }

    private static String serializar(Document doc) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }
}
