package br.com.assistentenfe.explicacao;

/** Texto exibido ao usuário para uma ocorrência. */
public record Explicacao(String titulo, String campo, String problema, String correcao) {

    /** Completa quando informa campo, problema e correção (critério do relatório de avaliação). */
    public boolean completa() {
        return preenchido(campo) && preenchido(problema) && preenchido(correcao);
    }

    private static boolean preenchido(String texto) {
        return texto != null && !texto.isBlank();
    }
}
