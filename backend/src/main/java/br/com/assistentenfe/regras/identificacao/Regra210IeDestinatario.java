package br.com.assistentenfe.regras.identificacao;

import br.com.assistentenfe.dominio.NFe;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.regras.RegraValidacao;

import java.util.List;

public final class Regra210IeDestinatario implements RegraValidacao {

    @Override
    public String codigoRejeicao() {
        return "210";
    }

    @Override
    public List<Ocorrencia> validar(NFe nfe) {
        return ConferenciaIE.conferir(nfe, codigoRejeicao(),
                "/NFe/infNFe/dest/IE", "/NFe/infNFe/dest/enderDest/UF");
    }
}
