package br.com.assistentenfe.regras.totais;

import br.com.assistentenfe.dominio.ItemNFe;

import java.util.Optional;

public final class Regra536TotalSeguro extends RegraSomaDosItens {

    @Override
    public String codigoRejeicao() {
        return "536";
    }

    @Override
    protected String campoTotal() {
        return "vSeg";
    }

    @Override
    protected Optional<String> valorDoItem(ItemNFe item) {
        return item.texto("prod/vSeg");
    }
}
