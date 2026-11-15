package br.com.assistentenfe.regras.totais;

import br.com.assistentenfe.dominio.ItemNFe;

import java.util.Optional;

public final class Regra531TotalBcIcms extends RegraSomaDosItens {

    @Override
    public String codigoRejeicao() {
        return "531";
    }

    @Override
    protected String campoTotal() {
        return "vBC";
    }

    @Override
    protected Optional<String> valorDoItem(ItemNFe item) {
        return item.noGrupoIcms("vBC");
    }
}
