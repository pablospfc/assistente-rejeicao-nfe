package br.com.assistentenfe.regras.identificacao.ie;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class EstrategiasIETest {

    @ParameterizedTest
    @CsvSource({
            "MA, 120000385",
            "MA, 123456789",
            "SP, 110042490114",
            "SP, 234567895011",
            "PR, 1234567850",
            "PR, 9012345663",
            "MA, 0123456789",
            "PR, 0001234567850",
            "SP, 00110042490114"})
    void inscricoesValidas(String uf, String ie) {
        assertThat(EstrategiasIE.para(uf)).hasValueSatisfying(e -> assertThat(e.valida(ie)).isTrue());
    }

    @ParameterizedTest
    @CsvSource({
            "MA, 120000386",
            "MA, 130000385",
            "MA, 12000038",
            "SP, 110042491114",
            "SP, 110042490115",
            "SP, P011004243002",
            "PR, 1234567860",
            "PR, 1234567851",
            "PR, 123456785",
            "PR, 91234567850"})
    void inscricoesInvalidas(String uf, String ie) {
        assertThat(EstrategiasIE.para(uf)).hasValueSatisfying(e -> assertThat(e.valida(ie)).isFalse());
    }

    @Test
    void ufSemEstrategia() {
        assertThat(EstrategiasIE.para("PI")).isEmpty();
    }
}
