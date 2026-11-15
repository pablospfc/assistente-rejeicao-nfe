package br.com.assistentenfe.dominio;

/**
 * Cálculos de dígito verificador por módulo 11 usados na chave de acesso,
 * no CNPJ (numérico e alfanumérico) e no CPF.
 *
 * <p>Cada caractere vale seu código ASCII menos 48: os algarismos valem 0 a 9 e
 * as letras do CNPJ alfanumérico valem 17 ('A') a 42 ('Z'), como define a Receita
 * Federal. Para entradas só com algarismos, o cálculo é o tradicional.
 */
public final class DigitoVerificador {

    private static final String FORMATO_CNPJ = "[0-9A-Z]{12}[0-9]{2}";
    private static final String FORMATO_CPF = "[0-9]{11}";

    private DigitoVerificador() {
    }

    /**
     * Módulo 11 com pesos de 2 a {@code pesoMaximo}, aplicados da direita para a
     * esquerda e reiniciados em 2 após o máximo. Resto 0 ou 1 resulta em dígito 0;
     * nos demais casos, o dígito é 11 − resto.
     */
    public static int modulo11(String numero, int pesoMaximo) {
        int soma = 0;
        int peso = 2;
        for (int i = numero.length() - 1; i >= 0; i--) {
            soma += valor(numero.charAt(i)) * peso;
            peso = peso == pesoMaximo ? 2 : peso + 1;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    /** Dígito da chave de acesso: módulo 11 com pesos 2 a 9 sobre as 43 posições. */
    public static int chaveAcesso(String chaveSemDigito) {
        return modulo11(chaveSemDigito, 9);
    }

    /** CNPJ numérico ou alfanumérico: dois dígitos por módulo 11, pesos 2 a 9; zerado é inválido. */
    public static boolean cnpjValido(String cnpj) {
        if (cnpj == null || !cnpj.matches(FORMATO_CNPJ) || cnpj.equals("00000000000000")) {
            return false;
        }
        String base = cnpj.substring(0, 12);
        int dv1 = modulo11(base, 9);
        int dv2 = modulo11(base + dv1, 9);
        return cnpj.endsWith("" + dv1 + dv2);
    }

    /** CPF: dois dígitos por módulo 11 (pesos crescentes, sem reinício); dígitos todos iguais é inválido. */
    public static boolean cpfValido(String cpf) {
        if (cpf == null || !cpf.matches(FORMATO_CPF) || cpf.chars().distinct().count() == 1) {
            return false;
        }
        String base = cpf.substring(0, 9);
        int dv1 = modulo11(base, 11);
        int dv2 = modulo11(base + dv1, 11);
        return cpf.endsWith("" + dv1 + dv2);
    }

    /** Soma de cada caractere multiplicado pelo peso da mesma posição, da esquerda para a direita. */
    public static int somaPonderada(String numero, int... pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += valor(numero.charAt(i)) * pesos[i];
        }
        return soma;
    }

    /** Valor do caractere: código ASCII − 48. */
    static int valor(char c) {
        return c - '0';
    }
}
