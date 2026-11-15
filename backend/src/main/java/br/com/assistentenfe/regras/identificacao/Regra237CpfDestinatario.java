package br.com.assistentenfe.regras.identificacao;

import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.DigitoVerificador;
import br.com.assistentenfe.dominio.NFe;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.regras.RegraValidacao;

import java.util.List;

public final class Regra237CpfDestinatario implements RegraValidacao {

    private static final String CAMPO = "/NFe/infNFe/dest/CPF";

    @Override
    public String codigoRejeicao() {
        return "237";
    }

    @Override
    public List<Ocorrencia> validar(NFe nfe) {
        return nfe.texto(CAMPO)
                .filter(cpf -> !DigitoVerificador.cpfValido(cpf))
                .map(cpf -> List.of(new Ocorrencia(codigoRejeicao(), Camada.REGRAS_NEGOCIO,
                        CAMPO, cpf, null, null)))
                .orElse(List.of());
    }
}
