package br.com.assistentenfe.dominio;

import java.util.List;

public record ResultadoValidacao(List<Ocorrencia> ocorrencias) {

    public ResultadoValidacao {
        ocorrencias = List.copyOf(ocorrencias);
    }

    public boolean valida() {
        return ocorrencias.isEmpty();
    }

    public boolean regrasApesarDeFalhaNoSchema() {
        boolean falhaNoSchema = ocorrencias.stream().anyMatch(o -> "225".equals(o.codigo()));
        boolean temRegras = ocorrencias.stream().anyMatch(o -> o.camada() == Camada.REGRAS_NEGOCIO);
        return falhaNoSchema && temRegras;
    }
}
