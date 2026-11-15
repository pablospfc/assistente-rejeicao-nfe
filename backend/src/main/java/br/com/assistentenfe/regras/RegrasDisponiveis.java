package br.com.assistentenfe.regras;

import br.com.assistentenfe.dominio.TabelaCfop;
import br.com.assistentenfe.regras.chave.Regra253DigitoDaChave;
import br.com.assistentenfe.regras.chave.Regra502IdDaChave;
import br.com.assistentenfe.regras.identificacao.Regra207CnpjEmitente;
import br.com.assistentenfe.regras.identificacao.Regra208CnpjDestinatario;
import br.com.assistentenfe.regras.identificacao.Regra209IeEmitente;
import br.com.assistentenfe.regras.identificacao.Regra210IeDestinatario;
import br.com.assistentenfe.regras.identificacao.Regra237CpfDestinatario;
import br.com.assistentenfe.regras.identificacao.Regra703DataEmissao;
import br.com.assistentenfe.regras.operacao.Regra321DevolucaoSemReferencia;
import br.com.assistentenfe.regras.operacao.Regra327CfopNaDevolucao;
import br.com.assistentenfe.regras.operacao.Regra328CfopDeDevolucaoForaDaDevolucao;
import br.com.assistentenfe.regras.operacao.Regra441DescricaoPagamento;
import br.com.assistentenfe.regras.operacao.Regra598RazaoSocialHomologacao;
import br.com.assistentenfe.regras.totais.Regra528IcmsDoItem;
import br.com.assistentenfe.regras.totais.Regra531TotalBcIcms;
import br.com.assistentenfe.regras.totais.Regra532TotalIcms;
import br.com.assistentenfe.regras.totais.Regra533TotalBcIcmsSt;
import br.com.assistentenfe.regras.totais.Regra534TotalIcmsSt;
import br.com.assistentenfe.regras.totais.Regra535TotalFrete;
import br.com.assistentenfe.regras.totais.Regra536TotalSeguro;
import br.com.assistentenfe.regras.totais.Regra537TotalDesconto;
import br.com.assistentenfe.regras.totais.Regra538TotalIpi;
import br.com.assistentenfe.regras.totais.Regra564TotalProdutos;
import br.com.assistentenfe.regras.totais.Regra610TotalNF;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

public final class RegrasDisponiveis {

    private RegrasDisponiveis() {
    }

    public static List<RegraValidacao> todas(Clock relogio, Duration toleranciaDhEmi) {
        TabelaCfop tabelaCfop = TabelaCfop.carregar();
        return List.of(
                new Regra502IdDaChave(),
                new Regra253DigitoDaChave(),
                new Regra703DataEmissao(relogio, toleranciaDhEmi),
                new Regra207CnpjEmitente(),
                new Regra208CnpjDestinatario(),
                new Regra237CpfDestinatario(),
                new Regra209IeEmitente(),
                new Regra210IeDestinatario(),
                new Regra564TotalProdutos(),
                new Regra531TotalBcIcms(),
                new Regra532TotalIcms(),
                new Regra528IcmsDoItem(),
                new Regra533TotalBcIcmsSt(),
                new Regra534TotalIcmsSt(),
                new Regra535TotalFrete(),
                new Regra536TotalSeguro(),
                new Regra537TotalDesconto(),
                new Regra538TotalIpi(),
                new Regra610TotalNF(),
                new Regra321DevolucaoSemReferencia(),
                new Regra327CfopNaDevolucao(tabelaCfop),
                new Regra328CfopDeDevolucaoForaDaDevolucao(tabelaCfop),
                new Regra598RazaoSocialHomologacao(),
                new Regra441DescricaoPagamento());
    }
}
