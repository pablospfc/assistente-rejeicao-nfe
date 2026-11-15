package br.com.assistentenfe.estrutura;

import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.Ocorrencia;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.Data;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Optional;

public final class VerificadorAssinatura {

    public static final String CODIGO = "297";

    private static final String XMLDSIG = XMLSignature.XMLNS;
    private static final int BITS_MINIMOS = 1024;

    public List<Ocorrencia> verificar(Document documento) {
        Element raiz = documento.getDocumentElement();
        Optional<Element> assinatura = filho(raiz, XMLDSIG, "Signature");
        Optional<Element> infNFe = filho(raiz, null, "infNFe");
        if (assinatura.isEmpty() || infNFe.isEmpty() || infNFe.get().getAttribute("Id").isBlank()) {
            return List.of();
        }
        infNFe.get().setIdAttribute("Id", true);
        String referenciaPermitida = "#" + infNFe.get().getAttribute("Id");

        XMLSignatureFactory fabrica = XMLSignatureFactory.getInstance("DOM");
        DOMValidateContext contexto = new DOMValidateContext(new ChaveDoCertificado(), assinatura.get());
        contexto.setProperty("org.jcp.xml.dsig.secureValidation", Boolean.FALSE);
        contexto.setURIDereferencer(new SomenteInfNFe(referenciaPermitida, fabrica.getURIDereferencer()));

        try {
            XMLSignature xmlSignature = fabrica.unmarshalXMLSignature(contexto);
            boolean digestConfere = true;
            for (Reference referencia : xmlSignature.getSignedInfo().getReferences()) {
                digestConfere &= referencia.validate(contexto);
            }
            boolean valorConfere = valorConfere(xmlSignature, contexto);

            if (digestConfere && valorConfere) {
                return List.of();
            }
            if (valorConfere) {
                return List.of(ocorrencia("/NFe/infNFe", "conteudoAlterado"));
            }
            return List.of(ocorrencia("/NFe/Signature/SignatureValue", "valorDaAssinatura"));
        } catch (MarshalException | XMLSignatureException | ChaveIndisponivel e) {
            return List.of();
        }
    }

    private static boolean valorConfere(XMLSignature xmlSignature, DOMValidateContext contexto) throws ChaveIndisponivel {
        try {
            return xmlSignature.getSignatureValue().validate(contexto);
        } catch (XMLSignatureException e) {
            if (e.getCause() instanceof KeySelectorException) {
                throw new ChaveIndisponivel();
            }
            return false;
        }
    }

    private static final class ChaveIndisponivel extends Exception {
    }

    private static Ocorrencia ocorrencia(String xpath, String motivo) {
        return new Ocorrencia(CODIGO, Camada.ESTRUTURA, xpath, null, null, motivo);
    }

    private static Optional<Element> filho(Element pai, String namespace, String nomeLocal) {
        for (Node n = pai.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element e && nomeLocal.equals(e.getLocalName())
                    && (namespace == null || namespace.equals(e.getNamespaceURI()))) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    static final class ChaveDoCertificado extends KeySelector {

        @Override
        public KeySelectorResult select(KeyInfo keyInfo, Purpose proposito, AlgorithmMethod metodo,
                                        XMLCryptoContext contexto) throws KeySelectorException {
            if (keyInfo != null) {
                for (XMLStructure item : keyInfo.getContent()) {
                    if (item instanceof X509Data dados) {
                        for (Object conteudo : dados.getContent()) {
                            if (conteudo instanceof X509Certificate certificado
                                    && certificado.getPublicKey() instanceof RSAPublicKey chave) {
                                if (chave.getModulus().bitLength() < BITS_MINIMOS) {
                                    throw new KeySelectorException("Chave RSA com menos de " + BITS_MINIMOS + " bits");
                                }
                                return () -> chave;
                            }
                        }
                    }
                }
            }
            throw new KeySelectorException("Certificado RSA não encontrado no KeyInfo");
        }
    }

    static final class SomenteInfNFe implements URIDereferencer {

        private final String referenciaPermitida;
        private final URIDereferencer padrao;

        SomenteInfNFe(String referenciaPermitida, URIDereferencer padrao) {
            this.referenciaPermitida = referenciaPermitida;
            this.padrao = padrao;
        }

        @Override
        public Data dereference(URIReference referencia, XMLCryptoContext contexto) throws URIReferenceException {
            if (!referenciaPermitida.equals(referencia.getURI())) {
                throw new URIReferenceException("Referência não permitida: " + referencia.getURI());
            }
            return padrao.dereference(referencia, contexto);
        }
    }
}
