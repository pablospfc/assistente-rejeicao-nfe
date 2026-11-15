package br.com.assistentenfe.avaliacao;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RelatorioAvaliacaoTest {

    @Test
    void celulaEscapaTagsEBarraVertical() {
        assertThat(RelatorioAvaliacao.celula("Remove o fechamento de uma tag (</natOp>)"))
                .isEqualTo("Remove o fechamento de uma tag (&lt;/natOp&gt;)");
        assertThat(RelatorioAvaliacao.celula("A | B & C")).isEqualTo("A \\| B &amp; C");
    }
}
