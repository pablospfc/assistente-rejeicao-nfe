package br.com.assistentenfe.estrutura;

import br.com.assistentenfe.XmlDeTeste;
import br.com.assistentenfe.dominio.Ocorrencia;
import org.junit.jupiter.api.Test;

import static br.com.assistentenfe.XmlDeTeste.bytes;
import static br.com.assistentenfe.XmlDeTeste.trocar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class VerificadorXmlBemFormadoTest {

    private final VerificadorXmlBemFormado verificador = new VerificadorXmlBemFormado();

    @Test
    void leXmlValido() {
        assertThatCode(() -> verificador.ler(bytes(XmlDeTeste.valido(XmlDeTeste.BASE))))
                .doesNotThrowAnyException();
    }

    @Test
    void tagSemFechamentoGera243ComPosicao() {
        String xml = trocar(XmlDeTeste.valido(XmlDeTeste.BASE), "</natOp>", "");

        Ocorrencia o = erro(xml);

        assertThat(o.codigo()).isEqualTo("243");
        assertThat(o.valorEncontrado()).matches("linha \\d+, coluna \\d+");
    }

    @Test
    void ecomercialSemEscapeGera243() {
        String xml = trocar(XmlDeTeste.valido(XmlDeTeste.BASE),
                "EMPRESA FICTICIA DE TESTES LTDA", "EMPRESA FICTICIA & FILHOS LTDA");

        assertThat(erro(xml).codigo()).isEqualTo("243");
    }

    @Test
    void doctypeNaoEhAceito() {
        String xml = trocar(XmlDeTeste.valido(XmlDeTeste.BASE),
                "<NFe xmlns", "<!DOCTYPE NFe [<!ENTITY x SYSTEM \"file:///etc/passwd\">]><NFe xmlns");

        assertThat(erro(xml).codigo()).isEqualTo("243");
    }

    @Test
    void arquivoVazioGera243() {
        assertThat(erro("").codigo()).isEqualTo("243");
    }

    private Ocorrencia erro(String xml) {
        XmlMalFormadoException e = catchThrowableOfType(XmlMalFormadoException.class,
                () -> verificador.ler(bytes(xml)));
        assertThat(e).as("deveria ser XML mal formado").isNotNull();
        return e.ocorrencia();
    }
}
