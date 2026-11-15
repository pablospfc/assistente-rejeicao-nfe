package br.com.assistentenfe.regras.identificacao.ie;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/** Os casos válidos incluem os exemplos das páginas de conferência do SINTEGRA. */
class EstrategiasIETest {

    @ParameterizedTest
    @CsvSource({
            "MA, 120000385",       // exemplo do SINTEGRA
            "MA, 123456789",
            "SP, 110042490114",    // exemplo do SINTEGRA (resto 10 → dígito 0)
            "SP, 234567895011",
            "PR, 1234567850",      // exemplo do SINTEGRA (resto 1 → dígito 0)
            "PR, 9012345663",
            // MOC 7.0, Anexo I, nota (*2): zeros não significativos são desprezados.
            "MA, 0123456789",
            "PR, 0001234567850",
            "SP, 00110042490114"})
    void inscricoesValidas(String uf, String ie) {
        assertThat(EstrategiasIE.para(uf)).hasValueSatisfying(e -> assertThat(e.valida(ie)).isTrue());
    }

    @ParameterizedTest
    @CsvSource({
            "MA, 120000386",       // dígito errado
            "MA, 130000385",       // não começa por 12
            "MA, 12000038",        // completada vira 012000038, que não começa por 12
            "SP, 110042491114",    // 1º dígito (9ª posição) errado
            "SP, 110042490115",    // 2º dígito (12ª posição) errado
            "SP, P011004243002",   // produtor rural: fora do escopo
            "PR, 1234567860",      // 1º dígito errado
            "PR, 1234567851",      // 2º dígito errado
            "PR, 123456785",       // falta um dígito: completado com zero à esquerda, o DV não confere
            "PR, 91234567850"})    // dígito significativo além do tamanho
    void inscricoesInvalidas(String uf, String ie) {
        assertThat(EstrategiasIE.para(uf)).hasValueSatisfying(e -> assertThat(e.valida(ie)).isFalse());
    }

    @Test
    void ufSemEstrategia() {
        assertThat(EstrategiasIE.para("PI")).isEmpty();
    }
}
