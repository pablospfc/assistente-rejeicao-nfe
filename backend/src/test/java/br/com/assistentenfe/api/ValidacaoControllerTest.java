package br.com.assistentenfe.api;

import br.com.assistentenfe.XmlDeTeste;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static br.com.assistentenfe.XmlDeTeste.bytes;
import static br.com.assistentenfe.XmlDeTeste.trocar;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ValidacaoControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void xmlValidoRetornaDiagnosticoSemOcorrencias() throws Exception {
        mvc.perform(multipart("/api/validacoes").file(arquivo(XmlDeTeste.valido(XmlDeTeste.BASE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valida").value(true))
                .andExpect(jsonPath("$.regrasApesarDeFalhaNoSchema").value(false))
                .andExpect(jsonPath("$.camadas").isEmpty());
    }

    @Test
    void erroDeSchemaVemExplicadoNaCamadaDeEstrutura() throws Exception {
        String xml = trocar(XmlDeTeste.valido(XmlDeTeste.BASE), "<cUF>21</cUF>", "<cUF>99</cUF>");

        mvc.perform(multipart("/api/validacoes").file(arquivo(xml)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valida").value(false))
                .andExpect(jsonPath("$.camadas.ESTRUTURA[0].codigo").value("225"))
                .andExpect(jsonPath("$.camadas.ESTRUTURA[0].xpath").value("/NFe/infNFe/ide/cUF"))
                .andExpect(jsonPath("$.camadas.ESTRUTURA[0].campo").value("cUF"))
                .andExpect(jsonPath("$.camadas.ESTRUTURA[0].correcao").isNotEmpty());
    }

    @Test
    void requisicaoSemArquivoRetorna400() throws Exception {
        mvc.perform(multipart("/api/validacoes"))
                .andExpect(status().isBadRequest());
    }

    private static MockMultipartFile arquivo(String xml) {
        return new MockMultipartFile("arquivo", "nfe.xml", "application/xml", bytes(xml));
    }
}
