package br.com.assistentenfe.regras.totais;

import br.com.assistentenfe.dominio.ItemNFe;

import java.util.Optional;

public final class Regra533TotalBcIcmsSt extends RegraSomaDosItens {

    @Override
    public String codigoRejeicao() {
        return "533";
    }

    @Override
    protected String campoTotal() {
        return "vBCST";
    }

    @Override
    protected Optional<String> valorDoItem(ItemNFe item) {
        return item.noGrupoIcms("vBCST");
    }
}
