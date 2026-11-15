package br.com.assistentenfe.validacao;

import br.com.assistentenfe.XmlDeTeste;
import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.NFe;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.dominio.ResultadoValidacao;
import br.com.assistentenfe.estrutura.ValidadorSchema;
import br.com.assistentenfe.regras.RegraValidacao;
import br.com.assistentenfe.regras.RegrasDisponiveis;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static br.com.assistentenfe.XmlDeTeste.assinado;
import static br.com.assistentenfe.XmlDeTeste.bytes;
import static br.com.assistentenfe.XmlDeTeste.trocar;
import static org.assertj.core.api.Assertions.assertThat;

class ValidadorNFeTest {

    private static final ValidadorNFe validador = new ValidadorNFe(ValidadorSchema.nfe400(),
            RegrasDisponiveis.todas(Clock.fixed(Instant.parse("2026-11-12T12:00:00Z"), ZoneOffset.UTC), Duration.ZERO));

    static List<String> xmlsValidos() {
        return XmlDeTeste.nomesValidos();
    }

    @ParameterizedTest
    @MethodSource("xmlsValidos")
    void xmlValidoNaoGeraOcorrencias(String nome) {
        ResultadoValidacao resultado = validador.validar(bytes(XmlDeTeste.valido(nome)));

        assertThat(resultado.ocorrencias()).isEmpty();
        assertThat(resultado.valida()).isTrue();
    }

    @Test
    void xmlMalFormadoInterrompeAsCamadasSeguintes() {
        String xml = trocar(XmlDeTeste.valido(XmlDeTeste.BASE), "</natOp>", "</natop>");

        ResultadoValidacao resultado = validador.validar(bytes(xml));

        assertThat(resultado.ocorrencias()).extracting(Ocorrencia::codigo).containsExactly("243");
    }

    private static final RegraValidacao REGRA_FICTICIA = new RegraValidacao() {
        @Override
        public String codigoRejeicao() {
            return "999";
        }

        @Override
        public List<Ocorrencia> validar(NFe nfe) {
            return nfe.texto("/NFe/infNFe/ide/natOp")
                    .map(natOp -> List.of(new Ocorrencia("999", Camada.REGRAS_NEGOCIO,
                            "/NFe/infNFe/ide/natOp", natOp, null, null)))
                    .orElse(List.of());
        }
    };

    private static final ValidadorNFe comRegra = new ValidadorNFe(ValidadorSchema.nfe400(), List.of(REGRA_FICTICIA));

    @Test
    void regrasRodamDepoisDeFalhaNoSchemaESaoSinalizadas() {
        String xml = assinado(trocar(XmlDeTeste.valido(XmlDeTeste.BASE), "<cUF>21</cUF>", "<cUF>99</cUF>"));

        ResultadoValidacao resultado = comRegra.validar(bytes(xml));

        assertThat(resultado.ocorrencias()).extracting(Ocorrencia::codigo).containsExactly("225", "999");
        assertThat(resultado.regrasApesarDeFalhaNoSchema()).isTrue();
    }

    @Test
    void regraSemOCampoNecessarioEhIgnorada() {
        String xml = assinado(trocar(XmlDeTeste.valido(XmlDeTeste.BASE), "<natOp>VENDA DE MERCADORIA</natOp>", ""));

        ResultadoValidacao resultado = comRegra.validar(bytes(xml));

        assertThat(resultado.ocorrencias()).extracting(Ocorrencia::codigo).containsExactly("225");
        assertThat(resultado.regrasApesarDeFalhaNoSchema()).isFalse();
    }

    @Test
    void semFalhaNoSchemaNaoHaSinalizacao() {
        ResultadoValidacao resultado = comRegra.validar(bytes(XmlDeTeste.valido(XmlDeTeste.BASE)));

        assertThat(resultado.ocorrencias()).extracting(Ocorrencia::codigo).containsExactly("999");
        assertThat(resultado.regrasApesarDeFalhaNoSchema()).isFalse();
    }

    @Test
    void prefixoENamespaceSaoReportadosJuntos() {
        String xml = XmlDeTeste.valido(XmlDeTeste.BASE)
                .replace("<NFe xmlns=", "<nfe:NFe xmlns:nfe=")
                .replace("</NFe>", "</nfe:NFe>");

        ResultadoValidacao resultado = validador.validar(bytes(xml));

        assertThat(resultado.ocorrencias()).extracting(Ocorrencia::codigo).contains("404", "225");
    }

    @Test
    void campoAlteradoDepoisDeAssinarDispara297NaCamadaDeEstrutura() {
        String xml = trocar(XmlDeTeste.valido(XmlDeTeste.BASE), "<natOp>VENDA DE MERCADORIA</natOp>",
                "<natOp>VENDA DE MERCADORIAS</natOp>");

        ResultadoValidacao resultado = validador.validar(bytes(xml));

        assertThat(resultado.ocorrencias()).singleElement().satisfies(o -> {
            assertThat(o.codigo()).isEqualTo("297");
            assertThat(o.camada()).isEqualTo(Camada.ESTRUTURA);
            assertThat(o.motivo()).isEqualTo("conteudoAlterado");
        });
    }
}
