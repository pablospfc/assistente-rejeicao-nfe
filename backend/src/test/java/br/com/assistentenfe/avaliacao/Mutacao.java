package br.com.assistentenfe.avaliacao;

import java.util.Optional;

/**
 * Uma mutação controlada do catálogo de avaliação.
 *
 * @param id            identificador estável, ex.: "M502-nNF"
 * @param codigo        código de rejeição esperado
 * @param motivo        motivo esperado (225 e 297) ou {@code null}
 * @param descricao     texto curto que vai para o Quadro 1 do artigo
 * @param xpath         XPath esperado da ocorrência; {@code {det}} vira "det" ou "det[1]" conforme a base,
 *                      e {@code {icms}} vira o grupo de ICMS do 1º item; {@code null} para o 243 (sem XPath)
 * @param assinatura    momento da assinatura em relação à mutação
 * @param isolamento    ajuste feito para isolar o código esperado, como faria um emissor real, ou {@code null}
 * @param aplicacao     a mutação; devolve vazio quando não se aplica à base
 */
public record Mutacao(String id, String codigo, String motivo, String descricao, String xpath,
                      Assinatura assinatura, String isolamento, Aplicacao aplicacao) {

    public enum Assinatura {
        /** Muta e depois reassina (padrão, D006/D022). */
        REASSINAR,
        /** Muta o XML já assinado, sem reassinar (297, e 243, que não pode ser assinado). */
        APOS_ASSINAR
    }

    @FunctionalInterface
    public interface Aplicacao {
        Optional<String> aplicar(String xmlAssinado);
    }
}
