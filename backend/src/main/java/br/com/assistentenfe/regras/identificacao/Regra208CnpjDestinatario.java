package br.com.assistentenfe.regras.identificacao;

import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.DigitoVerificador;
import br.com.assistentenfe.dominio.NFe;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.regras.RegraValidacao;

import java.util.List;

public final class Regra208CnpjDestinatario implements RegraValidacao {

    private static final String CAMPO = "/NFe/infNFe/dest/CNPJ";

    @Override
    public String codigoRejeicao() {
        return "208";
    }

    @Override
    public List<Ocorrencia> validar(NFe nfe) {
        return nfe.texto(CAMPO)
                .filter(cnpj -> !DigitoVerificador.cnpjValido(cnpj))
                .map(cnpj -> List.of(new Ocorrencia(codigoRejeicao(), Camada.REGRAS_NEGOCIO,
                        CAMPO, cnpj, null, null)))
                .orElse(List.of());
    }
}
