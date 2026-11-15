package br.com.assistentenfe.api;

import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.dominio.ResultadoValidacao;
import br.com.assistentenfe.explicacao.CatalogoRejeicoes;
import br.com.assistentenfe.validacao.ValidadorNFe;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/validacoes")
public class ValidacaoController {

    private final ValidadorNFe validador;
    private final CatalogoRejeicoes catalogo;

    public ValidacaoController(ValidadorNFe validador, CatalogoRejeicoes catalogo) {
        this.validador = validador;
        this.catalogo = catalogo;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DiagnosticoResposta validar(@RequestPart("arquivo") MultipartFile arquivo) throws IOException {
        ResultadoValidacao resultado = validador.validar(arquivo.getBytes());

        Map<Camada, List<OcorrenciaResposta>> porCamada = new EnumMap<>(Camada.class);
        for (Ocorrencia o : resultado.ocorrencias()) {
            porCamada.computeIfAbsent(o.camada(), c -> new ArrayList<>())
                    .add(OcorrenciaResposta.de(o, catalogo.explicar(o)));
        }
        return new DiagnosticoResposta(resultado.valida(), resultado.regrasApesarDeFalhaNoSchema(), porCamada);
    }
}
