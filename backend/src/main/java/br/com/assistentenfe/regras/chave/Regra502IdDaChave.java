package br.com.assistentenfe.regras.chave;

import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.ChaveAcesso;
import br.com.assistentenfe.dominio.NFe;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.regras.RegraValidacao;

import java.util.List;
import java.util.Optional;

/**
 * 502: o atributo {@code Id} de {@code infNFe} deve ser "NFe" + a chave composta
 * a partir dos campos + {@code cDV}. Qualquer campo da chave alterado cai aqui (D008).
 */
public final class Regra502IdDaChave implements RegraValidacao {

    @Override
    public String codigoRejeicao() {
        return "502";
    }

    @Override
    public List<Ocorrencia> validar(NFe nfe) {
        Optional<ChaveAcesso> chave = ChaveAcesso.compor(nfe);
        Optional<String> cDV = nfe.texto("/NFe/infNFe/ide/cDV");
        Optional<String> id = nfe.atributo("/NFe/infNFe", "Id");
        if (chave.isEmpty() || cDV.isEmpty() || id.isEmpty()) {
            return List.of();
        }

        String esperado = "NFe" + chave.get().semDigito() + cDV.get();
        if (id.get().equals(esperado)) {
            return List.of();
        }
        return List.of(new Ocorrencia(codigoRejeicao(), Camada.REGRAS_NEGOCIO,
                "/NFe/infNFe/@Id", id.get(), esperado, null));
    }
}
