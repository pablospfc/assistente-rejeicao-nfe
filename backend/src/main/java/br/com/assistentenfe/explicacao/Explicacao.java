package br.com.assistentenfe.explicacao;

public record Explicacao(String titulo, String campo, String problema, String correcao) {

    public boolean completa() {
        return preenchido(campo) && preenchido(problema) && preenchido(correcao);
    }

    private static boolean preenchido(String texto) {
        return texto != null && !texto.isBlank();
    }
}
