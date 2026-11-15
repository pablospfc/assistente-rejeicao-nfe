package br.com.assistentenfe.dominio;

public final class DigitoVerificador {

    private static final String FORMATO_CNPJ = "[0-9A-Z]{12}[0-9]{2}";
    private static final String FORMATO_CPF = "[0-9]{11}";

    private DigitoVerificador() {
    }

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

    public static int chaveAcesso(String chaveSemDigito) {
        return modulo11(chaveSemDigito, 9);
    }

    public static boolean cnpjValido(String cnpj) {
        if (cnpj == null || !cnpj.matches(FORMATO_CNPJ) || cnpj.equals("00000000000000")) {
            return false;
        }
        String base = cnpj.substring(0, 12);
        int dv1 = modulo11(base, 9);
        int dv2 = modulo11(base + dv1, 9);
        return cnpj.endsWith("" + dv1 + dv2);
    }

    public static boolean cpfValido(String cpf) {
        if (cpf == null || !cpf.matches(FORMATO_CPF) || cpf.chars().distinct().count() == 1) {
            return false;
        }
        String base = cpf.substring(0, 9);
        int dv1 = modulo11(base, 11);
        int dv2 = modulo11(base + dv1, 11);
        return cpf.endsWith("" + dv1 + dv2);
    }

    public static int somaPonderada(String numero, int... pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += valor(numero.charAt(i)) * pesos[i];
        }
        return soma;
    }

    static int valor(char c) {
        return c - '0';
    }
}
