package br.com.assistentenfe.dominio;

/**
 * Uma rejeição encontrada no XML. Traz apenas dados; o texto explicativo
 * vem do catálogo (pacote {@code explicacao}).
 *
 * @param codigo           código de rejeição da SEFAZ, ex.: "225"
 * @param camada           camada que detectou o problema
 * @param xpath            caminho do campo afetado, ou {@code null} se não se aplica
 * @param valorEncontrado  valor presente no XML (ou posição, no caso de 243)
 * @param valorEsperado    valor ou formato esperado, quando conhecido
 * @param motivo           chave que refina a explicação no catálogo, ou {@code null}
 */
public record Ocorrencia(
        String codigo,
        Camada camada,
        String xpath,
        String valorEncontrado,
        String valorEsperado,
        String motivo) {
}
