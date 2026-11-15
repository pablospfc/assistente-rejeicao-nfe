package br.com.assistentenfe.dominio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TabelaCfop {

    private static final String ARQUIVO = "/tabelas/cfop-it-2023-002-v2.10.tsv";

    private final Set<String> existentes;
    private final Set<String> devolucao;

    private TabelaCfop(Set<String> existentes, Set<String> devolucao) {
        this.existentes = Set.copyOf(existentes);
        this.devolucao = Set.copyOf(devolucao);
    }

    public static TabelaCfop carregar() {
        try (InputStream in = TabelaCfop.class.getResourceAsStream(ARQUIVO)) {
            if (in == null) {
                throw new IllegalStateException("Tabela CFOP não encontrada: " + ARQUIVO);
            }
            BufferedReader leitor = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            List<String> cabecalho = Arrays.asList(leitor.readLine().split("\t", -1));
            int colunaCfop = coluna(cabecalho, "CFOP");
            int colunaDevolucao = coluna(cabecalho, "indDevol");

            Set<String> existentes = new HashSet<>();
            Set<String> devolucao = new HashSet<>();
            for (String linha = leitor.readLine(); linha != null; linha = leitor.readLine()) {
                String[] campos = linha.split("\t", -1);
                existentes.add(campos[colunaCfop]);
                if ("1".equals(campos[colunaDevolucao])) {
                    devolucao.add(campos[colunaCfop]);
                }
            }
            return new TabelaCfop(existentes, devolucao);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler a tabela CFOP", e);
        }
    }

    public boolean devolucao(String cfop) {
        return devolucao.contains(cfop);
    }

    public boolean existe(String cfop) {
        return existentes.contains(cfop);
    }

    public int tamanho() {
        return existentes.size();
    }

    public int quantidadeDeDevolucao() {
        return devolucao.size();
    }

    private static int coluna(List<String> cabecalho, String nome) {
        int indice = cabecalho.indexOf(nome);
        if (indice < 0) {
            throw new IllegalStateException("Coluna " + nome + " ausente na tabela CFOP");
        }
        return indice;
    }
}
