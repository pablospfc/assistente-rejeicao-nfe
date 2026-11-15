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

public final class Regra327CfopNaDevolucao implements RegraValidacao {

    private static final Set<String> CFOP_NAO_CONTRIBUINTE = Set.of("1949", "2949");

    private final TabelaCfop tabela;

    public Regra327CfopNaDevolucao(TabelaCfop tabela) {
        this.tabela = tabela;
    }

    @Override
    public String codigoRejeicao() {
        return "327";
    }

    @Override
    public List<Ocorrencia> validar(NFe nfe) {
        boolean devolucao = nfe.texto("/NFe/infNFe/ide/finNFe").map("4"::equals).orElse(false);
        boolean modelo55 = nfe.texto("/NFe/infNFe/ide/mod").map("55"::equals).orElse(true);
        if (!devolucao || !modelo55) {
            return List.of();
        }
        boolean naoContribuinte = nfe.texto("/NFe/infNFe/dest/indIEDest").map("9"::equals).orElse(false);

        List<Ocorrencia> ocorrencias = new ArrayList<>();
        for (ItemNFe item : nfe.itens()) {
            Optional<String> cfop = item.texto("prod/CFOP");
            if (cfop.isEmpty() || tabela.devolucao(cfop.get())
                    || (naoContribuinte && CFOP_NAO_CONTRIBUINTE.contains(cfop.get()))) {
                continue;
            }
            ocorrencias.add(new Ocorrencia(codigoRejeicao(), Camada.REGRAS_NEGOCIO,
                    item.xpath("prod/CFOP"), cfop.get(), null, null));
        }
        return ocorrencias;
    }
}
