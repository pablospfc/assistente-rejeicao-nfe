package br.com.assistentenfe.api;

import br.com.assistentenfe.dominio.Camada;

import java.util.List;
import java.util.Map;

public record DiagnosticoResposta(
        boolean valida,
        boolean regrasApesarDeFalhaNoSchema,
        Map<Camada, List<OcorrenciaResposta>> camadas) {
}
