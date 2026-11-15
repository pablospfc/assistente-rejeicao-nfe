package br.com.assistentenfe.api;

import br.com.assistentenfe.dominio.Camada;

import java.util.List;
import java.util.Map;

/**
 * Resposta da validação: ocorrências agrupadas por camada, na ordem do fluxo.
 *
 * @param regrasApesarDeFalhaNoSchema verdadeiro quando há ocorrências de regras de negócio
 *                                    encontradas num XML que também falhou no schema (225)
 */
public record DiagnosticoResposta(
        boolean valida,
        boolean regrasApesarDeFalhaNoSchema,
        Map<Camada, List<OcorrenciaResposta>> camadas) {
}
