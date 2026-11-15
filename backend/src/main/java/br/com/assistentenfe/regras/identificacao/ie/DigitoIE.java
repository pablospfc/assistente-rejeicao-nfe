package br.com.assistentenfe.regras.identificacao.ie;

/** Estratégia de conferência da Inscrição Estadual de uma UF (padrão Strategy). */
public interface DigitoIE {

    /** Sigla da UF, ex.: "MA". */
    String uf();

    /** Número de dígitos da IE nessa UF. */
    int tamanho();

    /** Confere formato e dígitos de uma IE já normalizada para {@link #tamanho()} dígitos. */
    boolean confere(String ie);

    /**
     * Verdadeiro se a IE é válida para a UF. Antes da conferência, o tamanho é
     * normalizado "desprezando os zeros não significativos" (MOC 7.0, Anexo I,
     * nota (*2)): os zeros à esquerda são retirados e a IE é completada com zeros
     * até o tamanho da UF.
     */
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
