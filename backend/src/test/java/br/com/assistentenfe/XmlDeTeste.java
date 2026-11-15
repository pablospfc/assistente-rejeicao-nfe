package br.com.assistentenfe;

import br.com.assistentenfe.assinatura.AssinadorNFe;
import br.com.assistentenfe.dominio.NFe;
import br.com.assistentenfe.estrutura.VerificadorXmlBemFormado;
import br.com.assistentenfe.estrutura.XmlMalFormadoException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class XmlDeTeste {

    public static final String BASE = "nfe-ma-venda-interna.xml";
    public static final String ALFANUMERICO_SP = "nfe-ma-sp-cnpj-alfanumerico.xml";
    public static final String CONTRIBUINTE_PR = "nfe-ma-pr-contribuinte.xml";
    public static final String INDUSTRIA = "nfe-ma-industria-st-ipi.xml";
    public static final String DEVOLUCAO = "nfe-ma-devolucao.xml";

    private XmlDeTeste() {
    }

    public static NFe nfe(String xml) {
        try {
            return new NFe(new VerificadorXmlBemFormado().ler(bytes(xml)));
        } catch (XmlMalFormadoException e) {
            throw new IllegalArgumentException("XML de teste mal formado", e);
        }
    }

    public static String valido(String nome) {
        return ASSINADOS.computeIfAbsent(nome, n -> AssinadorNFe.assinar(noDisco(n)));
    }

    public static String noDisco(String nome) {
        try {
            return Files.readString(pastaValidos().resolve(nome), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String assinado(String xml) {
        return AssinadorNFe.assinar(xml);
    }

    private static final Map<String, String> ASSINADOS = new ConcurrentHashMap<>();

    public static List<String> nomesValidos() {
        try (Stream<Path> arquivos = Files.list(pastaValidos())) {
            return arquivos.map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(".xml"))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static byte[] bytes(String xml) {
        return xml.getBytes(StandardCharsets.UTF_8);
    }

    public static String trocar(String xml, String de, String para) {
        int i = xml.indexOf(de);
        if (i < 0) {
            throw new IllegalArgumentException("Trecho não encontrado no XML: " + de);
        }
        return xml.substring(0, i) + para + xml.substring(i + de.length());
    }

    private static Path pastaValidos() {
        try {
            return Path.of(XmlDeTeste.class.getResource("/xml/validos").toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
