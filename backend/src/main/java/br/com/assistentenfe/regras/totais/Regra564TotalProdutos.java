package br.com.assistentenfe.regras.totais;

import br.com.assistentenfe.dominio.ItemNFe;

import java.util.Optional;

public final class Regra564TotalProdutos extends RegraSomaDosItens {

    @Override
    public String codigoRejeicao() {
        return "564";
    }

    @Override
    protected String campoTotal() {
        return "vProd";
    }

    @Override
    protected Optional<String> valorDoItem(ItemNFe item) {
        return item.texto("prod/vProd");
    }

    @Override
    protected boolean considera(ItemNFe item) {
        boolean compoeTotal = item.texto("prod/indTot").map("1"::equals).orElse(false);
        return compoeTotal && item.grupoIcms().isPresent();
    }
}
