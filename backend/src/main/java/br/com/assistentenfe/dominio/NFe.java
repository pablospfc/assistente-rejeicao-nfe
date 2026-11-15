package br.com.assistentenfe.dominio;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class NFe {

    private final Document documento;

    public NFe(Document documento) {
        this.documento = documento;
    }

    public Optional<String> texto(String caminho) {
        return elemento(caminho).map(Element::getTextContent);
    }

    public Optional<BigDecimal> decimal(String caminho) {
        return texto(caminho).flatMap(Valor::ler);
    }

    public Optional<String> atributo(String caminhoElemento, String nome) {
        return elemento(caminhoElemento)
                .filter(e -> e.hasAttribute(nome))
                .map(e -> e.getAttribute(nome));
    }

    public List<ItemNFe> itens() {
        List<Element> dets = elemento("/NFe/infNFe").map(i -> Caminho.filhos(i, "det")).orElse(List.of());
        List<ItemNFe> itens = new ArrayList<>();
        for (int i = 0; i < dets.size(); i++) {
            itens.add(new ItemNFe(i + 1, dets.size(), dets.get(i)));
        }
        return itens;
    }

    public List<Element> filhos(String caminhoPai, String nomeLocal) {
        return elemento(caminhoPai).map(p -> Caminho.filhos(p, nomeLocal)).orElse(List.of());
    }

    private Optional<Element> elemento(String caminho) {
        Element raiz = documento.getDocumentElement();
        String[] partes = caminho.substring(1).split("/", 2);
        if (raiz == null || !partes[0].equals(raiz.getLocalName())) {
            return Optional.empty();
        }
        return partes.length == 1 ? Optional.of(raiz) : Caminho.descer(raiz, partes[1]);
    }
}
