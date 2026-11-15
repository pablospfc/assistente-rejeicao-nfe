package br.com.assistentenfe.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class TabelaCfopTest {

    private static final TabelaCfop tabela = TabelaCfop.carregar();

    @Test
    void carregaTodasAsLinhasDaPlanilhaOriginal() {
        assertThat(tabela.tamanho()).isEqualTo(619);
        assertThat(tabela.quantidadeDeDevolucao()).isEqualTo(105);
    }

    @ParameterizedTest
    @ValueSource(strings = {"5202", "6202", "1201", "1202", "1410", "1411", "1503", "1918"})
    void cfopsDeDevolucao(String cfop) {
        assertThat(tabela.devolucao(cfop)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"5101", "5102", "6102", "5401", "1949", "2949", "7930"})
    void cfopsQueNaoSaoDeDevolucao(String cfop) {
        assertThat(tabela.existe(cfop)).isTrue();
        assertThat(tabela.devolucao(cfop)).isFalse();
    }

    @Test
    void cfopInexistente() {
        assertThat(tabela.existe("9999")).isFalse();
        assertThat(tabela.devolucao("9999")).isFalse();
    }
}
