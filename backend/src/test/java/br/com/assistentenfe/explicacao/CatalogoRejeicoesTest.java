package br.com.assistentenfe.explicacao;

import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.regras.RegraValidacao;
import br.com.assistentenfe.regras.RegrasDisponiveis;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogoRejeicoesTest {

    private final CatalogoRejeicoes catalogo = CatalogoRejeicoes.carregar();

    @ParameterizedTest
    @ValueSource(strings = {"243", "225", "404"})
    void codigosDaFase1EstaoNoCatalogo(String codigo) {
        assertThat(catalogo.contem(codigo)).isTrue();
    }

    @Test
    void todaRegraDeNegocioTemExplicacaoCompleta() {
        for (RegraValidacao regra : RegrasDisponiveis.todas(Clock.systemUTC(), Duration.ZERO)) {
            Ocorrencia o = new Ocorrencia(regra.codigoRejeicao(), Camada.REGRAS_NEGOCIO,
                    "/NFe/infNFe/emit/CNPJ", "encontrado", "esperado", null);

            Explicacao e = catalogo.explicar(o);

            assertThat(e.completa()).as(regra.codigoRejeicao()).isTrue();
            assertThat(e.titulo()).as(regra.codigoRejeicao()).startsWith("Rejeição: ");
            assertThat(e.problema() + e.correcao()).as(regra.codigoRejeicao()).doesNotContain("{");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"formato", "lista", "tamanhoMaximo", "tamanhoMinimo", "tamanhoExato", "tipo",
            "elementoInesperado", "elementoFaltando", "elementoNaoPermitido", "textoNaoPermitido",
            "atributoNaoPermitido", "atributoFaltando", "raizDesconhecida", "generico"})
    void todoMotivoDo225GeraExplicacaoCompleta(String motivo) {
        Ocorrencia o = new Ocorrencia("225", Camada.ESTRUTURA, "/NFe/infNFe/ide/cUF", "99", "11, 12", motivo);

        Explicacao e = catalogo.explicar(o);

        assertThat(e.completa()).isTrue();
        assertThat(e.problema()).doesNotContain("{");
    }

    @ParameterizedTest
    @ValueSource(strings = {"conteudoAlterado", "valorDaAssinatura"})
    void todoMotivoDo297GeraExplicacaoCompleta(String motivo) {
        Ocorrencia o = new Ocorrencia("297", Camada.ESTRUTURA, "/NFe/Signature/SignatureValue", null, null, motivo);

        Explicacao e = catalogo.explicar(o);

        assertThat(e.completa()).isTrue();
        assertThat(e.titulo()).isEqualTo("Rejeição: Assinatura difere do calculado");
        assertThat(e.problema() + e.correcao()).doesNotContain("{");
    }

    @Test
    void explicacaoUsaOCampoEOsValoresDaOcorrencia() {
        Ocorrencia o = new Ocorrencia("225", Camada.ESTRUTURA, "/NFe/infNFe/det[2]/prod/vProd",
                "200,00", "padrão", "formato");

        Explicacao e = catalogo.explicar(o);

        assertThat(e.campo()).isEqualTo("vProd");
        assertThat(e.problema()).contains("\"200,00\"", "vProd");
    }

    @Test
    void prefixoDeNamespaceExplicaComoCorrigir() {
        Ocorrencia o = new Ocorrencia("404", Camada.ESTRUTURA, "/NFe", "nfe:NFe", "NFe", null);

        Explicacao e = catalogo.explicar(o);

        assertThat(e.completa()).isTrue();
        assertThat(e.campo()).isEqualTo("NFe");
        assertThat(e.correcao()).contains("<NFe>", "<nfe:NFe>");
    }

    @Test
    void codigoForaDoCatalogoGeraExplicacaoIncompleta() {
        Ocorrencia o = new Ocorrencia("999", Camada.REGRAS_NEGOCIO, null, null, null, null);

        assertThat(catalogo.explicar(o).completa()).isFalse();
    }
}
