package br.com.assistentenfe.api;

import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.explicacao.Explicacao;

/** Uma ocorrência com sua explicação, como o frontend a recebe. */
public record OcorrenciaResposta(
        String codigo,
        String xpath,
        String valorEncontrado,
        String valorEsperado,
        String titulo,
        String campo,
        String problema,
        String correcao) {

    static OcorrenciaResposta de(Ocorrencia o, Explicacao e) {
        return new OcorrenciaResposta(o.codigo(), o.xpath(), o.valorEncontrado(), o.valorEsperado(),
                e.titulo(), e.campo(), e.problema(), e.correcao());
    }
}
