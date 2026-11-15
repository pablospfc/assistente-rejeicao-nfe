package br.com.assistentenfe.assinatura;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Assina uma NF-e em memória no padrão do MOC: enveloped, C14N, RSA-SHA1 e SHA-1,
 * com o certificado em KeyInfo/X509Data e sem prefixo de namespace (D022).
 * Uma assinatura que já exista (ex.: a de preenchimento dos XMLs de base) é substituída.
 */
public final class AssinadorNFe {

    private static final String XMLDSIG = "http://www.w3.org/2000/09/xmldsig#";

    private AssinadorNFe() {
    }

    public static String assinar(String xml) {
        return assinar(xml, CertificadoTeste.padrao());
    }

    public static String assinar(String xml, CertificadoTeste certificado) {
        try {
            Document documento = ler(xml);
            Element raiz = documento.getDocumentElement();
            removerAssinaturas(raiz);

            Element infNFe = filho(raiz, "infNFe");
            infNFe.setIdAttribute("Id", true);

            XMLSignatureFactory fabrica = XMLSignatureFactory.getInstance("DOM");
            Reference referencia = fabrica.newReference("#" + infNFe.getAttribute("Id"),
                    fabrica.newDigestMethod(DigestMethod.SHA1, null),
                    List.of(fabrica.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null),
                            fabrica.newTransform(CanonicalizationMethod.INCLUSIVE, (TransformParameterSpec) null)),
                    null, null);
            SignedInfo signedInfo = fabrica.newSignedInfo(
                    fabrica.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
                    fabrica.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                    List.of(referencia));
            KeyInfoFactory chaves = fabrica.getKeyInfoFactory();
            KeyInfo keyInfo = chaves.newKeyInfo(List.of(chaves.newX509Data(List.of(certificado.certificado()))));

            fabrica.newXMLSignature(signedInfo, keyInfo).sign(new DOMSignContext(certificado.chavePrivada(), raiz));
            return escrever(documento);
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível assinar o XML de teste", e);
        }
    }

    private static void removerAssinaturas(Element raiz) {
        for (Node n = raiz.getFirstChild(); n != null; ) {
            Node proximo = n.getNextSibling();
            if (n instanceof Element e && "Signature".equals(e.getLocalName()) && XMLDSIG.equals(e.getNamespaceURI())) {
                raiz.removeChild(n);
                // Remove também a quebra de linha que antecedia a assinatura antiga.
                if (proximo != null && proximo.getNodeType() == Node.TEXT_NODE && proximo.getTextContent().isBlank()) {
                    Node seguinte = proximo.getNextSibling();
                    raiz.removeChild(proximo);
                    proximo = seguinte;
                }
            }
            n = proximo;
        }
    }

    private static Element filho(Element pai, String nomeLocal) {
        for (Node n = pai.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element e && nomeLocal.equals(e.getLocalName())) {
                return e;
            }
        }
        throw new IllegalArgumentException("XML de teste sem " + nomeLocal);
    }

    private static Document ler(String xml) throws Exception {
        DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setNamespaceAware(true);
        fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return fabrica.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private static String escrever(Document documento) throws Exception {
        TransformerFactory fabrica = TransformerFactory.newInstance();
        fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        var transformador = fabrica.newTransformer();
        transformador.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        StringWriter saida = new StringWriter();
        transformador.transform(new DOMSource(documento), new StreamResult(saida));
        // No Windows o serializador grava CRLF. Volta para LF, como nos arquivos de base, para as
        // mutações por trecho de texto continuarem valendo. A leitura do XML normaliza CRLF para LF,
        // então o digest não muda.
        return saida.toString().replace("\r\n", "\n");
    }
}
