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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Gera os XMLs de demonstração (Fase 6, D022/D027): os XMLs de base assinados e alguns
 * com erro, criados pelas mesmas mutações da avaliação, e os casos das figuras do artigo. Só grava com
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

    /**
     * Os XMLs de base trazem um comentário dizendo que a assinatura é só de preenchimento (D006).
     * Nos exemplos ela é real, então o comentário é trocado. Comentários ficam fora do infNFe e
     * da canonicalização, e a troca não afeta a assinatura.
     */
    private static final Pattern COMENTARIO_PREENCHIMENTO = Pattern.compile("<!--[^>]*apenas estrutural[^>]*-->");
    private static final String COMENTARIO_ASSINADA =
            "<!-- Assinada com certificado de teste autoassinado, sem valor fiscal (só o certificado público; D027). -->";

    record Exemplo(String arquivo, String descricao, String xml, List<String> codigos) {
    }

    @Test
    void geraExemplosComCertificadoPublicoESemChavePrivada() throws IOException {
        List<Exemplo> exemplos = new ArrayList<>();
        for (String base : XmlDeTeste.nomesValidos()) {
            exemplos.add(exemplo("valido-" + base, "XML de base, sem rejeições", XmlDeTeste.valido(base)));
        }
        for (var entrada : EXEMPLOS_COM_ERRO.entrySet()) {
            Mutacao mutacao = mutacao(entrada.getKey());
            String mutado = mutacao.aplicacao().aplicar(XmlDeTeste.valido(entrada.getValue())).orElseThrow();
            String xml = mutacao.assinatura() == Mutacao.Assinatura.REASSINAR ? AssinadorNFe.assinar(mutado) : mutado;
            String nome = "erro-" + mutacao.codigo() + "-" + mutacao.id().substring(mutacao.id().indexOf('-') + 1) + ".xml";
            exemplos.add(exemplo(nome, mutacao.descricao() + " (base " + entrada.getValue() + ")", xml));
        }
        exemplos.addAll(figuras());

        for (Exemplo e : exemplos) {
            // Só o certificado público em KeyInfo/X509Data; nunca a chave privada.
            assertThat(e.xml()).as(e.arquivo()).doesNotContain("PRIVATE KEY").doesNotContain("RSAKeyValue")
                    .doesNotContain("apenas estrutural");
            if (!e.arquivo().startsWith("erro-243")) {
                assertThat(X509.matcher(e.xml()).results().count()).as(e.arquivo()).isEqualTo(1);
            }
            if (e.arquivo().startsWith("valido-") || e.arquivo().equals("figura-1-valido.xml")) {
                assertThat(e.codigos()).as(e.arquivo()).isEmpty();
            } else {
                assertThat(e.codigos()).as(e.arquivo()).isNotEmpty();
            }
        }
        // Figura 2: ocorrências nas duas camadas (Estrutura e Regras de negócio).
        assertThat(exemplos.stream().filter(e -> e.arquivo().equals("figura-2-varias-ocorrencias.xml")).findFirst()
                .orElseThrow().codigos()).contains("225", "207", "610", "598");
        // Figura 3: o 225 da base de devolução, em que o validador aponta o elemento seguinte (D024).
        assertThat(exemplos.stream().filter(e -> e.arquivo().equals("figura-3-225-devolucao.xml")).findFirst()
                .orElseThrow().codigos()).containsExactly("225");

        String saida = System.getProperty("exemplos.saida");
        if (saida != null && !saida.isBlank()) {
            gravar(Path.of(saida), exemplos);
        }
    }

    /** Casos que viram figura no artigo (as capturas finais são feitas pelo autor). */
    private List<Exemplo> figuras() {
        String valido = XmlDeTeste.valido(XmlDeTeste.BASE);

        // Várias mutações na mesma NF-e, todas do tipo "muta e reassina", com uma assinatura no fim.
        String varias = XmlDeTeste.valido(XmlDeTeste.INDUSTRIA);
        for (String id : List.of("M225-lista", "M207-dv", "M610-total", "M598-razao-social")) {
            Mutacao m = mutacao(id);
            assertThat(m.assinatura()).isEqualTo(Mutacao.Assinatura.REASSINAR);
            varias = m.aplicacao().aplicar(varias).orElseThrow();
        }

        Mutacao m225 = mutacao("M225-faltando");
        String devolucao = AssinadorNFe.assinar(m225.aplicacao().aplicar(XmlDeTeste.valido(XmlDeTeste.DEVOLUCAO)).orElseThrow());

        return List.of(
                exemplo("figura-1-valido.xml", "Figura: NF-e válida (base " + XmlDeTeste.BASE + ")", valido),
                exemplo("figura-2-varias-ocorrencias.xml",
                        "Figura: ocorrências nas duas camadas — tpImp fora da lista (225), DV do CNPJ do emitente (207), "
                                + "vNF + R$ 1,00 (610) e razão social real em homologação (598) (base " + XmlDeTeste.INDUSTRIA + ")",
                        AssinadorNFe.assinar(varias)),
                exemplo("figura-3-225-devolucao.xml",
                        "Figura: verProc removido na devolução; o validador aponta o NFref, elemento seguinte (D024) (base "
                                + XmlDeTeste.DEVOLUCAO + ")",
                        devolucao));
    }

    private Exemplo exemplo(String arquivo, String descricao, String xml) {
        String ajustado = COMENTARIO_PREENCHIMENTO.matcher(xml).replaceAll(Matcher.quoteReplacement(COMENTARIO_ASSINADA));
        return new Exemplo(arquivo, descricao, ajustado, codigos(ajustado));
    }

    private static Mutacao mutacao(String id) {
        return CatalogoMutacoes.todas().stream().filter(m -> m.id().equals(id)).findFirst().orElseThrow();
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
            leia.append("| `").append(e.arquivo()).append("` | ").append(RelatorioAvaliacao.celula(e.descricao())).append(" | ")
                    .append(e.codigos().isEmpty() ? "nenhum" : String.join(", ", e.codigos())).append(" |\n");
        }
        Files.writeString(pasta.resolve("README.md"), leia.toString(), StandardCharsets.UTF_8);
    }
}
