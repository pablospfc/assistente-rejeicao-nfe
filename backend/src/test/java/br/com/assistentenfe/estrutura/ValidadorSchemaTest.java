package br.com.assistentenfe.estrutura;

import br.com.assistentenfe.XmlDeTeste;
import br.com.assistentenfe.dominio.Ocorrencia;
import org.junit.jupiter.api.Test;

import java.util.List;

import static br.com.assistentenfe.XmlDeTeste.bytes;
import static br.com.assistentenfe.XmlDeTeste.trocar;
import static org.assertj.core.api.Assertions.assertThat;

class ValidadorSchemaTest {

    private static final ValidadorSchema validador = ValidadorSchema.nfe400();

    private final String base = XmlDeTeste.valido(XmlDeTeste.BASE);

    @Test
    void xmlValidoPassa() throws Exception {
        assertThat(validar(base)).isEmpty();
    }

    @Test
    void valorForaDaListaApontaCampoEValor() throws Exception {
        List<Ocorrencia> ocorrencias = validar(trocar(base, "<cUF>21</cUF>", "<cUF>99</cUF>"));

        assertThat(ocorrencias).singleElement().satisfies(o -> {
            assertThat(o.codigo()).isEqualTo("225");
            assertThat(o.xpath()).isEqualTo("/NFe/infNFe/ide/cUF");
            assertThat(o.valorEncontrado()).isEqualTo("99");
            assertThat(o.valorEsperado()).contains("21");
            assertThat(o.motivo()).isEqualTo("lista");
        });
    }

    @Test
    void formatoInvalidoNoSegundoItemUsaIndiceNoXPath() throws Exception {
        List<Ocorrencia> ocorrencias = validar(trocar(base, "<vProd>200.00</vProd>", "<vProd>200,00</vProd>"));

        assertThat(ocorrencias).singleElement().satisfies(o -> {
            assertThat(o.xpath()).isEqualTo("/NFe/infNFe/det[2]/prod/vProd");
            assertThat(o.valorEncontrado()).isEqualTo("200,00");
            assertThat(o.motivo()).isEqualTo("formato");
        });
    }

    @Test
    void textoLongoDemaisInformaTamanhoMaximo() throws Exception {
        String nomeLongo = "EMPRESA FICTICIA ".repeat(5).trim();

        List<Ocorrencia> ocorrencias = validar(trocar(base,
                "<xNome>EMPRESA FICTICIA DE TESTES LTDA</xNome>", "<xNome>" + nomeLongo + "</xNome>"));

        assertThat(ocorrencias).singleElement().satisfies(o -> {
            assertThat(o.xpath()).isEqualTo("/NFe/infNFe/emit/xNome");
            assertThat(o.motivo()).isEqualTo("tamanhoMaximo");
            assertThat(o.valorEsperado()).isEqualTo("60");
        });
    }

    @Test
    void campoObrigatorioAusenteNoMeioDoGrupo() throws Exception {
        List<Ocorrencia> ocorrencias = validar(trocar(base, "<cNF>30405060</cNF>", ""));

        assertThat(ocorrencias).singleElement().satisfies(o -> {
            assertThat(o.motivo()).isEqualTo("elementoInesperado");
            assertThat(o.valorEncontrado()).isEqualTo("natOp");
            assertThat(o.valorEsperado()).contains("cNF");
        });
    }

    @Test
    void campoObrigatorioAusenteNoFimDoGrupo() throws Exception {
        List<Ocorrencia> ocorrencias = validar(trocar(base, "<verProc>TCC-1.0</verProc>", ""));

        assertThat(ocorrencias).singleElement().satisfies(o -> {
            assertThat(o.xpath()).isEqualTo("/NFe/infNFe/ide");
            assertThat(o.motivo()).isEqualTo("elementoFaltando");
            assertThat(o.valorEsperado()).contains("verProc");
        });
    }

    @Test
    void atributoInvalidoApontaParaOAtributo() throws Exception {
        List<Ocorrencia> ocorrencias = validar(trocar(base, "versao=\"4.00\"", "versao=\"3.10\""));

        assertThat(ocorrencias).singleElement().satisfies(o -> {
            assertThat(o.xpath()).isEqualTo("/NFe/infNFe/@versao");
            assertThat(o.valorEncontrado()).isEqualTo("3.10");
        });
    }

    @Test
    void namespaceErradoNaRaiz() throws Exception {
        List<Ocorrencia> ocorrencias = validar(trocar(base,
                "<NFe xmlns=\"http://www.portalfiscal.inf.br/nfe\">", "<NFe xmlns=\"http://exemplo.com/nfe\">"));

        assertThat(ocorrencias).first().satisfies(o -> assertThat(o.motivo()).isEqualTo("raizDesconhecida"));
    }

    @Test
    void coletaTodosOsErrosENaoSoOPrimeiro() throws Exception {
        String xml = trocar(trocar(base, "<cUF>21</cUF>", "<cUF>99</cUF>"),
                "<vProd>200.00</vProd>", "<vProd>200,00</vProd>");

        assertThat(validar(xml)).extracting(Ocorrencia::xpath)
                .containsExactly("/NFe/infNFe/ide/cUF", "/NFe/infNFe/det[2]/prod/vProd");
    }

    private List<Ocorrencia> validar(String xml) throws XmlMalFormadoException {
        return validador.validar(new VerificadorXmlBemFormado().ler(bytes(xml)));
    }
}
