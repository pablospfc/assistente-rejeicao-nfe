package br.com.assistentenfe.avaliacao;

import br.com.assistentenfe.dominio.ChaveAcesso;
import br.com.assistentenfe.dominio.NFe;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class Dom {

    static final String NS_NFE = "http://www.portalfiscal.inf.br/nfe";

    private Dom() {
    }

    static Document ler(String xml) {
        try {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setNamespaceAware(true);
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return fabrica.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalArgumentException("XML de base ilegível", e);
        }
    }

    static String escrever(Document documento) {
        try {
            TransformerFactory fabrica = TransformerFactory.newInstance();
            fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            var transformador = fabrica.newTransformer();
            transformador.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            StringWriter saida = new StringWriter();
            transformador.transform(new DOMSource(documento), new StreamResult(saida));
            return saida.toString().replace("\r\n", "\n");
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível escrever o XML", e);
        }
    }

    static Optional<Element> elemento(Document documento, String caminho) {
        String[] partes = caminho.substring(1).split("/");
        Element atual = documento.getDocumentElement();
        if (!partes[0].equals(atual.getLocalName())) {
            return Optional.empty();
        }
        for (int i = 1; i < partes.length && atual != null; i++) {
            atual = filhos(atual, partes[i]).stream().findFirst().orElse(null);
        }
        return Optional.ofNullable(atual);
    }

    static Optional<Element> doPrimeiroItem(Document documento, String relativo) {
        return elemento(documento, "/NFe/infNFe/det/" + relativo);
    }

    static Optional<Element> grupoIcmsDoPrimeiroItem(Document documento) {
        return doPrimeiroItem(documento, "imposto/ICMS").flatMap(Dom::primeiroFilho);
    }

    static List<Element> filhos(Element pai, String nomeLocal) {
        List<Element> encontrados = new ArrayList<>();
        for (Node n = pai.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element e && nomeLocal.equals(e.getLocalName())) {
                encontrados.add(e);
            }
        }
        return encontrados;
    }

    static Optional<Element> primeiroFilho(Element pai) {
        for (Node n = pai.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element e) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    static void somar(Element campo, String valor) {
        campo.setTextContent(new BigDecimal(campo.getTextContent()).add(new BigDecimal(valor)).toPlainString());
    }

    static void remover(Element elemento) {
        Node anterior = elemento.getPreviousSibling();
        elemento.getParentNode().removeChild(elemento);
        if (anterior != null && anterior.getNodeType() == Node.TEXT_NODE && anterior.getTextContent().isBlank()) {
            anterior.getParentNode().removeChild(anterior);
        }
    }

    static String outroUltimoDigito(String valor) {
        char ultimo = valor.charAt(valor.length() - 1);
        return valor.substring(0, valor.length() - 1) + (char) ('0' + (ultimo - '0' + 1) % 10);
    }

    static void recalcularCdv(Document documento) {
        ChaveAcesso chave = ChaveAcesso.compor(new NFe(documento)).orElseThrow();
        elemento(documento, "/NFe/infNFe/ide/cDV").orElseThrow().setTextContent(String.valueOf(chave.digitoCalculado()));
    }

    static void recomporChave(Document documento) {
        ChaveAcesso chave = ChaveAcesso.compor(new NFe(documento)).orElseThrow();
        String dv = String.valueOf(chave.digitoCalculado());
        elemento(documento, "/NFe/infNFe/ide/cDV").orElseThrow().setTextContent(dv);
        elemento(documento, "/NFe/infNFe").orElseThrow().setAttribute("Id", "NFe" + chave.semDigito() + dv);
    }
}
