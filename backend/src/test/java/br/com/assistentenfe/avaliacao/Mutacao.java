package br.com.assistentenfe.avaliacao;

import java.util.Optional;

public record Mutacao(String id, String codigo, String motivo, String descricao, String xpath,
                      Assinatura assinatura, String isolamento, Aplicacao aplicacao) {

    public enum Assinatura {
        REASSINAR,
        APOS_ASSINAR
    }

    @FunctionalInterface
    public interface Aplicacao {
        Optional<String> aplicar(String xmlAssinado);
    }
}
