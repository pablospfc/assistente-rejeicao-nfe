package br.com.assistentenfe.regras.totais;

import br.com.assistentenfe.dominio.ItemNFe;

import java.util.Optional;

public final class Regra535TotalFrete extends RegraSomaDosItens {

    @Override
    public String codigoRejeicao() {
        return "535";
    }

    @Override
    protected String campoTotal() {
        return "vFrete";
    }

    @Override
    protected Optional<String> valorDoItem(ItemNFe item) {
        return item.texto("prod/vFrete");
    }
}
