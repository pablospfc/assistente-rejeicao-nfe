package br.com.assistentenfe.avaliacao;

import br.com.assistentenfe.XmlDeTeste;
import br.com.assistentenfe.assinatura.AssinadorNFe;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.estrutura.ValidadorSchema;
import br.com.assistentenfe.regras.RegrasDisponiveis;
import br.com.assistentenfe.validacao.ValidadorNFe;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Gera os XMLs de demonstração (Fase 6, D022/D027): os XMLs de base assinados e alguns
 * com erro, criados pelas mesmas mutações da avaliação. Só grava com
 * {@code -Dexemplos.saida=<pasta>}; sem a propriedade, apenas confere os exemplos.
 *
 * <p>Uso: {@code ./mvnw test -Dtest=ExemplosAssinadosTest -Dexemplos.saida=../exemplos}
 */
class ExemplosAssinadosTest {

    /** Mutação → base usada no exemplo com erro (uma por grupo, mais o 243 para o visualizador de texto). */
    private static final Map<String, String> EXEMPLOS_COM_ERRO = new LinkedHashMap<>();

    static {
        EXEMPLOS_COM_ERRO.put("M243-fechamento", XmlDeTeste.BASE);
        EXEMPLOS_COM_ERRO.put("M225-lista", XmlDeTeste.BASE);
        EXEMPLOS_COM_ERRO.put("M297-conteudo", XmlDeTeste.BASE);
        EXEMPLOS_COM_ERRO.put("M502-nNF", XmlDeTeste.ALFANUMERICO_SP);
        EXEMPLOS_COM_ERRO.put("M210-dv", XmlDeTeste.CONTRIBUINTE_PR);
        EXEMPLOS_COM_ERRO.put("M610-total", XmlDeTeste.INDUSTRIA);
        EXEMPLOS_COM_ERRO.put("M531-item", XmlDeTeste.INDUSTRIA);
        EXEMPLOS_COM_ERRO.put("M327-cfop-venda", XmlDeTeste.DEVOLUCAO);
    }

    private static final Pattern X509 = Pattern.compile("<X509Certificate>([^<]+)</X509Certificate>");

    /** Valida com o relógio real, como a aplicação fará ao receber o exemplo. */
    private final ValidadorNFe validador = new ValidadorNFe(ValidadorSchema.nfe400(),
            RegrasDisponiveis.todas(Clock.systemUTC(), Duration.ofMinutes(5)));

    record Exemplo(String arquivo, String descricao, String xml, List<String> codigos) {
    }

    @Test
    void geraExemplosComCertificadoPublicoESemChavePrivada() throws IOException {
        List<Exemplo> exemplos = new ArrayList<>();
        for (String base : XmlDeTeste.nomesValidos()) {
            String xml = XmlDeTeste.valido(base);
            exemplos.add(new Exemplo("valido-" + base, "XML de base, sem rejeições", xml, codigos(xml)));
        }
        for (var entrada : EXEMPLOS_COM_ERRO.entrySet()) {
            Mutacao mutacao = CatalogoMutacoes.todas().stream()
                    .filter(m -> m.id().equals(entrada.getKey())).findFirst().orElseThrow();
            String mutado = mutacao.aplicacao().aplicar(XmlDeTeste.valido(entrada.getValue())).orElseThrow();
            String xml = mutacao.assinatura() == Mutacao.Assinatura.REASSINAR ? AssinadorNFe.assinar(mutado) : mutado;
            String nome = "erro-" + mutacao.codigo() + "-" + mutacao.id().substring(mutacao.id().indexOf('-') + 1) + ".xml";
            exemplos.add(new Exemplo(nome, mutacao.descricao() + " (base " + entrada.getValue() + ")", xml, codigos(xml)));
        }

        for (Exemplo e : exemplos) {
            // Só o certificado público em KeyInfo/X509Data; nunca a chave privada.
            assertThat(e.xml()).as(e.arquivo()).doesNotContain("PRIVATE KEY").doesNotContain("RSAKeyValue");
            if (!e.arquivo().startsWith("erro-243")) {
                assertThat(X509.matcher(e.xml()).results().count()).as(e.arquivo()).isEqualTo(1);
            }
            if (e.arquivo().startsWith("valido-")) {
                assertThat(e.codigos()).as(e.arquivo()).isEmpty();
            } else {
                assertThat(e.codigos()).as(e.arquivo()).isNotEmpty();
            }
        }

        String saida = System.getProperty("exemplos.saida");
        if (saida != null && !saida.isBlank()) {
            gravar(Path.of(saida), exemplos);
        }
    }

    private List<String> codigos(String xml) {
        return validador.validar(xml.getBytes(StandardCharsets.UTF_8)).ocorrencias().stream()
                .map(Ocorrencia::codigo).distinct().toList();
    }

    private static void gravar(Path pasta, List<Exemplo> exemplos) throws IOException {
        Files.createDirectories(pasta);
        StringBuilder leia = new StringBuilder("""
                # Exemplos de NF-e para demonstração

                **NF-e sintéticas, sem nenhum dado fiscal real**, em ambiente de homologação (`tpAmb = 2`).
                Estão assinadas com um **certificado de teste autoassinado, sem valor fiscal** (não é ICP-Brasil),
                gerado na hora e descartado: cada arquivo leva só o certificado público em `KeyInfo/X509Data`;
                a chave privada nunca é gravada.

                Gerados pelo comando (D022/D027), a partir dos XMLs de base e das mutações da avaliação:

                ```bash
                cd backend
                ./mvnw test -Dtest=ExemplosAssinadosTest -Dexemplos.saida=../exemplos
                ```

                | Arquivo | O que mostra | Códigos esperados |
                |---|---|---|
                """);
        for (Exemplo e : exemplos) {
            Files.writeString(pasta.resolve(e.arquivo()), e.xml(), StandardCharsets.UTF_8);
            leia.append("| `").append(e.arquivo()).append("` | ").append(e.descricao()).append(" | ")
                    .append(e.codigos().isEmpty() ? "nenhum" : String.join(", ", e.codigos())).append(" |\n");
        }
        Files.writeString(pasta.resolve("README.md"), leia.toString(), StandardCharsets.UTF_8);
    }
}
