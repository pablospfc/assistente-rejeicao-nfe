package br.com.assistentenfe.regras.identificacao;

import br.com.assistentenfe.dominio.NFe;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.regras.RegraValidacao;

import java.util.List;

public final class Regra209IeEmitente implements RegraValidacao {

    private static final String CAMPO = "/NFe/infNFe/emit/IE";

    @Override
    public String codigoRejeicao() {
        return "209";
    }

    @Override
    public List<Ocorrencia> validar(NFe nfe) {
        if (nfe.texto(CAMPO).filter(ie -> ie.matches("0+")).isPresent()) {
            return List.of();
        }
        return ConferenciaIE.conferir(nfe, codigoRejeicao(), CAMPO, "/NFe/infNFe/emit/enderEmit/UF");
    }
}
