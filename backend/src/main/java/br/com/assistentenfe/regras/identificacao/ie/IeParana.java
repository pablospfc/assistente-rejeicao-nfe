package br.com.assistentenfe.regras.identificacao.ie;

import br.com.assistentenfe.dominio.DigitoVerificador;

public final class IeParana implements DigitoIE {

    @Override
    public String uf() {
        return "PR";
    }

    @Override
    public int tamanho() {
        return 10;
    }

    @Override
    public boolean confere(String ie) {
        int dv1 = DigitoVerificador.modulo11(ie.substring(0, 8), 7);
        int dv2 = DigitoVerificador.modulo11(ie.substring(0, 9), 7);
        return ie.charAt(8) - '0' == dv1 && ie.charAt(9) - '0' == dv2;
    }
}
