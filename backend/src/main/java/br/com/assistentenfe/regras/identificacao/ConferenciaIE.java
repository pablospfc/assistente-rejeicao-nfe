package br.com.assistentenfe.regras.identificacao;

import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.NFe;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.regras.identificacao.ie.EstrategiasIE;

import java.util.List;
import java.util.Optional;

/**
 * Conferência comum às rejeições 209 e 210: escolhe a estratégia pela UF do
 * endereço e aplica à IE. Sem IE, sem UF ou sem estratégia para a UF, a regra é ignorada.
 * O valor esperado da ocorrência é a UF cuja regra foi aplicada.
 */
final class ConferenciaIE {

    private ConferenciaIE() {
    }

    static List<Ocorrencia> conferir(NFe nfe, String codigo, String caminhoIE, String caminhoUF) {
        Optional<String> ie = nfe.texto(caminhoIE).filter(v -> !v.equals("ISENTO"));
        Optional<String> uf = nfe.texto(caminhoUF);
        if (ie.isEmpty() || uf.isEmpty()) {
            return List.of();
        }
        return EstrategiasIE.para(uf.get())
                .filter(estrategia -> !estrategia.valida(ie.get()))
                .map(estrategia -> List.of(new Ocorrencia(codigo, Camada.REGRAS_NEGOCIO,
                        caminhoIE, ie.get(), uf.get(), null)))
                .orElse(List.of());
    }
}
