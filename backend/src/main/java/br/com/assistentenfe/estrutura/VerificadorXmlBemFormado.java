package br.com.assistentenfe.estrutura;

import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.Ocorrencia;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public final class VerificadorXmlBemFormado {

    public static final String CODIGO = "243";

    public Document ler(byte[] xml) throws XmlMalFormadoException {
        try {
            DocumentBuilder parser = criarFabrica().newDocumentBuilder();
            parser.setErrorHandler(SILENCIOSO);
            return parser.parse(new ByteArrayInputStream(xml));
        } catch (SAXParseException e) {
            String posicao = "linha %d, coluna %d".formatted(e.getLineNumber(), e.getColumnNumber());
            throw new XmlMalFormadoException(ocorrencia(posicao), e);
        } catch (SAXException | IOException e) {
            throw new XmlMalFormadoException(ocorrencia(null), e);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Parser XML mal configurado", e);
        }
    }

    private static Ocorrencia ocorrencia(String posicao) {
        return new Ocorrencia(CODIGO, Camada.ESTRUTURA, null, posicao, null, null);
    }

    private static DocumentBuilderFactory criarFabrica() throws ParserConfigurationException {
        DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setNamespaceAware(true);
        fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        fabrica.setFeature("http://xml.org/sax/features/external-general-entities", false);
        fabrica.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        fabrica.setXIncludeAware(false);
        fabrica.setExpandEntityReferences(false);
        return fabrica;
    }

    private static final ErrorHandler SILENCIOSO = new ErrorHandler() {
        @Override
        public void warning(SAXParseException e) {
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    };
}
