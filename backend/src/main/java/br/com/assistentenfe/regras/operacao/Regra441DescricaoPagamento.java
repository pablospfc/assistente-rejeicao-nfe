package br.com.assistentenfe.regras.operacao;

import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.NFe;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.regras.RegraValidacao;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * 441: quando o meio de pagamento é 99 (Outros), a descrição {@code xPag} é
 * obrigatória (NT 2020.006 v1.30, regra YA02a-10, modelos 55 e 65). O XSD não
 * pega esse caso, porque {@code xPag} é opcional. Gera uma ocorrência por detPag.
 */
public final class Regra441DescricaoPagamento implements RegraValidacao {

    @Override
    public String codigoRejeicao() {
        return "441";
    }

    @Override
    public List<Ocorrencia> validar(NFe nfe) {
        List<Element> pagamentos = nfe.filhos("/NFe/infNFe/pag", "detPag");
        List<Ocorrencia> ocorrencias = new ArrayList<>();
        for (int i = 0; i < pagamentos.size(); i++) {
            Element detPag = pagamentos.get(i);
            String tPag = filho(detPag, "tPag");
            String xPag = filho(detPag, "xPag");
            if ("99".equals(tPag) && (xPag == null || xPag.isBlank())) {
                String indice = pagamentos.size() > 1 ? "[" + (i + 1) + "]" : "";
                ocorrencias.add(new Ocorrencia(codigoRejeicao(), Camada.REGRAS_NEGOCIO,
                        "/NFe/infNFe/pag/detPag" + indice + "/tPag", tPag, null, null));
            }
        }
        return ocorrencias;
    }

    private static String filho(Element pai, String nome) {
        for (Node n = pai.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element e && nome.equals(e.getLocalName())) {
                return e.getTextContent();
            }
        }
        return null;
    }
}
