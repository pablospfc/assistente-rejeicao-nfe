package br.com.assistentenfe.regras.totais;

import br.com.assistentenfe.dominio.ItemNFe;

import java.util.Optional;

/** 531: vBC (W03) = soma de vBC (N15) do grupo de ICMS de cada item (Anexo I, W03-20). */
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
