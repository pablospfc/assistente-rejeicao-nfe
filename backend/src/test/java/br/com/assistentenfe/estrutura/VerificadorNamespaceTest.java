package br.com.assistentenfe.estrutura;

import br.com.assistentenfe.XmlDeTeste;
import br.com.assistentenfe.dominio.Ocorrencia;
import org.junit.jupiter.api.Test;

import java.util.List;

import static br.com.assistentenfe.XmlDeTeste.bytes;
import static org.assertj.core.api.Assertions.assertThat;

class VerificadorNamespaceTest {

    private final VerificadorNamespace verificador = new VerificadorNamespace();

    @Test
    void xmlSemPrefixoPassa() throws Exception {
        assertThat(verificar(XmlDeTeste.valido(XmlDeTeste.BASE))).isEmpty();
    }

    @Test
    void prefixoNaRaizGera404() throws Exception {
        String xml = XmlDeTeste.valido(XmlDeTeste.BASE)
                .replace("<NFe xmlns=", "<nfe:NFe xmlns:nfe=")
                .replace("</NFe>", "</nfe:NFe>");

        List<Ocorrencia> ocorrencias = verificar(xml);

        assertThat(ocorrencias).singleElement().satisfies(o -> {
            assertThat(o.codigo()).isEqualTo("404");
            assertThat(o.xpath()).isEqualTo("/NFe");
            assertThat(o.valorEncontrado()).isEqualTo("nfe:NFe");
            assertThat(o.valorEsperado()).isEqualTo("NFe");
        });
    }

    @Test
    void prefixoNaAssinaturaGeraUmaOcorrenciaPorPrefixo() throws Exception {
        String xml = XmlDeTeste.valido(XmlDeTeste.BASE)
                .replaceAll("<(/?)(Signature|SignedInfo|SignatureValue|KeyInfo)\\b", "<$1ds:$2")
                .replace("<ds:Signature xmlns=", "<ds:Signature xmlns:ds=");

        List<Ocorrencia> ocorrencias = verificar(xml);

        assertThat(ocorrencias).singleElement().satisfies(o -> {
            assertThat(o.xpath()).isEqualTo("/NFe/Signature");
            assertThat(o.valorEncontrado()).isEqualTo("ds:Signature");
        });
    }

    private List<Ocorrencia> verificar(String xml) throws XmlMalFormadoException {
        return verificador.verificar(new VerificadorXmlBemFormado().ler(bytes(xml)));
    }
}
