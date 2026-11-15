package br.com.assistentenfe.regras.operacao;

import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.NFe;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.regras.RegraValidacao;

import java.util.List;

/**
 * 598: em homologação ({@code tpAmb = 2}), a razão social do destinatário deve ser
 * o texto padrão (MOC 7.0, Anexo I, E04; NT 2011/002). Substitui o 373, que é só da NFC-e (D016).
 */
public final class Regra598RazaoSocialHomologacao implements RegraValidacao {

    static final String TEXTO_HOMOLOGACAO = "NF-E EMITIDA EM AMBIENTE DE HOMOLOGACAO - SEM VALOR FISCAL";
    private static final String CAMPO = "/NFe/infNFe/dest/xNome";

    @Override
    public String codigoRejeicao() {
        return "598";
    }

    @Override
    public List<Ocorrencia> validar(NFe nfe) {
        boolean homologacao = nfe.texto("/NFe/infNFe/ide/tpAmb").map("2"::equals).orElse(false);
        if (!homologacao) {
            return List.of();
        }
        return nfe.texto(CAMPO)
                .filter(xNome -> !xNome.equals(TEXTO_HOMOLOGACAO))
                .map(xNome -> List.of(new Ocorrencia(codigoRejeicao(), Camada.REGRAS_NEGOCIO,
                        CAMPO, xNome, TEXTO_HOMOLOGACAO, null)))
                .orElse(List.of());
    }
}
