package br.com.assistentenfe.regras.identificacao.ie;

import br.com.assistentenfe.dominio.DigitoVerificador;

public final class IeMaranhao implements DigitoIE {

    @Override
    public String uf() {
        return "MA";
    }

    @Override
    public int tamanho() {
        return 9;
    }

    @Override
    public boolean confere(String ie) {
        if (!ie.startsWith("12")) {
            return false;
        }
        int dv = DigitoVerificador.modulo11(ie.substring(0, 8), 9);
        return ie.charAt(8) - '0' == dv;
    }
}
