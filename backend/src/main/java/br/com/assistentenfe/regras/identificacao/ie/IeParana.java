package br.com.assistentenfe.regras.identificacao.ie;

import br.com.assistentenfe.dominio.DigitoVerificador;

/**
 * IE do Paraná: 10 dígitos (8 + 2 verificadores). Os dois dígitos usam módulo 11
 * com pesos 2 a 7 aplicados da direita para a esquerda; resto 0 ou 1 resulta em 0.
 * O 1º cobre as posições 1–8 (pesos 3, 2, 7, 6, 5, 4, 3, 2) e o 2º cobre 1–9,
 * incluindo o 1º dígito (pesos 4, 3, 2, 7, 6, 5, 4, 3, 2).
 *
 * <p>Fonte: SINTEGRA, "Conferência de Inscrições Estaduais — Paraná",
 * http://www.sintegra.gov.br/Cad_Estados/cad_PR.html (exemplo: 123.45678-50).
 */
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
