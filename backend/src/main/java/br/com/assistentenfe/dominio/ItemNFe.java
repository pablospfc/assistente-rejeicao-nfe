package br.com.assistentenfe.dominio;

import org.w3c.dom.Element;

import java.util.Optional;

public record ItemNFe(int posicao, int total, Element det) {

    public Optional<String> texto(String relativo) {
        return Caminho.descer(det, relativo).map(Element::getTextContent);
    }

    public Optional<Element> grupoIcms() {
        return Caminho.descer(det, "imposto/ICMS").flatMap(Caminho::primeiroFilho);
    }

    public Optional<String> noGrupoIcms(String campo) {
        return grupoIcms().flatMap(g -> Caminho.descer(g, campo)).map(Element::getTextContent);
    }

    public String xpath(String relativo) {
        String det = total > 1 ? "det[" + posicao + "]" : "det";
        return "/NFe/infNFe/" + det + "/" + relativo;
    }

    public String xpathNoGrupoIcms(String campo) {
        String grupo = grupoIcms().map(Element::getLocalName).orElse("ICMS");
        return xpath("imposto/ICMS/" + grupo + "/" + campo);
    }
}
