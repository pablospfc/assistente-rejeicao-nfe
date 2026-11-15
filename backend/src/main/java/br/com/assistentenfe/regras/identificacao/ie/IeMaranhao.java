package br.com.assistentenfe.regras.identificacao.ie;

import br.com.assistentenfe.dominio.DigitoVerificador;

/**
 * IE do Maranhão: 9 dígitos, começando por "12" (código do estado); o 9º é o
 * dígito verificador, por módulo 11 com pesos 9 a 2. Resto 0 ou 1 resulta em 0.
 *
 * <p>Fonte: SINTEGRA, "Conferência de Inscrições Estaduais — Maranhão",
 * http://www.sintegra.gov.br/Cad_Estados/cad_MA.html (exemplo: 120000385).
 */
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
