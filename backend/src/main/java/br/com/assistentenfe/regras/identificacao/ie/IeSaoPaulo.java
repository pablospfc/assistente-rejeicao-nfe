package br.com.assistentenfe.regras.identificacao.ie;

import br.com.assistentenfe.dominio.DigitoVerificador;

public final class IeSaoPaulo implements DigitoIE {

    private static final int[] PESOS_DV1 = {1, 3, 4, 5, 6, 7, 8, 10};
    private static final int[] PESOS_DV2 = {3, 2, 10, 9, 8, 7, 6, 5, 4, 3, 2};

    @Override
    public String uf() {
        return "SP";
    }

    @Override
    public int tamanho() {
        return 12;
    }

    @Override
    public boolean confere(String ie) {
        int dv1 = DigitoVerificador.somaPonderada(ie, PESOS_DV1) % 11 % 10;
        int dv2 = DigitoVerificador.somaPonderada(ie, PESOS_DV2) % 11 % 10;
        return ie.charAt(8) - '0' == dv1 && ie.charAt(11) - '0' == dv2;
    }
}
