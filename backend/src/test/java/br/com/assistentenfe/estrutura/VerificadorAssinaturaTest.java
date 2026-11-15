package br.com.assistentenfe.estrutura;

import br.com.assistentenfe.XmlDeTeste;
import br.com.assistentenfe.assinatura.AssinadorNFe;
import br.com.assistentenfe.assinatura.CertificadoTeste;
import br.com.assistentenfe.dominio.Ocorrencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static br.com.assistentenfe.XmlDeTeste.bytes;
import static br.com.assistentenfe.XmlDeTeste.trocar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerificadorAssinaturaTest {

    private final VerificadorAssinatura verificador = new VerificadorAssinatura();
    private final String base = XmlDeTeste.valido(XmlDeTeste.BASE);

    static List<String> xmlsValidos() {
        return XmlDeTeste.nomesValidos();
    }

    @ParameterizedTest
    @MethodSource("xmlsValidos")
    void xmlsDeBaseAssinadosPassam(String nome) throws Exception {
        assertThat(verificar(XmlDeTeste.valido(nome))).isEmpty();
    }

    @Test
    void campoAlteradoDepoisDeAssinar() throws Exception {
        String xml = trocar(base, "<vProd>300.00</vProd>", "<vProd>301.00</vProd>");

        assertThat(verificar(xml)).singleElement().satisfies(o -> esperado(o, "conteudoAlterado", "/NFe/infNFe"));
    }

    @Test
    void signatureValueAlterado() throws Exception {
        String xml = alterarConteudo(base, "SignatureValue", 10);

        assertThat(verificar(xml)).singleElement()
                .satisfies(o -> esperado(o, "valorDaAssinatura", "/NFe/Signature/SignatureValue"));
    }

    @Test
    void digestValueAlterado() throws Exception {
        String xml = alterarConteudo(base, "DigestValue", 5);

        assertThat(verificar(xml)).singleElement()
                .satisfies(o -> esperado(o, "valorDaAssinatura", "/NFe/Signature/SignatureValue"));
    }

    @Test
    void certificadoTrocadoPorOutraChave() throws Exception {
        String outro = Base64.getEncoder().encodeToString(CertificadoTeste.gerar(2048).certificado().getEncoded());
        String xml = base.replaceFirst("(?s)<X509Certificate>.*?</X509Certificate>",
                "<X509Certificate>" + outro + "</X509Certificate>");

        assertThat(verificar(xml)).singleElement()
                .satisfies(o -> esperado(o, "valorDaAssinatura", "/NFe/Signature/SignatureValue"));
    }

    @Test
    void certificadoRealComSignatureValueDePreenchimento() throws Exception {
        String xml = base.replaceFirst("(?s)<SignatureValue>.*?</SignatureValue>", "<SignatureValue>AAAA</SignatureValue>");

        assertThat(verificar(xml)).singleElement()
                .satisfies(o -> esperado(o, "valorDaAssinatura", "/NFe/Signature/SignatureValue"));
    }

    @Test
    void assinaturaDePreenchimentoDoDiscoEhIgnorada() throws Exception {
        assertThat(verificar(XmlDeTeste.noDisco(XmlDeTeste.BASE))).isEmpty();
    }

    @Test
    void semAssinaturaO297EhIgnorado() throws Exception {
        String xml = base.replaceFirst("(?s)<Signature .*</Signature>", "");

        assertThat(verificar(xml)).isEmpty();
    }

    @Test
    void referenciaExternaNaoEhLida() throws Exception {
        String xml = base.replaceFirst("URI=\"#NFe[0-9A-Z]+\"", "URI=\"file:///C:/Windows/win.ini\"");

        assertThat(verificar(xml)).isEmpty();
    }

    @Test
    void dereferenciadorSoAceitaOInfNFe() {
        var dereferenciador = new VerificadorAssinatura.SomenteInfNFe("#NFe123",
                XMLSignatureFactory.getInstance("DOM").getURIDereferencer());
        URIReference externa = new URIReference() {
            @Override
            public String getURI() {
                return "file:///etc/passwd";
            }

            @Override
            public String getType() {
                return null;
            }
        };

        assertThatThrownBy(() -> dereferenciador.dereference(externa, null)).isInstanceOf(URIReferenceException.class);
    }

    @Test
    void chaveRsaMenorQue1024BitsEhRecusada() throws Exception {
        String xml = AssinadorNFe.assinar(XmlDeTeste.noDisco(XmlDeTeste.BASE), CertificadoTeste.gerar(512));

        assertThat(verificar(xml)).isEmpty();
    }

    private List<Ocorrencia> verificar(String xml) throws XmlMalFormadoException {
        return verificador.verificar(new VerificadorXmlBemFormado().ler(bytes(xml)));
    }

    private static void esperado(Ocorrencia o, String motivo, String xpath) {
        assertThat(o.codigo()).isEqualTo("297");
        assertThat(o.motivo()).isEqualTo(motivo);
        assertThat(o.xpath()).isEqualTo(xpath);
    }

    private static String alterarConteudo(String xml, String elemento, int posicao) {
        Matcher m = Pattern.compile("<" + elemento + ">([^<]+)</" + elemento + ">").matcher(xml);
        if (!m.find()) {
            throw new IllegalArgumentException("Sem " + elemento);
        }
        char[] valor = m.group(1).toCharArray();
        valor[posicao] = valor[posicao] == 'A' ? 'B' : 'A';
        return xml.substring(0, m.start(1)) + new String(valor) + xml.substring(m.end(1));
    }
}
