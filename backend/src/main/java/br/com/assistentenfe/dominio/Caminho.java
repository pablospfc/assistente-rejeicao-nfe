package br.com.assistentenfe.dominio;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Navegação no DOM por nomes locais, ignorando namespaces. */
final class Caminho {

    private Caminho() {
    }

    /** Desce pelo caminho relativo (ex.: "prod/vProd"), sempre pelo primeiro filho de cada nome. */
    static Optional<Element> descer(Element origem, String relativo) {
        Element atual = origem;
        for (String parte : relativo.split("/")) {
            if (atual == null) {
                break;
            }
            atual = filhos(atual, parte).stream().findFirst().orElse(null);
        }
        return Optional.ofNullable(atual);
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
}
