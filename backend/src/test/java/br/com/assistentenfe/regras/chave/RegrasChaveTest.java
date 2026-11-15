package br.com.assistentenfe.regras.chave;

import br.com.assistentenfe.XmlDeTeste;
import br.com.assistentenfe.dominio.DigitoVerificador;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.regras.RegraValidacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static br.com.assistentenfe.XmlDeTeste.nfe;
import static br.com.assistentenfe.XmlDeTeste.trocar;
import static org.assertj.core.api.Assertions.assertThat;

class RegrasChaveTest {

    private final RegraValidacao regra502 = new Regra502IdDaChave();
    private final RegraValidacao regra253 = new Regra253DigitoDaChave();

    private final String base = XmlDeTeste.valido(XmlDeTeste.BASE);

    static List<String> xmlsValidos() {
        return XmlDeTeste.nomesValidos();
    }

    @ParameterizedTest
    @MethodSource("xmlsValidos")
    void xmlsValidosPassam(String nome) {
        String xml = XmlDeTeste.valido(nome);

        assertThat(regra502.validar(nfe(xml))).isEmpty();
        assertThat(regra253.validar(nfe(xml))).isEmpty();
    }

    @Test
    void campoDaChaveAlteradoDispara502() {
        // Muda o nNF e recalcula o cDV: a chave composta fica coerente, mas o Id não (D008).
        int novoDv = DigitoVerificador.chaveAcesso("2126111122233300018155001000000002130405060");
        String xml = trocar(trocar(base, "<nNF>1</nNF>", "<nNF>2</nNF>"), "<cDV>1</cDV>", "<cDV>" + novoDv + "</cDV>");

        List<Ocorrencia> ocorrencias = regra502.validar(nfe(xml));

        assertThat(ocorrencias).singleElement().satisfies(o -> {
            assertThat(o.codigo()).isEqualTo("502");
            assertThat(o.xpath()).isEqualTo("/NFe/infNFe/@Id");
            assertThat(o.valorEncontrado()).isEqualTo("NFe21261111222333000181550010000000011304050601");
            assertThat(o.valorEsperado()).isEqualTo("NFe2126111122233300018155001000000002130405060" + novoDv);
        });
        assertThat(regra253.validar(nfe(xml))).isEmpty();
    }

    @Test
    void cnpjAlfanumericoTrocadoNoEmitenteDispara502() {
        String alfa = XmlDeTeste.valido(XmlDeTeste.ALFANUMERICO_SP);
        String xml = trocar(alfa, "<CNPJ>Q7X2M9K4000140</CNPJ>", "<CNPJ>12ABC34501DE35</CNPJ>");

        assertThat(regra502.validar(nfe(xml))).extracting(Ocorrencia::codigo).containsExactly("502");
    }

    @Test
    void digitoErradoNoCdvENoIdDispara253() {
        // cDV e último dígito do Id mudam juntos: o Id corresponde aos campos, mas o dígito não confere.
        String xml = trocar(trocar(base, "<cDV>1</cDV>", "<cDV>5</cDV>"),
                "Id=\"NFe21261111222333000181550010000000011304050601\"",
                "Id=\"NFe21261111222333000181550010000000011304050605\"");

        List<Ocorrencia> ocorrencias = regra253.validar(nfe(xml));

        assertThat(ocorrencias).singleElement().satisfies(o -> {
            assertThat(o.codigo()).isEqualTo("253");
            assertThat(o.xpath()).isEqualTo("/NFe/infNFe/ide/cDV");
            assertThat(o.valorEncontrado()).isEqualTo("5");
            assertThat(o.valorEsperado()).isEqualTo("1");
        });
        assertThat(regra502.validar(nfe(xml))).isEmpty();
    }

    @Test
    void semCampoDaChaveAsRegrasSaoIgnoradas() {
        String xml = trocar(base, "<cNF>30405060</cNF>", "");

        assertThat(regra502.validar(nfe(xml))).isEmpty();
        assertThat(regra253.validar(nfe(xml))).isEmpty();
    }

    @Test
    void semCdvESemIdAsRegrasSaoIgnoradas() {
        String semCdv = trocar(base, "<cDV>1</cDV>", "");
        String semId = trocar(base, "Id=\"NFe21261111222333000181550010000000011304050601\" ", "");

        assertThat(regra253.validar(nfe(semCdv))).isEmpty();
        assertThat(regra502.validar(nfe(semCdv))).isEmpty();
        assertThat(regra502.validar(nfe(semId))).isEmpty();
    }

    @Test
    void emitenteComCpfEhIgnorado() {
        // D009: emitente pessoa física fica fora do escopo.
        String xml = trocar(base, "<CNPJ>11222333000181</CNPJ>\n      <xNome>EMPRESA", "<CPF>11144477735</CPF>\n      <xNome>EMPRESA");

        assertThat(regra502.validar(nfe(xml))).isEmpty();
        assertThat(regra253.validar(nfe(xml))).isEmpty();
    }
}
