package br.com.assistentenfe.avaliacao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

final class Git {

    private Git() {
    }

    static String commit() {
        String hash = executar("git", "rev-parse", "HEAD");
        return hash.isBlank() ? "sem commit" : hash;
    }

    static boolean temAlteracoesPendentes() {
        return executar("git", "status", "--porcelain").lines()
                .anyMatch(l -> !l.substring(3).startsWith("docs/avaliacao/"));
    }

    private static String executar(String... comando) {
        try {
            Process processo = new ProcessBuilder(List.of(comando)).redirectErrorStream(false).start();
            String saida = new String(processo.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            return processo.waitFor() == 0 ? saida : "";
        } catch (IOException | InterruptedException e) {
            return "";
        }
    }
}
