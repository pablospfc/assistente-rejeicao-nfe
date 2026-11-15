package br.com.assistentenfe.estrutura;

import br.com.assistentenfe.dominio.Ocorrencia;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ValidadorSchema {

    private static final String XSD_NFE = "/xsd/nfe_v4.00.xsd";

    private static final String NO_ATUAL = "http://apache.org/xml/properties/dom/current-element-node";
    private static final String IDIOMA = "http://apache.org/xml/properties/locale";

    private final Schema schema;
    private final TradutorErroSchema tradutor = new TradutorErroSchema();

    public ValidadorSchema(URL xsdPrincipal) {
        this.schema = carregar(xsdPrincipal);
    }

    public static ValidadorSchema nfe400() {
        return new ValidadorSchema(ValidadorSchema.class.getResource(XSD_NFE));
    }

    public List<Ocorrencia> validar(Document documento) {
        Validator validator = schema.newValidator();
        Coletor coletor = new Coletor(validator);
        try {
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            validator.setProperty(IDIOMA, Locale.ENGLISH);
            validator.setErrorHandler(coletor);
            validator.validate(new DOMSource(documento));
        } catch (SAXException | IOException e) {
        }
        return coletor.ocorrencias;
    }

    private static Schema carregar(URL xsd) {
        try {
            SchemaFactory fabrica = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            fabrica.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            fabrica.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "file,jar:file,nested");
            fabrica.setErrorHandler(AVISO_TAMBEM_EH_ERRO);
            return fabrica.newSchema(xsd);
        } catch (SAXException e) {
            throw new IllegalStateException("Não foi possível carregar o schema " + xsd, e);
        }
    }

    private final class Coletor implements ErrorHandler {

        private final Validator validator;
        private final List<Ocorrencia> ocorrencias = new ArrayList<>();

        Coletor(Validator validator) {
            this.validator = validator;
        }

        @Override
        public void warning(SAXParseException e) {
        }

        @Override
        public void error(SAXParseException e) {
            registrar(e.getMessage());
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            registrar(e.getMessage());
            throw e;
        }

        private void registrar(String mensagem) {
            String xpath = CaminhoXPath.de(noAtual());
            Ocorrencia anterior = ocorrencias.isEmpty() ? null : ocorrencias.getLast();
            boolean mesmoElemento = anterior != null && xpath != null && xpath.equals(anterior.xpath());

            String atributo = tradutor.atributoCitado(mensagem);
            if (atributo != null && mesmoElemento) {
                ocorrencias.set(ocorrencias.size() - 1, comXpath(anterior, xpath + "/@" + atributo));
            } else if (!(tradutor.ehRepeticao(mensagem) && mesmoElemento)) {
                ocorrencias.add(tradutor.traduzir(mensagem, xpath));
            }
        }

        private Node noAtual() {
            try {
                return (Node) validator.getProperty(NO_ATUAL);
            } catch (SAXException e) {
                return null;
            }
        }
    }

    private static final ErrorHandler AVISO_TAMBEM_EH_ERRO = new ErrorHandler() {
        @Override
        public void warning(SAXParseException e) throws SAXException {
            throw e;
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

    private static Ocorrencia comXpath(Ocorrencia o, String xpath) {
        return new Ocorrencia(o.codigo(), o.camada(), xpath, o.valorEncontrado(), o.valorEsperado(), o.motivo());
    }
}
