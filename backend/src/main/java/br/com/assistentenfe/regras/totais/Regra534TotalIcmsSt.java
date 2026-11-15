package br.com.assistentenfe.regras.totais;

import br.com.assistentenfe.dominio.ItemNFe;

import java.util.Optional;

public final class Regra534TotalIcmsSt extends RegraSomaDosItens {

    @Override
    public String codigoRejeicao() {
        return "534";
    }

    @Override
    protected String campoTotal() {
        return "vST";
    }

    @Override
    protected Optional<String> valorDoItem(ItemNFe item) {
        return item.noGrupoIcms("vICMSST");
    }
}
