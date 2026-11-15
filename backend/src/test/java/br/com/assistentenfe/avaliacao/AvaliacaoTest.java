package br.com.assistentenfe.avaliacao;

import br.com.assistentenfe.avaliacao.Avaliador.Caso;
import br.com.assistentenfe.avaliacao.Avaliador.ControleNegativo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Avaliação completa (D023). Roda em todo {@code mvnw test} sem gravar nada e falha se o
 * controle negativo tiver ocorrências. O resultado de cada mutação é registrado, não
 * cobrado: casos fora do esperado vão para o relatório.
 *
 * <p>Para gravar os arquivos: {@code ./mvnw test -Dtest=AvaliacaoTest -Davaliacao.saida=../docs/avaliacao/final}
 */
class AvaliacaoTest {

    private static List<Mutacao> mutacoes;
    private static List<Caso> casos;
    private static List<ControleNegativo> controles;

    @BeforeAll
    static void avaliar() throws IOException {
        Avaliador avaliador = new Avaliador();
        mutacoes = CatalogoMutacoes.todas();
        controles = avaliador.controleNegativo();
        casos = avaliador.casos(mutacoes);

        String saida = System.getProperty("avaliacao.saida");
        if (saida != null && !saida.isBlank()) {
            new RelatorioAvaliacao(mutacoes, casos, controles, CatalogoMutacoes.correcoes()).gravar(Path.of(saida));
        }
    }

    @Test
    void controleNegativoSemNenhumaOcorrencia() {
        for (ControleNegativo controle : controles) {
            assertThat(controle.ocorrencias()).as(controle.base()).isEmpty();
        }
        assertThat(controles).hasSize(5);
    }

    @Test
    void todoCodigoDoEscopoTemAoMenosUmCasoAplicavel() {
        Set<String> escopo = RelatorioAvaliacao.GRUPOS.values().stream().flatMap(List::stream).collect(Collectors.toSet());
        Set<String> comCaso = casos.stream().map(c -> c.mutacao().codigo()).collect(Collectors.toSet());

        assertThat(escopo).hasSize(28);
        assertThat(comCaso).containsExactlyInAnyOrderElementsOf(escopo);
    }

    @Test
    void identificadoresDasMutacoesSaoUnicos() {
        assertThat(mutacoes).extracting(Mutacao::id).doesNotHaveDuplicates();
    }
}
