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

/**
 * XMLs sintéticos usados nos testes e mutações textuais simples sobre eles.
 * As mutações são funções puras sobre a String, para serem reaproveitadas
 * pelo gerador de mutações (Fase 5).
 */
public final class XmlDeTeste {

    /** Venda interna no MA; emitente com CNPJ numérico, destinatário pessoa física. */
    public static final String BASE = "nfe-ma-venda-interna.xml";
    /** Emitente com CNPJ alfanumérico; destinatário contribuinte em SP. */
    public static final String ALFANUMERICO_SP = "nfe-ma-sp-cnpj-alfanumerico.xml";
    /** Emitente com CNPJ numérico; destinatário contribuinte no PR. */
    public static final String CONTRIBUINTE_PR = "nfe-ma-pr-contribuinte.xml";
    /** Indústria: ICMS-ST, IPI (IPITrib e IPINT), frete, seguro, desconto, vOutro e pagamento 99 com xPag. */
    public static final String INDUSTRIA = "nfe-ma-industria-st-ipi.xml";
    /** Devolução de compra (finNFe = 4) com NFref e vIPIDevol. */
    public static final String DEVOLUCAO = "nfe-ma-devolucao.xml";

    private XmlDeTeste() {
    }

    /** Lê o XML como {@link NFe}; os XMLs de teste são sempre bem formados. */
    public static NFe nfe(String xml) {
        try {
            return new NFe(new VerificadorXmlBemFormado().ler(bytes(xml)));
        } catch (XmlMalFormadoException e) {
            throw new IllegalArgumentException("XML de teste mal formado", e);
        }
    }

    /**
     * XML de base assinado com o certificado gerado nesta execução (D022). No disco, os
     * arquivos têm só uma assinatura de preenchimento, que é substituída aqui.
     */
    public static String valido(String nome) {
        return ASSINADOS.computeIfAbsent(nome, n -> AssinadorNFe.assinar(noDisco(n)));
    }

    /** XML de base como está no disco, com a assinatura de preenchimento. */
    public static String noDisco(String nome) {
        try {
            return Files.readString(pastaValidos().resolve(nome), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Reassina um XML depois de uma mutação, para ela não disparar também o 297. */
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

    /** Substitui exatamente uma ocorrência; falha se o trecho não existir, para a mutação nunca passar em branco. */
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
