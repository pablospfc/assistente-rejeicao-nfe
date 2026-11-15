package br.com.assistentenfe.estrutura;

import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.Ocorrencia;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rejeição 404: a NF-e não admite prefixos de namespace nos elementos
 * (ex.: {@code <nfe:NFe>} ou {@code <ds:Signature>}). Gera uma ocorrência por
 * prefixo usado, apontando o primeiro elemento em que ele aparece.
 */
public final class VerificadorNamespace {

    public static final String CODIGO = "404";

    public List<Ocorrencia> verificar(Document documento) {
        Map<String, Element> primeiroUsoPorPrefixo = new LinkedHashMap<>();
        percorrer(documento.getDocumentElement(), primeiroUsoPorPrefixo);

        List<Ocorrencia> ocorrencias = new ArrayList<>();
        primeiroUsoPorPrefixo.forEach((prefixo, elemento) -> ocorrencias.add(new Ocorrencia(
                CODIGO, Camada.ESTRUTURA, CaminhoXPath.de(elemento),
                elemento.getNodeName(), elemento.getLocalName(), null)));
        return ocorrencias;
    }

    private static void percorrer(Element elemento, Map<String, Element> primeiroUso) {
        if (elemento.getPrefix() != null) {
            primeiroUso.putIfAbsent(elemento.getPrefix(), elemento);
        }
        for (Node n = elemento.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element filho) {
                percorrer(filho, primeiroUso);
            }
        }
    }
}
