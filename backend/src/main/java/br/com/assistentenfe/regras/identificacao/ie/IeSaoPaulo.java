package br.com.assistentenfe.regras.identificacao.ie;

import br.com.assistentenfe.dominio.DigitoVerificador;

/**
 * IE de São Paulo (industriais e comerciantes): 12 dígitos; o 9º e o 12º são
 * dígitos verificadores. Cada dígito é o algarismo mais à direita do resto da
 * divisão por 11 da soma ponderada:
 * <ul>
 *   <li>9º: posições 1–8, pesos 1, 3, 4, 5, 6, 7, 8, 10;</li>
 *   <li>12º: posições 1–11, pesos 3, 2, 10, 9, 8, 7, 6, 5, 4, 3, 2.</li>
 * </ul>
 * A IE de produtor rural (iniciada por "P") está fora do escopo e é recusada pelo formato.
 *
 * <p>Fonte: SINTEGRA, "Conferência de Inscrições Estaduais — São Paulo",
 * http://www.sintegra.gov.br/Cad_Estados/cad_SP.html (exemplo: 110.042.490.114).
 */
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
