package br.com.assistentenfe.dominio;

import java.util.List;

/** Resultado de uma validação: a NF-e é válida quando não há ocorrências. */
public record ResultadoValidacao(List<Ocorrencia> ocorrencias) {

    public ResultadoValidacao {
        ocorrencias = List.copyOf(ocorrencias);
    }

    public boolean valida() {
        return ocorrencias.isEmpty();
    }

    /**
     * Indica que há ocorrências de regras de negócio num XML que também falhou
     * no schema (225): elas foram avaliadas sobre um documento estruturalmente inválido.
     */
    public boolean regrasApesarDeFalhaNoSchema() {
        boolean falhaNoSchema = ocorrencias.stream().anyMatch(o -> "225".equals(o.codigo()));
        boolean temRegras = ocorrencias.stream().anyMatch(o -> o.camada() == Camada.REGRAS_NEGOCIO);
        return falhaNoSchema && temRegras;
    }
}
