package br.com.assistentenfe.explicacao;

import br.com.assistentenfe.dominio.Ocorrencia;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Catálogo que transforma uma {@link Ocorrencia} em {@link Explicacao}.
 * Os textos ficam em {@code catalogo-rejeicoes.yml}, nunca nas regras.
 */
public final class CatalogoRejeicoes {

    private static final String ARQUIVO = "/catalogo-rejeicoes.yml";

    private final Map<String, Map<String, Object>> entradas;

    private CatalogoRejeicoes(Map<String, Map<String, Object>> entradas) {
        this.entradas = entradas;
    }

    public static CatalogoRejeicoes carregar() {
        try (InputStream in = CatalogoRejeicoes.class.getResourceAsStream(ARQUIVO)) {
            if (in == null) {
                throw new IllegalStateException("Catálogo não encontrado: " + ARQUIVO);
            }
            return new CatalogoRejeicoes(new Yaml().load(in));
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler o catálogo", e);
        }
    }

    public boolean contem(String codigo) {
        return entradas.containsKey(codigo);
    }

    public Explicacao explicar(Ocorrencia o) {
        Map<String, Object> entrada = entradas.getOrDefault(o.codigo(), Map.of());
        Map<String, Object> motivo = motivo(entrada, o.motivo());

        String titulo = texto(entrada, "titulo");
        String campo = texto(entrada, "campo");
        String problema = texto(motivo, "problema", texto(entrada, "problema"));
        String correcao = texto(motivo, "correcao", texto(entrada, "correcao"));

        String nomeCampo = preencher(campo, o, nomeDoCampo(o.xpath()));
        return new Explicacao(titulo,
                nomeCampo,
                preencher(problema, o, nomeCampo),
                preencher(correcao, o, nomeCampo));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> motivo(Map<String, Object> entrada, String chave) {
        Object motivos = entrada.get("motivos");
        if (chave == null || !(motivos instanceof Map<?, ?> mapa) || !(mapa.get(chave) instanceof Map<?, ?> motivo)) {
            return Map.of();
        }
        return (Map<String, Object>) motivo;
    }

    private static String texto(Map<String, Object> mapa, String chave) {
        return texto(mapa, chave, null);
    }

    private static String texto(Map<String, Object> mapa, String chave, String padrao) {
        Object valor = mapa.get(chave);
        return valor != null ? valor.toString() : padrao;
    }

    /** Último passo do XPath, sem índice: /NFe/infNFe/det[2]/prod/vProd → vProd. */
    static String nomeDoCampo(String xpath) {
        if (xpath == null) {
            return null;
        }
        String ultimo = xpath.substring(xpath.lastIndexOf('/') + 1);
        return ultimo.replaceAll("\\[\\d+]$", "").replace("@", "");
    }

    private static String preencher(String modelo, Ocorrencia o, String campo) {
        if (modelo == null) {
            return null;
        }
        return modelo
                .replace("{campo}", valorOuVazio(campo))
                .replace("{encontrado}", valorOuVazio(o.valorEncontrado()))
                .replace("{esperado}", valorOuVazio(o.valorEsperado()));
    }

    private static String valorOuVazio(String valor) {
        return valor != null ? valor : "";
    }
}
