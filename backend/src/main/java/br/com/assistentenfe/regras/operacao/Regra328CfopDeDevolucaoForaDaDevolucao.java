package br.com.assistentenfe.regras.operacao;

import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.ItemNFe;
import br.com.assistentenfe.dominio.NFe;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.dominio.TabelaCfop;
import br.com.assistentenfe.regras.RegraValidacao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 328: NF-e que não é de devolução nem complementar ({@code finNFe} diferente de 2 e 4,
 * modelo 55) não pode ter item com CFOP de devolução ({@code indDevol = 1} na Tabela
 * CFOP; MOC 7.0, Anexo I, I08-144). Gera uma ocorrência por item.
 */
public final class Regra328CfopDeDevolucaoForaDaDevolucao implements RegraValidacao {

    private static final Set<String> FINALIDADES_QUE_ACEITAM = Set.of("2", "4");

    private final TabelaCfop tabela;

    public Regra328CfopDeDevolucaoForaDaDevolucao(TabelaCfop tabela) {
        this.tabela = tabela;
    }

    @Override
    public String codigoRejeicao() {
        return "328";
    }

    @Override
    public List<Ocorrencia> validar(NFe nfe) {
        Optional<String> finNFe = nfe.texto("/NFe/infNFe/ide/finNFe");
        boolean modelo55 = nfe.texto("/NFe/infNFe/ide/mod").map("55"::equals).orElse(true);
        if (finNFe.isEmpty() || FINALIDADES_QUE_ACEITAM.contains(finNFe.get()) || !modelo55) {
            return List.of();
        }

        List<Ocorrencia> ocorrencias = new ArrayList<>();
        for (ItemNFe item : nfe.itens()) {
            Optional<String> cfop = item.texto("prod/CFOP");
            if (cfop.isPresent() && tabela.devolucao(cfop.get())) {
                ocorrencias.add(new Ocorrencia(codigoRejeicao(), Camada.REGRAS_NEGOCIO,
                        item.xpath("prod/CFOP"), cfop.get(), null, null));
            }
        }
        return ocorrencias;
    }
}
