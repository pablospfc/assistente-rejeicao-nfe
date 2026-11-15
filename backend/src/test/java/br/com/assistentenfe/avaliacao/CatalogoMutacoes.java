package br.com.assistentenfe.avaliacao;

import br.com.assistentenfe.avaliacao.Mutacao.Aplicacao;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static br.com.assistentenfe.avaliacao.Mutacao.Assinatura.APOS_ASSINAR;
import static br.com.assistentenfe.avaliacao.Mutacao.Assinatura.REASSINAR;

public final class CatalogoMutacoes {

    private static final String INF = "/NFe/infNFe/";
    private static final String IDE = INF + "ide/";
    private static final String TOT = INF + "total/ICMSTot/";
    private static final Set<String> CST_528 = Set.of("00", "10", "20", "70");

    private CatalogoMutacoes() {
    }

    public static List<String> correcoes() {
        return List.of();
    }

    public static List<Mutacao> todas() {
        return List.of(
                new Mutacao("M243-fechamento", "243", null,
                        "Remove o fechamento de uma tag (</natOp>)", null, APOS_ASSINAR, null,
                        noTexto("</natOp>", "")),
                new Mutacao("M243-ecomercial", "243", null,
                        "Insere \"&\" sem escape na razão social do emitente", null, APOS_ASSINAR, null,
                        noTextoComRegex("(<emit>\\s*<C[NP][PF]J?>[^<]*</C[NP][PF]J?>\\s*<xNome>)", "$1A & B ")),
                new Mutacao("M225-lista", "225", "lista",
                        "Informa tpImp = 9, fora da lista de valores do schema", IDE + "tpImp", REASSINAR, null,
                        noDom(campo(IDE + "tpImp", e -> e.setTextContent("9")))),
                new Mutacao("M225-faltando", "225", "elementoFaltando",
                        "Remove o campo obrigatório verProc, último do grupo ide", INF + "ide", REASSINAR, null,
                        noDom(campo(IDE + "verProc", Dom::remover))),
                new Mutacao("M404-prefixo", "404", null,
                        "Escreve emit/xNome com prefixo (nfe:) para o próprio namespace da NF-e",
                        INF + "emit/xNome", REASSINAR, "prefixo para o mesmo namespace, para o schema continuar válido",
                        noDom(campo(INF + "emit/xNome",
                                e -> e.getOwnerDocument().renameNode(e, Dom.NS_NFE, "nfe:xNome")))),
                new Mutacao("M297-conteudo", "297", "conteudoAlterado",
                        "Altera a natureza da operação depois de assinar", "/NFe/infNFe", APOS_ASSINAR, null,
                        noDom(campo(IDE + "natOp", e -> e.setTextContent(e.getTextContent() + " ALTERADA")))),
                new Mutacao("M297-valor", "297", "valorDaAssinatura",
                        "Altera um caractere do SignatureValue", "/NFe/Signature/SignatureValue", APOS_ASSINAR, null,
                        CatalogoMutacoes::alterarSignatureValue),

                new Mutacao("M502-nNF", "502", null,
                        "Soma 1 ao nNF e recalcula o cDV, sem atualizar o Id", INF + "@Id", REASSINAR,
                        "cDV recalculado, para isolar o 502 do 253 (D008)",
                        noDom(campo(IDE + "nNF", e -> {
                            e.setTextContent(String.valueOf(Long.parseLong(e.getTextContent()) + 1));
                            Dom.recalcularCdv(e.getOwnerDocument());
                        }))),
                new Mutacao("M253-cDV", "253", null,
                        "Altera o cDV e o último dígito do Id juntos", IDE + "cDV", REASSINAR,
                        "Id alterado junto com o cDV, para isolar o 253 do 502 (D008)",
                        noDom(campo(IDE + "cDV", e -> {
                            String novo = Dom.outroUltimoDigito(e.getTextContent());
                            e.setTextContent(novo);
                            Element infNFe = (Element) e.getParentNode().getParentNode();
                            String id = infNFe.getAttribute("Id");
                            infNFe.setAttribute("Id", id.substring(0, id.length() - 1) + novo);
                        }))),

                new Mutacao("M703-futuro", "703", null,
                        "Informa dhEmi uma hora depois do instante da validação", IDE + "dhEmi", REASSINAR, null,
                        noDom(campo(IDE + "dhEmi", e -> e.setTextContent(Avaliador.DH_EMI_FUTURO)))),
                new Mutacao("M207-dv", "207", null,
                        "Altera o dígito verificador do CNPJ do emitente", INF + "emit/CNPJ", REASSINAR,
                        "chave de acesso recomposta com o CNPJ alterado, como faria o emissor",
                        noDom(campo(INF + "emit/CNPJ", e -> {
                            e.setTextContent(Dom.outroUltimoDigito(e.getTextContent()));
                            Dom.recomporChave(e.getOwnerDocument());
                        }))),
                new Mutacao("M208-dv", "208", null,
                        "Altera o dígito verificador do CNPJ do destinatário", INF + "dest/CNPJ", REASSINAR, null,
                        noDom(campo(INF + "dest/CNPJ", e -> e.setTextContent(Dom.outroUltimoDigito(e.getTextContent()))))),
                new Mutacao("M237-dv", "237", null,
                        "Altera o dígito verificador do CPF do destinatário", INF + "dest/CPF", REASSINAR, null,
                        noDom(campo(INF + "dest/CPF", e -> e.setTextContent(Dom.outroUltimoDigito(e.getTextContent()))))),
                new Mutacao("M237-iguais", "237", null,
                        "Informa CPF do destinatário com todos os dígitos iguais", INF + "dest/CPF", REASSINAR, null,
                        noDom(campo(INF + "dest/CPF", e -> e.setTextContent("11111111111")))),
                new Mutacao("M209-dv", "209", null,
                        "Altera o dígito verificador da IE do emitente", INF + "emit/IE", REASSINAR, null,
                        noDom(campo(INF + "emit/IE", e -> e.setTextContent(Dom.outroUltimoDigito(e.getTextContent()))))),
                new Mutacao("M210-dv", "210", null,
                        "Altera o dígito verificador da IE do destinatário", INF + "dest/IE", REASSINAR, null,
                        noDom(campo(INF + "dest/IE", e -> e.setTextContent(Dom.outroUltimoDigito(e.getTextContent()))))),

                new Mutacao("M564-item", "564", null,
                        "Soma R$ 1,00 ao vProd do 1º item", TOT + "vProd", REASSINAR, null,
                        noDom(campo(INF + "det/prod/vProd", e -> Dom.somar(e, "1.00")))),
                new Mutacao("M531-item", "531", null,
                        "Soma R$ 1,00 à base de cálculo do ICMS do 1º item", TOT + "vBC", REASSINAR, null,
                        noDom(noGrupoIcms("vBC", e -> Dom.somar(e, "1.00")))),
                new Mutacao("M532-item", "532", null,
                        "Soma R$ 1,00 ao ICMS do 1º item", TOT + "vICMS", REASSINAR, null,
                        noDom(noGrupoIcms("vICMS", e -> Dom.somar(e, "1.00")))),
                new Mutacao("M528-item", "528", null,
                        "Soma R$ 1,00 ao ICMS do 1º item (CST 00/10/20/70, NF-e normal)",
                        INF + "{det}/imposto/ICMS/{icms}/vICMS", REASSINAR,
                        "total do ICMS ajustado junto, como faria o emissor, para isolar o 528 do 532",
                        noDom(CatalogoMutacoes::icmsDoItemComTotal)),
                new Mutacao("M533-item", "533", null,
                        "Soma R$ 1,00 à base de cálculo do ICMS-ST do 1º item", TOT + "vBCST", REASSINAR, null,
                        noDom(noGrupoIcms("vBCST", e -> Dom.somar(e, "1.00")))),
                new Mutacao("M534-item", "534", null,
                        "Soma R$ 1,00 ao ICMS-ST do 1º item", TOT + "vST", REASSINAR, null,
                        noDom(noGrupoIcms("vICMSST", e -> Dom.somar(e, "1.00")))),
                new Mutacao("M535-item", "535", null,
                        "Soma R$ 1,00 ao frete do 1º item", TOT + "vFrete", REASSINAR, null,
                        noDom(campo(INF + "det/prod/vFrete", e -> Dom.somar(e, "1.00")))),
                new Mutacao("M536-item", "536", null,
                        "Soma R$ 1,00 ao seguro do 1º item", TOT + "vSeg", REASSINAR, null,
                        noDom(campo(INF + "det/prod/vSeg", e -> Dom.somar(e, "1.00")))),
                new Mutacao("M537-item", "537", null,
                        "Soma R$ 1,00 ao desconto do 1º item", TOT + "vDesc", REASSINAR, null,
                        noDom(campo(INF + "det/prod/vDesc", e -> Dom.somar(e, "1.00")))),
                new Mutacao("M538-item", "538", null,
                        "Soma R$ 1,00 ao IPI tributado do 1º item", TOT + "vIPI", REASSINAR, null,
                        noDom(campo(INF + "det/imposto/IPI/IPITrib/vIPI", e -> Dom.somar(e, "1.00")))),
                new Mutacao("M610-total", "610", null,
                        "Soma R$ 1,00 ao vNF, acima da tolerância de R$ 0,50", TOT + "vNF", REASSINAR, null,
                        noDom(campo(TOT + "vNF", e -> Dom.somar(e, "1.00")))),

                new Mutacao("M321-sem-NFref", "321", null,
                        "Remove o documento referenciado de uma NF-e de devolução", IDE + "finNFe", REASSINAR, null,
                        noDom(CatalogoMutacoes::removerNFref)),
                new Mutacao("M327-cfop-venda", "327", null,
                        "Usa CFOP de venda (x102) numa NF-e de devolução", INF + "{det}/prod/CFOP", REASSINAR, null,
                        noDom(CatalogoMutacoes::cfopDeVendaNaDevolucao)),
                new Mutacao("M328-cfop-devolucao", "328", null,
                        "Usa CFOP de devolução (x202) numa NF-e normal", INF + "{det}/prod/CFOP", REASSINAR, null,
                        noDom(CatalogoMutacoes::cfopDeDevolucaoNaVenda)),
                new Mutacao("M598-razao-social", "598", null,
                        "Informa a razão social real do destinatário em homologação", INF + "dest/xNome", REASSINAR, null,
                        noDom(campo(INF + "dest/xNome", e -> e.setTextContent("CLIENTE FICTICIO LTDA")))),
                new Mutacao("M441-sem-xPag", "441", null,
                        "Remove a descrição (xPag) de um pagamento 99 (Outros)", INF + "pag/detPag[2]/tPag", REASSINAR, null,
                        noDom(CatalogoMutacoes::removerXPagDoPagamento99)));
    }

    private static Aplicacao noDom(Predicate<Document> mutar) {
        return xml -> {
            Document documento = Dom.ler(xml);
            return mutar.test(documento) ? Optional.of(Dom.escrever(documento)) : Optional.empty();
        };
    }

    private static Aplicacao noTexto(String de, String para) {
        return xml -> xml.contains(de) ? Optional.of(xml.replaceFirst(Pattern.quote(de), Matcher.quoteReplacement(para)))
                : Optional.empty();
    }

    private static Aplicacao noTextoComRegex(String regex, String substituicao) {
        return xml -> {
            Matcher m = Pattern.compile(regex).matcher(xml);
            return m.find() ? Optional.of(m.replaceFirst(substituicao)) : Optional.empty();
        };
    }

    private static Predicate<Document> campo(String caminho, Consumer<Element> alterar) {
        return documento -> Dom.elemento(documento, caminho).map(e -> {
            alterar.accept(e);
            return true;
        }).orElse(false);
    }

    private static Predicate<Document> noGrupoIcms(String campo, Consumer<Element> alterar) {
        return documento -> Dom.grupoIcmsDoPrimeiroItem(documento)
                .flatMap(g -> Dom.filhos(g, campo).stream().findFirst())
                .map(e -> {
                    alterar.accept(e);
                    return true;
                }).orElse(false);
    }

    private static Optional<String> alterarSignatureValue(String xml) {
        Matcher m = Pattern.compile("<SignatureValue>([^<]{20,})</SignatureValue>").matcher(xml);
        if (!m.find()) {
            return Optional.empty();
        }
        char[] valor = m.group(1).toCharArray();
        valor[10] = valor[10] == 'A' ? 'B' : 'A';
        return Optional.of(xml.substring(0, m.start(1)) + new String(valor) + xml.substring(m.end(1)));
    }

    private static boolean icmsDoItemComTotal(Document documento) {
        boolean normal = Dom.elemento(documento, IDE + "finNFe").map(e -> e.getTextContent().equals("1")).orElse(false);
        Optional<Element> grupo = Dom.grupoIcmsDoPrimeiroItem(documento);
        boolean cst = grupo.flatMap(g -> Dom.filhos(g, "CST").stream().findFirst())
                .map(e -> CST_528.contains(e.getTextContent())).orElse(false);
        Optional<Element> vICMS = grupo.flatMap(g -> Dom.filhos(g, "vICMS").stream().findFirst());
        Optional<Element> total = Dom.elemento(documento, TOT + "vICMS");
        if (!normal || !cst || vICMS.isEmpty() || total.isEmpty()) {
            return false;
        }
        Dom.somar(vICMS.get(), "1.00");
        Dom.somar(total.get(), "1.00");
        return true;
    }

    private static boolean removerNFref(Document documento) {
        boolean devolucao = Dom.elemento(documento, IDE + "finNFe").map(e -> e.getTextContent().equals("4")).orElse(false);
        List<Element> referencias = Dom.elemento(documento, INF + "ide").map(i -> Dom.filhos(i, "NFref")).orElse(List.of());
        if (!devolucao || referencias.isEmpty()) {
            return false;
        }
        referencias.forEach(Dom::remover);
        return true;
    }

    private static boolean cfopDeVendaNaDevolucao(Document documento) {
        return trocarCfop(documento, "4", "102");
    }

    private static boolean cfopDeDevolucaoNaVenda(Document documento) {
        return trocarCfop(documento, "1", "202");
    }

    private static boolean trocarCfop(Document documento, String finNFe, String final3) {
        boolean finalidade = Dom.elemento(documento, IDE + "finNFe").map(e -> e.getTextContent().equals(finNFe)).orElse(false);
        Optional<Element> cfop = Dom.doPrimeiroItem(documento, "prod/CFOP");
        if (!finalidade || cfop.isEmpty()) {
            return false;
        }
        cfop.get().setTextContent(cfop.get().getTextContent().charAt(0) + final3);
        return true;
    }

    private static boolean removerXPagDoPagamento99(Document documento) {
        for (Element detPag : Dom.elemento(documento, INF + "pag").map(p -> Dom.filhos(p, "detPag")).orElse(List.of())) {
            boolean outros = Dom.filhos(detPag, "tPag").stream().anyMatch(t -> t.getTextContent().equals("99"));
            List<Element> xPag = Dom.filhos(detPag, "xPag");
            if (outros && !xPag.isEmpty()) {
                xPag.forEach(Dom::remover);
                return true;
            }
        }
        return false;
    }
}
