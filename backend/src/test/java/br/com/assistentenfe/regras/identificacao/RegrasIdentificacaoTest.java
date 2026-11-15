package br.com.assistentenfe.regras.identificacao;

import br.com.assistentenfe.XmlDeTeste;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.regras.RegraValidacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static br.com.assistentenfe.XmlDeTeste.nfe;
import static br.com.assistentenfe.XmlDeTeste.trocar;
import static org.assertj.core.api.Assertions.assertThat;

class RegrasIdentificacaoTest {

    /** "Agora" fixo: 12/11/2026 09:00 em -03:00. */
    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-11-12T12:00:00Z"), ZoneOffset.UTC);

    private final RegraValidacao regra703 = new Regra703DataEmissao(RELOGIO, Duration.ofMinutes(5));
    private final RegraValidacao regra207 = new Regra207CnpjEmitente();
    private final RegraValidacao regra208 = new Regra208CnpjDestinatario();
    private final RegraValidacao regra237 = new Regra237CpfDestinatario();
    private final RegraValidacao regra209 = new Regra209IeEmitente();
    private final RegraValidacao regra210 = new Regra210IeDestinatario();

    private final String base = XmlDeTeste.valido(XmlDeTeste.BASE);
    private final String alfanumericoSp = XmlDeTeste.valido(XmlDeTeste.ALFANUMERICO_SP);
    private final String contribuintePr = XmlDeTeste.valido(XmlDeTeste.CONTRIBUINTE_PR);

    static List<String> xmlsValidos() {
        return XmlDeTeste.nomesValidos();
    }

    @ParameterizedTest
    @MethodSource("xmlsValidos")
    void xmlsValidosPassamEmTodasAsRegras(String nome) {
        String xml = XmlDeTeste.valido(nome);

        for (RegraValidacao regra : List.of(regra703, regra207, regra208, regra237, regra209, regra210)) {
            assertThat(regra.validar(nfe(xml))).as(regra.codigoRejeicao()).isEmpty();
        }
    }

    // --- 703: data-hora de emissão ----------------------------------------------------

    @Test
    void emissaoNoFuturoDispara703() {
        String xml = comDhEmi("2026-11-12T10:00:00-03:00");

        assertThat(regra703.validar(nfe(xml))).singleElement().satisfies(o -> {
            assertThat(o.codigo()).isEqualTo("703");
            assertThat(o.xpath()).isEqualTo("/NFe/infNFe/ide/dhEmi");
            assertThat(o.valorEncontrado()).isEqualTo("2026-11-12T10:00:00-03:00");
            assertThat(o.valorEsperado()).isEqualTo("2026-11-12T09:00:00-03:00");
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-11-12T09:00:00-03:00", "2026-11-12T09:04:00-03:00", "2026-11-12T09:05:00-03:00"})
    void diferencaDeAteCincoMinutosPassa(String dhEmi) {
        // MOC 7.0: tolerância de até 5 minutos pelo sincronismo de horário.
        assertThat(regra703.validar(nfe(comDhEmi(dhEmi)))).isEmpty();
    }

    @Test
    void diferencaAcimaDeCincoMinutosDispara703() {
        assertThat(regra703.validar(nfe(comDhEmi("2026-11-12T09:06:00-03:00")))).hasSize(1);
    }

    @Test
    void toleranciaEhConfiguravel() {
        RegraValidacao semTolerancia = new Regra703DataEmissao(RELOGIO, Duration.ZERO);

        assertThat(semTolerancia.validar(nfe(comDhEmi("2026-11-12T09:04:00-03:00")))).hasSize(1);
    }

    @Test
    void fusoDoDhEmiEhRespeitado() {
        // 12:04 em UTC é o mesmo instante que 09:04 em -03:00: dentro da tolerância.
        assertThat(regra703.validar(nfe(comDhEmi("2026-11-12T12:04:00+00:00")))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "<dhEmi>ontem</dhEmi>"})
    void semDataLegivelO703EhIgnorado(String substituto) {
        String xml = trocar(base, "<dhEmi>2026-11-02T10:00:00-03:00</dhEmi>", substituto);

        assertThat(regra703.validar(nfe(xml))).isEmpty();
    }

    private String comDhEmi(String dhEmi) {
        return trocar(base, "2026-11-02T10:00:00-03:00", dhEmi);
    }

    // --- 207 e 208: CNPJ ----------------------------------------------------------------

    @Test
    void cnpjNumericoDoEmitenteInvalidoDispara207() {
        String xml = trocar(base, "<CNPJ>11222333000181</CNPJ>\n      <xNome>EMPRESA",
                "<CNPJ>11222333000182</CNPJ>\n      <xNome>EMPRESA");

        assertThat(regra207.validar(nfe(xml))).singleElement().satisfies(o -> {
            assertThat(o.codigo()).isEqualTo("207");
            assertThat(o.xpath()).isEqualTo("/NFe/infNFe/emit/CNPJ");
            assertThat(o.valorEncontrado()).isEqualTo("11222333000182");
        });
    }

    @Test
    void cnpjAlfanumericoDoEmitenteInvalidoDispara207() {
        String xml = trocar(alfanumericoSp, "<CNPJ>Q7X2M9K4000140</CNPJ>", "<CNPJ>Q7X2M9K4000141</CNPJ>");

        assertThat(regra207.validar(nfe(xml))).extracting(Ocorrencia::codigo).containsExactly("207");
    }

    @Test
    void emitenteComCpfNaoPassaPelo207() {
        String xml = trocar(base, "<CNPJ>11222333000181</CNPJ>\n      <xNome>EMPRESA",
                "<CPF>11144477735</CPF>\n      <xNome>EMPRESA");

        assertThat(regra207.validar(nfe(xml))).isEmpty();
    }

    @Test
    void cnpjDoDestinatarioInvalidoDispara208() {
        String xml = trocar(alfanumericoSp, "<CNPJ>55666777000181</CNPJ>", "<CNPJ>55666777000182</CNPJ>");

        assertThat(regra208.validar(nfe(xml))).singleElement().satisfies(o -> {
            assertThat(o.codigo()).isEqualTo("208");
            assertThat(o.xpath()).isEqualTo("/NFe/infNFe/dest/CNPJ");
        });
    }

    @Test
    void destinatarioSemCnpjNaoPassaPelo208() {
        // No XML de base, o destinatário é pessoa física.
        assertThat(regra208.validar(nfe(base))).isEmpty();
    }

    // --- 237: CPF -----------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"11144477736", "11111111111"})
    void cpfDoDestinatarioInvalidoDispara237(String cpf) {
        String xml = trocar(base, "<CPF>11144477735</CPF>", "<CPF>" + cpf + "</CPF>");

        assertThat(regra237.validar(nfe(xml))).singleElement().satisfies(o -> {
            assertThat(o.codigo()).isEqualTo("237");
            assertThat(o.xpath()).isEqualTo("/NFe/infNFe/dest/CPF");
            assertThat(o.valorEncontrado()).isEqualTo(cpf);
        });
    }

    @Test
    void destinatarioSemCpfNaoPassaPelo237() {
        assertThat(regra237.validar(nfe(alfanumericoSp))).isEmpty();
    }

    // --- 209 e 210: IE ------------------------------------------------------------------

    @Test
    void ieDoEmitenteInvalidaDispara209() {
        String xml = trocar(base, "<IE>123456789</IE>", "<IE>123456788</IE>");

        assertThat(regra209.validar(nfe(xml))).singleElement().satisfies(o -> {
            assertThat(o.codigo()).isEqualTo("209");
            assertThat(o.xpath()).isEqualTo("/NFe/infNFe/emit/IE");
            assertThat(o.valorEncontrado()).isEqualTo("123456788");
            assertThat(o.valorEsperado()).isEqualTo("MA");
        });
    }

    @Test
    void emitenteIsentoPassa() {
        String xml = trocar(base, "<IE>123456789</IE>", "<IE>ISENTO</IE>");

        assertThat(regra209.validar(nfe(xml))).isEmpty();
    }

    @Test
    void ieDoEmitenteZeradaNaoEh209() {
        // MOC 7.0: IE do emitente com zeros é o 229, fora do escopo.
        String xml = trocar(base, "<IE>123456789</IE>", "<IE>000000000</IE>");

        assertThat(regra209.validar(nfe(xml))).isEmpty();
    }

    @Test
    void ufSemEstrategiaEhIgnorada() {
        String xml = trocar(trocar(base, "<IE>123456789</IE>", "<IE>123456788</IE>"),
                "<UF>MA</UF>\n        <CEP>65000000</CEP>", "<UF>PI</UF>\n        <CEP>65000000</CEP>");

        assertThat(regra209.validar(nfe(xml))).isEmpty();
    }

    @Test
    void ieDoDestinatarioInvalidaEmSpDispara210() {
        String xml = trocar(alfanumericoSp, "<IE>234567895011</IE>", "<IE>234567895012</IE>");

        assertThat(regra210.validar(nfe(xml))).singleElement().satisfies(o -> {
            assertThat(o.codigo()).isEqualTo("210");
            assertThat(o.xpath()).isEqualTo("/NFe/infNFe/dest/IE");
            assertThat(o.valorEsperado()).isEqualTo("SP");
        });
    }

    @Test
    void ieDoDestinatarioInvalidaNoPrDispara210() {
        String xml = trocar(contribuintePr, "<IE>9012345663</IE>", "<IE>9012345664</IE>");

        assertThat(regra210.validar(nfe(xml))).extracting(Ocorrencia::valorEsperado).containsExactly("PR");
    }

    @Test
    void semIeOuSemUfAsRegrasDeIeSaoIgnoradas() {
        String semIeEmit = trocar(base, "<IE>123456789</IE>", "");
        String semUfDest = trocar(trocar(alfanumericoSp, "<IE>234567895011</IE>", "<IE>234567895012</IE>"),
                "<UF>SP</UF>", "");

        assertThat(regra209.validar(nfe(semIeEmit))).isEmpty();
        assertThat(regra210.validar(nfe(base))).isEmpty();
        assertThat(regra210.validar(nfe(semUfDest))).isEmpty();
    }
}
