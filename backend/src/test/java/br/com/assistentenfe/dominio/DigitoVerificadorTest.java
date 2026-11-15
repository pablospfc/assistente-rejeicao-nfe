package br.com.assistentenfe.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class DigitoVerificadorTest {

    @Test
    void digitoDaChaveDoXmlDeBase() {
        assertThat(DigitoVerificador.chaveAcesso("2126111122233300018155001000000001130405060")).isEqualTo(1);
    }

    @Test
    void digitoDaChaveComCnpjAlfanumerico() {
        assertThat(DigitoVerificador.chaveAcesso("212611Q7X2M9K400014055001000000002141526374")).isEqualTo(7);
    }

    @Test
    void letrasValemAsciiMenos48() {
        assertThat(DigitoVerificador.valor('0')).isZero();
        assertThat(DigitoVerificador.valor('9')).isEqualTo(9);
        assertThat(DigitoVerificador.valor('A')).isEqualTo(17);
        assertThat(DigitoVerificador.valor('Z')).isEqualTo(42);
    }

    @ParameterizedTest
    @ValueSource(strings = {"11222333000181", "12ABC34501DE35", "Q7X2M9K4000140"})
    void cnpjsValidos(String cnpj) {
        // 12ABC34501DE35 é o exemplo de CNPJ alfanumérico da Receita Federal.
        assertThat(DigitoVerificador.cnpjValido(cnpj)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"11222333000182", "12ABC34501DE36", "00000000000000", "12abc34501de35", "1122233300018"})
    void cnpjsInvalidos(String cnpj) {
        assertThat(DigitoVerificador.cnpjValido(cnpj)).isFalse();
    }

    @Test
    void cpfValido() {
        assertThat(DigitoVerificador.cpfValido("11144477735")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"11144477736", "11111111111", "00000000000", "1114447773", "1114447773X"})
    void cpfsInvalidos(String cpf) {
        // 111.111.111-11 tem dígitos que "conferem", mas é inválido por ter todos os dígitos iguais.
        assertThat(DigitoVerificador.cpfValido(cpf)).isFalse();
    }
}
