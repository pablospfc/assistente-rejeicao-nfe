package br.com.assistentenfe.regras.totais;

import br.com.assistentenfe.XmlDeTeste;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.regras.RegraValidacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static br.com.assistentenfe.XmlDeTeste.nfe;
import static br.com.assistentenfe.XmlDeTeste.trocar;
import static org.assertj.core.api.Assertions.assertThat;

/** Mutações sobre a NF-e de indústria, que tem todos os campos de totais preenchidos. */
class RegrasTotaisTest {

    private static final String ICMS_TOT = "/NFe/infNFe/total/ICMSTot/";

    private final String industria = XmlDeTeste.valido(XmlDeTeste.INDUSTRIA);

    static List<RegraValidacao> regras() {
        return List.of(new Regra564TotalProdutos(), new Regra531TotalBcIcms(), new Regra532TotalIcms(),
                new Regra528IcmsDoItem(), new Regra533TotalBcIcmsSt(), new Regra534TotalIcmsSt(),
                new Regra535TotalFrete(), new Regra536TotalSeguro(), new Regra537TotalDesconto(),
                new Regra538TotalIpi(), new Regra610TotalNF());
    }

    static List<String> xmlsValidos() {
        return XmlDeTeste.nomesValidos();
    }

    @ParameterizedTest
    @MethodSource("xmlsValidos")
    void xmlsValidosPassamEmTodasAsRegrasDeTotais(String nome) {
        String xml = XmlDeTeste.valido(nome);

        for (RegraValidacao regra : regras()) {
            assertThat(regra.validar(nfe(xml))).as(regra.codigoRejeicao()).isEmpty();
        }
    }

    // --- Somatórios: uma mutação no item faz o total divergir ---------------------------

    @ParameterizedTest(name = "{0}: item {1} → {2}")
    @CsvSource(delimiter = '|', value = {
            "564 | <vProd>1000.00</vProd>     | <vProd>1001.00</vProd>     | vProd  | 1700.00 | 1701.00",
            "531 | <vBC>475.00</vBC>          | <vBC>476.00</vBC>          | vBC    | 1555.00 | 1556.00",
            "532 | <vICMS>95.00</vICMS>       | <vICMS>96.00</vICMS>       | vICMS  | 311.00  | 312.00",
            "533 | <vBCST>1512.00</vBCST>     | <vBCST>1500.00</vBCST>     | vBCST  | 1512.00 | 1500.00",
            "534 | <vICMSST>106.40</vICMSST>  | <vICMSST>106.50</vICMSST>  | vST    | 106.40  | 106.50",
            "535 | <vFrete>25.00</vFrete>     | <vFrete>26.00</vFrete>     | vFrete | 75.00   | 76.00",
            "536 | <vSeg>10.00</vSeg>         | <vSeg>11.00</vSeg>         | vSeg   | 10.00   | 11.00",
            "537 | <vDesc>50.00</vDesc>       | <vDesc>51.00</vDesc>       | vDesc  | 150.00  | 151.00",
            "538 | <vIPI>50.00</vIPI>         | <vIPI>51.00</vIPI>         | vIPI   | 150.00  | 151.00"})
    void valorDoItemAlteradoDisparaACodigoDoTotal(String codigo, String de, String para,
                                                  String campo, String total, String soma) {
        // A primeira ocorrência de cada trecho está nos itens (o total vem depois no XML).
        String xml = trocar(industria, de, para);

        RegraValidacao regra = regras().stream().filter(r -> r.codigoRejeicao().equals(codigo)).findFirst().orElseThrow();

        assertThat(regra.validar(nfe(xml))).singleElement().satisfies(o -> {
            assertThat(o.codigo()).isEqualTo(codigo);
            assertThat(o.xpath()).isEqualTo(ICMS_TOT + campo);
            assertThat(o.valorEncontrado()).isEqualTo(total);
            assertThat(o.valorEsperado()).isEqualTo(soma);
        });
    }

    @Test
    void itemComIndTotZeroNaoEntraNoVProd() {
        String item3ForaDoTotal = trocar(industria,
                "<vUnTrib>40.0000000000</vUnTrib>\n        <indTot>1</indTot>",
                "<vUnTrib>40.0000000000</vUnTrib>\n        <indTot>0</indTot>");

        assertThat(new Regra564TotalProdutos().validar(nfe(item3ForaDoTotal))).hasSize(1);
        assertThat(new Regra564TotalProdutos().validar(nfe(trocar(item3ForaDoTotal,
                "<vProd>1700.00</vProd>", "<vProd>1500.00</vProd>")))).isEmpty();
    }

    @Test
    void cst41NaoEntraNoTotalDoIcms() {
        // O item 2 passa a CST 41: seu vICMS (95,00) sai da soma, que vira 216,00.
        String xml = trocar(industria, "<CST>00</CST>", "<CST>41</CST>");

        assertThat(new Regra532TotalIcms().validar(nfe(xml))).extracting(Ocorrencia::valorEsperado).containsExactly("216.00");
        assertThat(new Regra532TotalIcms().validar(nfe(trocar(xml, "<vICMS>311.00</vICMS>", "<vICMS>216.00</vICMS>")))).isEmpty();
    }

    @Test
    void ipiSoEhConferidoNoModelo55() {
        String xml = trocar(trocar(industria, "<vIPI>50.00</vIPI>", "<vIPI>51.00</vIPI>"), "<mod>55</mod>", "<mod>65</mod>");

        assertThat(new Regra538TotalIpi().validar(nfe(xml))).isEmpty();
    }

    @Test
    void semOTotalOuComValorNaoNumericoAsRegrasSaoIgnoradas() {
        String semTotalFrete = trocar(industria, "<vFrete>75.00</vFrete>", "");
        String freteInvalido = trocar(industria, "<vFrete>25.00</vFrete>", "<vFrete>abc</vFrete>");

        assertThat(new Regra535TotalFrete().validar(nfe(semTotalFrete))).isEmpty();
        assertThat(new Regra535TotalFrete().validar(nfe(freteInvalido))).isEmpty();
    }

    @Test
    void campoOpcionalAusenteNoItemValeZero() {
        // O item 3 não tem vFrete; tirar o do item 2 também faz a soma cair para 50,00.
        String xml = trocar(industria, "<vFrete>25.00</vFrete>", "");

        assertThat(new Regra535TotalFrete().validar(nfe(xml))).extracting(Ocorrencia::valorEsperado).containsExactly("50.00");
    }

    // --- 528: ICMS do item -------------------------------------------------------------------

    @Test
    void icmsDoItemDiferenteDeBaseVezesAliquotaDispara528() {
        String xml = trocar(industria, "<vICMS>20.00</vICMS>", "<vICMS>20.02</vICMS>");

        assertThat(new Regra528IcmsDoItem().validar(nfe(xml))).singleElement().satisfies(o -> {
            assertThat(o.codigo()).isEqualTo("528");
            assertThat(o.xpath()).isEqualTo("/NFe/infNFe/det[3]/imposto/ICMS/ICMS20/vICMS");
            assertThat(o.valorEncontrado()).isEqualTo("20.02");
            assertThat(o.valorEsperado()).isEqualTo("20.00");
        });
    }

    @Test
    void diferencaDeUmCentavoNo528EstaNaTolerancia() {
        String xml = trocar(industria, "<vICMS>20.00</vICMS>", "<vICMS>20.01</vICMS>");

        assertThat(new Regra528IcmsDoItem().validar(nfe(xml))).isEmpty();
    }

    @Test
    void o528SoValeParaNotaNormal() {
        String xml = trocar(trocar(industria, "<vICMS>20.00</vICMS>", "<vICMS>25.00</vICMS>"),
                "<finNFe>1</finNFe>", "<finNFe>2</finNFe>");

        assertThat(new Regra528IcmsDoItem().validar(nfe(xml))).isEmpty();
    }

    // --- 610: total da NF ---------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"1911.40", "1911.90", "1910.90"})
    void diferencaDeAteCinquentaCentavosNoVnfPassa(String vNF) {
        String xml = trocar(industria, "<vNF>1911.40</vNF>", "<vNF>" + vNF + "</vNF>");

        assertThat(new Regra610TotalNF().validar(nfe(xml))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1911.91", "1910.89"})
    void diferencaAcimaDeCinquentaCentavosDispara610(String vNF) {
        String xml = trocar(industria, "<vNF>1911.40</vNF>", "<vNF>" + vNF + "</vNF>");

        assertThat(new Regra610TotalNF().validar(nfe(xml))).singleElement().satisfies(o -> {
            assertThat(o.codigo()).isEqualTo("610");
            assertThat(o.xpath()).isEqualTo(ICMS_TOT + "vNF");
            assertThat(o.valorEsperado()).isEqualTo("1911.40");
        });
    }

    @Test
    void vnfSemSubtrairODescontoDispara610() {
        // O desconto (R$ 150,00) é muito maior que a tolerância, então a mutação não se perde nela.
        String xml = trocar(industria, "<vNF>1911.40</vNF>", "<vNF>2061.40</vNF>");

        assertThat(new Regra610TotalNF().validar(nfe(xml))).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1901.40", "1911.40"})
    void icmsDesoneradoPodeOuNaoTerSidoSubtraido(String vNF) {
        // Exceção 3: não rejeitar se o vICMSDeson não tiver sido subtraído.
        String xml = trocar(trocar(industria, "<vICMSDeson>0.00</vICMSDeson>", "<vICMSDeson>10.00</vICMSDeson>"),
                "<vNF>1911.40</vNF>", "<vNF>" + vNF + "</vNF>");

        assertThat(new Regra610TotalNF().validar(nfe(xml))).isEmpty();
    }

    @Test
    void importacaoNaoPassaPelo610() {
        String xml = trocar(trocar(industria, "<CFOP>5101</CFOP>", "<CFOP>3102</CFOP>"),
                "<vNF>1911.40</vNF>", "<vNF>9999.99</vNF>");

        assertThat(new Regra610TotalNF().validar(nfe(xml))).isEmpty();
    }

    @Test
    void faturamentoDiretoDeVeiculoNaoSomaOIcmsSt() {
        // Exceção 1: sem vST, o total esperado é 1911,40 − 106,40 = 1805,00.
        String veiculo = trocar(industria, "<vOutro>20.00</vOutro>\n        <indTot>1</indTot>",
                "<vOutro>20.00</vOutro>\n        <indTot>1</indTot>\n        <veicProd><tpOp>2</tpOp></veicProd>");

        assertThat(new Regra610TotalNF().validar(nfe(veiculo))).extracting(Ocorrencia::valorEsperado).containsExactly("1805.00");
        assertThat(new Regra610TotalNF().validar(nfe(trocar(veiculo, "<vNF>1911.40</vNF>", "<vNF>1805.00</vNF>")))).isEmpty();
    }

    @Test
    void semParcelaObrigatoriaO610EhIgnorado() {
        String xml = trocar(industria, "<vST>106.40</vST>", "");

        assertThat(new Regra610TotalNF().validar(nfe(xml))).isEmpty();
    }
}
