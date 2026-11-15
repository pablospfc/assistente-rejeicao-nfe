package br.com.assistentenfe.estrutura;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** O schema oficial carrega com todos os includes/imports resolvidos a partir de resources/xsd. */
class CargaSchemaTest {

    /** nfe_v4.00.xsd inclui o leiaute, que inclui os tipos básicos e importa o xmldsig. */
    private static final List<String> ARQUIVOS = List.of(
            "nfe_v4.00.xsd",
            "leiauteNFe_v4.00.xsd",
            "tiposBasico_v4.00.xsd",
            "DFeTiposBasicos_v1.00.xsd",
            "xmldsig-core-schema_v1.01.xsd");

    @Test
    void todosOsArquivosDoPacoteEstaoEmResources() {
        for (String arquivo : ARQUIVOS) {
            assertThat(getClass().getResource("/xsd/" + arquivo)).as(arquivo).isNotNull();
        }
    }

    @Test
    void schemaDaNFeCarregaComTodosOsIncludesResolvidos() {
        assertThatCode(ValidadorSchema::nfe400).doesNotThrowAnyException();
    }

    @Test
    void faltandoQualquerArquivoIncluidoACargaFalha(@TempDir Path pasta) throws IOException {
        for (String ausente : ARQUIVOS.subList(1, ARQUIVOS.size())) {
            Path copia = Files.createDirectories(pasta.resolve("sem-" + ausente));
            for (String arquivo : ARQUIVOS) {
                if (!arquivo.equals(ausente)) {
                    copiar(arquivo, copia);
                }
            }

            assertThatThrownBy(() -> new ValidadorSchema(copia.resolve("nfe_v4.00.xsd").toUri().toURL()))
                    .as("sem %s", ausente)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    private void copiar(String arquivo, Path destino) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/xsd/" + arquivo)) {
            Files.copy(in, destino.resolve(arquivo));
        }
    }
}
