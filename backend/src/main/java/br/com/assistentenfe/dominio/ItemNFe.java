package br.com.assistentenfe.dominio;

import org.w3c.dom.Element;

import java.util.Optional;

/**
 * Um item ({@code det}) da NF-e. Os caminhos são relativos ao {@code det},
 * ex.: {@code texto("prod/vProd")}.
 *
 * @param posicao posição do item na NF-e, a partir de 1
 * @param total   número de itens, usado para montar o XPath com índice só quando há mais de um
 */
public record ItemNFe(int posicao, int total, Element det) {

    public Optional<String> texto(String relativo) {
        return Caminho.descer(det, relativo).map(Element::getTextContent);
    }

    /** Grupo de ICMS do item (ICMS00, ICMS10, ..., ICMSSN900), filho único de {@code imposto/ICMS}. */
    public Optional<Element> grupoIcms() {
        return Caminho.descer(det, "imposto/ICMS").flatMap(Caminho::primeiroFilho);
    }

    /** Campo dentro do grupo de ICMS, qualquer que seja o grupo, ex.: {@code noGrupoIcms("vBC")}. */
    public Optional<String> noGrupoIcms(String campo) {
        return grupoIcms().flatMap(g -> Caminho.descer(g, campo)).map(Element::getTextContent);
    }

    /** XPath de um campo do item, ex.: /NFe/infNFe/det[2]/prod/vProd. */
    public String xpath(String relativo) {
        String det = total > 1 ? "det[" + posicao + "]" : "det";
        return "/NFe/infNFe/" + det + "/" + relativo;
    }

    /** XPath de um campo dentro do grupo de ICMS, ex.: /NFe/infNFe/det/imposto/ICMS/ICMS00/vICMS. */
    public String xpathNoGrupoIcms(String campo) {
        String grupo = grupoIcms().map(Element::getLocalName).orElse("ICMS");
        return xpath("imposto/ICMS/" + grupo + "/" + campo);
    }
}
