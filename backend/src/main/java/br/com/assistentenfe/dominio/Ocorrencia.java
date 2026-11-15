package br.com.assistentenfe.dominio;

public record Ocorrencia(
        String codigo,
        Camada camada,
        String xpath,
        String valorEncontrado,
        String valorEsperado,
        String motivo) {
}
