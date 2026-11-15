package br.com.assistentenfe.estrutura;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Monta o XPath (por nomes locais) de um elemento do DOM, ex.:
 * {@code /NFe/infNFe/det[2]/prod/vProd}. O índice só aparece quando há irmãos de mesmo nome.
 */
public final class CaminhoXPath {

    private CaminhoXPath() {
    }

    public static String de(Node no) {
        if (!(no instanceof Element elemento)) {
            return null;
        }
        StringBuilder caminho = new StringBuilder();
        for (Element e = elemento; e != null; e = pai(e)) {
            caminho.insert(0, "/" + nome(e) + indice(e));
        }
        return caminho.toString();
    }

    private static String indice(Element e) {
        int posicao = 1;
        int total = 0;
        Node pai = e.getParentNode();
        if (pai == null) {
            return "";
        }
        for (Node n = pai.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element irmao && nome(irmao).equals(nome(e))) {
                total++;
                if (n == e) {
                    posicao = total;
                }
            }
        }
        return total > 1 ? "[" + posicao + "]" : "";
    }

    private static String nome(Element e) {
        return e.getLocalName() != null ? e.getLocalName() : e.getNodeName();
    }

    private static Element pai(Element e) {
        return e.getParentNode() instanceof Element p ? p : null;
    }
}
