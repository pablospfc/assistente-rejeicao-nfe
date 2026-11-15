package br.com.assistentenfe.avaliacao;

import br.com.assistentenfe.avaliacao.Avaliador.Caso;
import br.com.assistentenfe.avaliacao.Avaliador.ControleNegativo;
import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.explicacao.CatalogoRejeicoes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class RelatorioAvaliacao {

    static final Map<String, List<String>> GRUPOS = new LinkedHashMap<>();

    static {
        GRUPOS.put("Estrutura e assinatura", List.of("243", "225", "297", "404"));
        GRUPOS.put("Chave de acesso", List.of("502", "253"));
        GRUPOS.put("Identificação", List.of("703", "207", "208", "237", "209", "210"));
        GRUPOS.put("Totais e cálculos", List.of("564", "610", "531", "532", "528", "538", "536", "533", "534", "535", "537"));
        GRUPOS.put("Regras da operação", List.of("321", "327", "328", "598", "441"));
    }

    static final List<String> FONTES = List.of(
            "Schemas: Pacote de Liberação 010g v1.03 (NT 2026.006 v1.00 e NT 2026.008 v1.00) — D001",
            "MOC 7.0 e Anexo I (Leiaute e Regras de Validação), versão 7.00 — D013",
            "NT 2020.006 v1.30 (Intermediador e Marketplace), código 441 — D016",
            "Informe Técnico 2023.002 v2.10 (Tabela CFOP), códigos 327 e 328 — D019");

    private final List<Mutacao> mutacoes;
    private final List<Caso> casos;
    private final List<ControleNegativo> controles;
    private final List<String> correcoes;
    private final CatalogoRejeicoes catalogo = CatalogoRejeicoes.carregar();

    public RelatorioAvaliacao(List<Mutacao> mutacoes, List<Caso> casos, List<ControleNegativo> controles,
                              List<String> correcoes) {
        this.mutacoes = mutacoes;
        this.casos = casos;
        this.controles = controles;
        this.correcoes = correcoes;
    }

    static String grupo(String codigo) {
        return GRUPOS.entrySet().stream().filter(g -> g.getValue().contains(codigo))
                .map(Map.Entry::getKey).findFirst().orElse("?");
    }

    public void gravar(Path pasta) throws IOException {
        Files.createDirectories(pasta);
        String cabecalho = cabecalho();
        Files.writeString(pasta.resolve("resultados.csv"), csv(), StandardCharsets.UTF_8);
        Files.writeString(pasta.resolve("tabela-1.md"), cabecalho + tabela1(), StandardCharsets.UTF_8);
        Files.writeString(pasta.resolve("quadro-1.md"), cabecalho + quadro1(), StandardCharsets.UTF_8);
    }

    String csv() {
        StringBuilder s = new StringBuilder("id;codigo;grupo;base;assinatura;isolamento;detectada;codigo_correto;"
                + "campo_correto;explicacao_completa;xpath_esperado;xpath_encontrado;codigos_encontrados;extras\n");
        for (Caso c : casos) {
            Mutacao m = c.mutacao();
            s.append(String.join(";", m.id(), m.codigo(), grupo(m.codigo()), c.base(), m.assinatura().name(),
                    m.isolamento() == null ? "" : "sim", simNao(c.detectada()), simNao(c.codigoCorreto()),
                    simNao(c.campoCorreto()), simNao(c.explicacaoCompleta()),
                    nulo(c.xpathEsperado()), c.xpathEncontrado(),
                    String.join(" ", c.codigos()), String.join(" ", c.extras()))).append('\n');
        }
        return s.toString();
    }

    String tabela1() {
        StringBuilder s = new StringBuilder("## Tabela 1 — Resultado da avaliação por grupo\n\n");
        s.append("Casos = mutações × XMLs de base em que o campo mutado existe. As porcentagens usam os casos como denominador.\n\n");
        s.append("| Grupo | Códigos | Mutações | Casos | Detectadas | Código correto | Campo correto | Explicação completa |\n");
        s.append("|---|---|---:|---:|---:|---:|---:|---:|\n");
        for (var g : GRUPOS.entrySet()) {
            List<Caso> doGrupo = casos.stream().filter(c -> g.getValue().contains(c.mutacao().codigo())).toList();
            long nMutacoes = mutacoes.stream().filter(m -> g.getValue().contains(m.codigo())).count();
            s.append(linha(g.getKey(), g.getValue().size(), nMutacoes, doGrupo));
        }
        s.append(linha("**Total**", GRUPOS.values().stream().mapToInt(List::size).sum(), mutacoes.size(), casos));

        s.append("\n### Definições\n\n");
        s.append("- **Detectada:** a validação devolveu ao menos uma ocorrência.\n");
        s.append("- **Código correto:** o código esperado está entre as ocorrências (D007).\n");
        s.append("- **Campo correto:** alguma ocorrência do código esperado aponta o XPath esperado (e o motivo esperado, no 225 e no 297). No 243 não há XPath.\n");
        s.append("- **Explicação completa:** o catálogo traz campo, problema e correção para o código e o motivo da ocorrência.\n");
        s.append("- **Ocorrências extras:** outros códigos que apareceram no mesmo caso; não entram nas taxas.\n");

        s.append("\n### Controle negativo (XMLs de base sem mutação)\n\n| XML de base | Ocorrências |\n|---|---:|\n");
        for (ControleNegativo c : controles) {
            s.append("| `").append(c.base()).append("` | ").append(c.ocorrencias().size()).append(" |\n");
        }

        s.append("\n### Casos fora do esperado\n\n");
        List<Caso> fora = casos.stream().filter(c -> !(c.codigoCorreto() && c.campoCorreto() && c.explicacaoCompleta())).toList();
        if (fora.isEmpty()) {
            s.append("Nenhum.\n");
        } else {
            s.append("| Mutação | Base | Código correto | Campo correto | Explicação completa | Encontrado |\n|---|---|---|---|---|---|\n");
            for (Caso c : fora) {
                s.append("| ").append(c.mutacao().id()).append(" | `").append(c.base()).append("` | ")
                        .append(simNao(c.codigoCorreto())).append(" | ").append(simNao(c.campoCorreto())).append(" | ")
                        .append(simNao(c.explicacaoCompleta())).append(" | ").append(descrever(c.ocorrencias())).append(" |\n");
            }
        }

        s.append("\n### Ocorrências extras (informativo)\n\n");
        Map<String, List<Caso>> comExtras = casos.stream().filter(c -> !c.extras().isEmpty())
                .collect(Collectors.groupingBy(c -> c.mutacao().id(), LinkedHashMap::new, Collectors.toList()));
        if (comExtras.isEmpty()) {
            s.append("Nenhuma.\n");
        } else {
            s.append("| Mutação | Casos com extras | Códigos extras |\n|---|---:|---|\n");
            comExtras.forEach((id, lista) -> s.append("| ").append(id).append(" | ").append(lista.size()).append(" | ")
                    .append(lista.stream().flatMap(c -> c.extras().stream()).distinct().sorted().collect(Collectors.joining(", ")))
                    .append(" |\n"));
        }

        s.append("\n### Correções feitas durante a avaliação\n\n");
        if (correcoes.isEmpty()) {
            s.append("Nenhuma.\n");
        } else {
            correcoes.forEach(c -> s.append("- ").append(c).append('\n'));
        }
        return s.toString();
    }

    private static String linha(String grupo, int codigos, long nMutacoes, List<Caso> lista) {
        return "| " + grupo + " | " + codigos + " | " + nMutacoes + " | " + lista.size() + " | "
                + taxa(lista, Caso::detectada) + " | " + taxa(lista, Caso::codigoCorreto) + " | "
                + taxa(lista, Caso::campoCorreto) + " | " + taxa(lista, Caso::explicacaoCompleta) + " |\n";
    }

    private static String taxa(List<Caso> lista, Predicate<Caso> criterio) {
        long n = lista.stream().filter(criterio).count();
        double pct = lista.isEmpty() ? 0 : 100.0 * n / lista.size();
        return n + " (" + String.format(Locale.of("pt", "BR"), "%.1f", pct) + "%)";
    }

    String quadro1() {
        StringBuilder s = new StringBuilder("## Quadro 1 — Mutações aplicadas por código de rejeição\n\n");
        s.append("| Grupo | Código | Rejeição | Mutação | Assinatura | Ajuste de isolamento | Casos |\n");
        s.append("|---|---|---|---|---|---|---:|\n");
        for (var g : GRUPOS.entrySet()) {
            for (String codigo : g.getValue()) {
                for (Mutacao m : mutacoes.stream().filter(m -> m.codigo().equals(codigo)).toList()) {
                    long n = casos.stream().filter(c -> c.mutacao().id().equals(m.id())).count();
                    s.append("| ").append(g.getKey()).append(" | ").append(codigo).append(" | ")
                            .append(titulo(codigo)).append(" | ").append(celula(m.descricao())).append(" | ")
                            .append(m.assinatura() == Mutacao.Assinatura.REASSINAR ? "muta e reassina" : "muta após assinar")
                            .append(" | ").append(m.isolamento() == null ? "—" : celula(m.isolamento()))
                            .append(" | ").append(n).append(" |\n");
                }
            }
        }
        return s.toString();
    }

    private String titulo(String codigo) {
        return celula(catalogo.explicar(new Ocorrencia(codigo, Camada.ESTRUTURA, null, null, null, null)).titulo());
    }

    static String celula(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("|", "\\|");
    }

    private String cabecalho() {
        List<String> linhas = new ArrayList<>();
        linhas.add("# Avaliação do Assistente de Rejeições da NF-e\n");
        linhas.add("- **Gerado em:** " + ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        linhas.add("- **Commit:** `" + Git.commit() + "`" + (Git.temAlteracoesPendentes() ? " (com alterações não commitadas)" : ""));
        linhas.add("- **JDK:** " + System.getProperty("java.vendor") + " " + System.getProperty("java.version"));
        linhas.add("- **Instante fixo da validação:** " + Avaliador.AGORA + "; tolerância do 703: " + Avaliador.TOLERANCIA_DH_EMI);
        linhas.add("- **Fontes:**");
        FONTES.forEach(f -> linhas.add("  - " + f));
        return String.join("\n", linhas) + "\n\n";
    }

    private static String descrever(List<Ocorrencia> ocorrencias) {
        if (ocorrencias.isEmpty()) {
            return "nenhuma ocorrência";
        }
        return ocorrencias.stream().map(o -> o.codigo() + (o.xpath() == null ? "" : " em `" + o.xpath() + "`")
                + (o.motivo() == null ? "" : " (" + o.motivo() + ")")).collect(Collectors.joining("; "));
    }

    private static String simNao(boolean valor) {
        return valor ? "sim" : "não";
    }

    private static String nulo(String valor) {
        return valor == null ? "" : valor;
    }
}
