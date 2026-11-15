package br.com.assistentenfe.regras.operacao;

import br.com.assistentenfe.XmlDeTeste;
import br.com.assistentenfe.dominio.TabelaCfop;
import br.com.assistentenfe.regras.RegraValidacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static br.com.assistentenfe.XmlDeTeste.nfe;
import static br.com.assistentenfe.XmlDeTeste.trocar;
import static org.assertj.core.api.Assertions.assertThat;

class RegrasOperacaoTest {

    private static final String NF_REF = """
            <NFref>
                    <refNFe>21261144555666000181550010000000041637485965</refNFe>
                  </NFref>""";

    private final RegraValidacao regra321 = new Regra321DevolucaoSemReferencia();
    private final RegraValidacao regra598 = new Regra598RazaoSocialHomologacao();
    private final RegraValidacao regra441 = new Regra441DescricaoPagamento();
    private static final TabelaCfop TABELA_CFOP = TabelaCfop.carregar();
    private final RegraValidacao regra327 = new Regra327CfopNaDevolucao(TABELA_CFOP);
    private final RegraValidacao regra328 = new Regra328CfopDeDevolucaoForaDaDevolucao(TABELA_CFOP);

    private final String devolucao = XmlDeTeste.valido(XmlDeTeste.DEVOLUCAO);
    private final String industria = XmlDeTeste.valido(XmlDeTeste.INDUSTRIA);
    private final String base = XmlDeTeste.valido(XmlDeTeste.BASE);

    static List<String> xmlsValidos() {
        return XmlDeTeste.nomesValidos();
    }

    @ParameterizedTest
    @MethodSource("xmlsValidos")
    void xmlsValidosPassam(String nome) {
        String xml = XmlDeTeste.valido(nome);

        for (RegraValidacao regra : List.of(regra321, regra327, regra328, regra598, regra441)) {
            assertThat(regra.validar(nfe(xml))).as(regra.codigoRejeicao()).isEmpty();
        }
    }

    @Test
    void devolucaoSemNfrefDispara321() {
        String xml = trocar(devolucao, NF_REF, "");

        assertThat(regra321.validar(nfe(xml))).singleElement().satisfies(o -> {
            assertThat(o.codigo()).isEqualTo("321");
            assertThat(o.xpath()).isEqualTo("/NFe/infNFe/ide/finNFe");
        });
    }

    @Test
    void referenciaACteNaoBasta() {
        String xml = trocar(devolucao, "<refNFe>21261144555666000181550010000000041637485965</refNFe>",
                "<refCTe>21261144555666000181570010000000041637485965</refCTe>");

        assertThat(regra321.validar(nfe(xml))).hasSize(1);
    }

    @Test
    void referenciaComChaveSigilosaConta() {
        String xml = trocar(devolucao, "<refNFe>21261144555666000181550010000000041637485965</refNFe>",
                "<refNFeSig>21261144555666000181550010000000041000000005</refNFeSig>");

        assertThat(regra321.validar(nfe(xml))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1201", "1202", "1410", "1411", "5921", "6921"})
    void cfopDaExcecaoDispensaAReferencia(String cfop) {
        String xml = trocar(trocar(devolucao, NF_REF, ""), "<CFOP>5202</CFOP>", "<CFOP>" + cfop + "</CFOP>");

        assertThat(regra321.validar(nfe(xml))).isEmpty();
    }

    @Test
    void notaQueNaoEhDevolucaoNaoPrecisaDeReferencia() {
        String xml = trocar(trocar(devolucao, NF_REF, ""), "<finNFe>4</finNFe>", "<finNFe>1</finNFe>");

        assertThat(regra321.validar(nfe(xml))).isEmpty();
    }

    @Test
    void cfopDeVendaNaDevolucaoDispara327() {
        String xml = trocar(devolucao, "<CFOP>5202</CFOP>", "<CFOP>5102</CFOP>");

        assertThat(regra327.validar(nfe(xml))).singleElement().satisfies(o -> {
            assertThat(o.codigo()).isEqualTo("327");
            assertThat(o.xpath()).isEqualTo("/NFe/infNFe/det/prod/CFOP");
            assertThat(o.valorEncontrado()).isEqualTo("5102");
        });
    }

    @Test
    void cfop1949NaDevolucaoDeNaoContribuintePassa() {
        String xml = trocar(trocar(devolucao, "<CFOP>5202</CFOP>", "<CFOP>1949</CFOP>"),
                "<indIEDest>1</indIEDest>", "<indIEDest>9</indIEDest>");

        assertThat(regra327.validar(nfe(xml))).isEmpty();
    }

    @Test
    void cfop1949NaDevolucaoDeContribuinteDispara327() {
        String xml = trocar(devolucao, "<CFOP>5202</CFOP>", "<CFOP>1949</CFOP>");

        assertThat(regra327.validar(nfe(xml))).hasSize(1);
    }

    @Test
    void semCfopO327IgnoraOItem() {
        String xml = trocar(devolucao, "<CFOP>5202</CFOP>", "");

        assertThat(regra327.validar(nfe(xml))).isEmpty();
    }

    @Test
    void cfopDeDevolucaoNumaVendaDispara328() {
        String xml = trocar(base, "<CFOP>5102</CFOP>", "<CFOP>5202</CFOP>");

        assertThat(regra328.validar(nfe(xml))).singleElement().satisfies(o -> {
            assertThat(o.codigo()).isEqualTo("328");
            assertThat(o.xpath()).isEqualTo("/NFe/infNFe/det[1]/prod/CFOP");
            assertThat(o.valorEncontrado()).isEqualTo("5202");
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"2", "4"})
    void notaComplementarOuDeDevolucaoAceitaCfopDeDevolucao(String finNFe) {
        String xml = trocar(trocar(base, "<CFOP>5102</CFOP>", "<CFOP>5202</CFOP>"),
                "<finNFe>1</finNFe>", "<finNFe>" + finNFe + "</finNFe>");

        assertThat(regra328.validar(nfe(xml))).isEmpty();
    }

    @Test
    void semFinalidadeO328EhIgnorado() {
        String xml = trocar(trocar(base, "<CFOP>5102</CFOP>", "<CFOP>5202</CFOP>"), "<finNFe>1</finNFe>", "");

        assertThat(regra328.validar(nfe(xml))).isEmpty();
    }

    @Test
    void razaoSocialRealEmHomologacaoDispara598() {
        String xml = trocar(base, "NF-E EMITIDA EM AMBIENTE DE HOMOLOGACAO - SEM VALOR FISCAL", "FULANO DE TAL FICTICIO");

        assertThat(regra598.validar(nfe(xml))).singleElement().satisfies(o -> {
            assertThat(o.codigo()).isEqualTo("598");
            assertThat(o.xpath()).isEqualTo("/NFe/infNFe/dest/xNome");
            assertThat(o.valorEncontrado()).isEqualTo("FULANO DE TAL FICTICIO");
            assertThat(o.valorEsperado()).isEqualTo("NF-E EMITIDA EM AMBIENTE DE HOMOLOGACAO - SEM VALOR FISCAL");
        });
    }

    @Test
    void emProducaoO598NaoSeAplica() {
        String xml = trocar(trocar(base, "NF-E EMITIDA EM AMBIENTE DE HOMOLOGACAO - SEM VALOR FISCAL", "FULANO DE TAL FICTICIO"),
                "<tpAmb>2</tpAmb>", "<tpAmb>1</tpAmb>");

        assertThat(regra598.validar(nfe(xml))).isEmpty();
    }

    @Test
    void semNomeDoDestinatarioO598EhIgnorado() {
        String xml = trocar(base, "<xNome>NF-E EMITIDA EM AMBIENTE DE HOMOLOGACAO - SEM VALOR FISCAL</xNome>", "");

        assertThat(regra598.validar(nfe(xml))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "<xPag> </xPag>"})
    void pagamento99SemDescricaoDispara441(String substituto) {
        String xml = trocar(industria, "<xPag>CREDITO EM CONTA FICTICIA</xPag>", substituto);

        assertThat(regra441.validar(nfe(xml))).singleElement().satisfies(o -> {
            assertThat(o.codigo()).isEqualTo("441");
            assertThat(o.xpath()).isEqualTo("/NFe/infNFe/pag/detPag[2]/tPag");
            assertThat(o.valorEncontrado()).isEqualTo("99");
        });
    }

    @Test
    void outroMeioDePagamentoSemDescricaoPassa() {
        String xml = trocar(trocar(industria, "<xPag>CREDITO EM CONTA FICTICIA</xPag>", ""), "<tPag>99</tPag>", "<tPag>03</tPag>");

        assertThat(regra441.validar(nfe(xml))).isEmpty();
    }
}
