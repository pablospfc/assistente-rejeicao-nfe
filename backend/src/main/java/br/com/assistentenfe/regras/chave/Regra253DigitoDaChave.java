package br.com.assistentenfe.regras.chave;

import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.ChaveAcesso;
import br.com.assistentenfe.dominio.NFe;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.regras.RegraValidacao;

import java.util.List;
import java.util.Optional;

public final class Regra253DigitoDaChave implements RegraValidacao {

    @Override
    public String codigoRejeicao() {
        return "253";
    }

    @Override
    public List<Ocorrencia> validar(NFe nfe) {
        Optional<ChaveAcesso> chave = ChaveAcesso.compor(nfe);
        Optional<String> cDV = nfe.texto("/NFe/infNFe/ide/cDV");
        if (chave.isEmpty() || cDV.isEmpty()) {
            return List.of();
        }

        String calculado = String.valueOf(chave.get().digitoCalculado());
        if (cDV.get().equals(calculado)) {
            return List.of();
        }
        return List.of(new Ocorrencia(codigoRejeicao(), Camada.REGRAS_NEGOCIO,
                "/NFe/infNFe/ide/cDV", cDV.get(), calculado, null));
    }
}
