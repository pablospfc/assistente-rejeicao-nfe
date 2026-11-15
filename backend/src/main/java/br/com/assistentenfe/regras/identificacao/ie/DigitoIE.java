package br.com.assistentenfe.regras.identificacao.ie;

public interface DigitoIE {

    String uf();

    int tamanho();

    boolean confere(String ie);

    default boolean valida(String ie) {
        if (!ie.matches("\\d+")) {
            return false;
        }
        String semZeros = ie.replaceFirst("^0+", "");
        if (semZeros.length() > tamanho()) {
            return false;
        }
        return confere("0".repeat(tamanho() - semZeros.length()) + semZeros);
    }
}
