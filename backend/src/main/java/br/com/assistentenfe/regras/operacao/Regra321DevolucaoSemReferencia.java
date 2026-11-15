package br.com.assistentenfe.regras.operacao;

import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.NFe;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.regras.RegraValidacao;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Set;

/**
 * 321: NF-e de devolução ({@code finNFe = 4}, modelo 55) deve referenciar o documento
 * fiscal devolvido (MOC 7.0, Anexo I, regra de B25). Interpretações do autor (D017):
 * <ul>
 *   <li>a exceção vale se <em>qualquer</em> item tiver CFOP 1.201, 1.202, 1.410, 1.411, 5.921 ou 6.921;</li>
 *   <li>contam refNFe, refNFeSig, refNF, refNFP e refECF; refCTe não conta.</li>
 * </ul>
 */
public final class Regra321DevolucaoSemReferencia implements RegraValidacao {

    private static final Set<String> CFOP_EXCECAO = Set.of("1201", "1202", "1410", "1411", "5921", "6921");
    private static final Set<String> REFERENCIAS_ACEITAS = Set.of("refNFe", "refNFeSig", "refNF", "refNFP", "refECF");

    @Override
    public String codigoRejeicao() {
        return "321";
    }

    @Override
    public List<Ocorrencia> validar(NFe nfe) {
        boolean devolucao = nfe.texto("/NFe/infNFe/ide/finNFe").map("4"::equals).orElse(false);
        boolean modelo55 = nfe.texto("/NFe/infNFe/ide/mod").map("55"::equals).orElse(true);
        if (!devolucao || !modelo55 || temCfopDeExcecao(nfe) || temReferenciaAceita(nfe)) {
            return List.of();
        }
        return List.of(new Ocorrencia(codigoRejeicao(), Camada.REGRAS_NEGOCIO,
                "/NFe/infNFe/ide/finNFe", "4", null, null));
    }

    private static boolean temCfopDeExcecao(NFe nfe) {
        return nfe.itens().stream()
                .anyMatch(item -> item.texto("prod/CFOP").map(CFOP_EXCECAO::contains).orElse(false));
    }

    /** Algum NFref com um tipo de documento aceito (cada NFref tem exatamente um filho). */
    private static boolean temReferenciaAceita(NFe nfe) {
        for (Element nfRef : nfe.filhos("/NFe/infNFe/ide", "NFref")) {
            for (Node n = nfRef.getFirstChild(); n != null; n = n.getNextSibling()) {
                if (n instanceof Element tipo && REFERENCIAS_ACEITAS.contains(tipo.getLocalName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
