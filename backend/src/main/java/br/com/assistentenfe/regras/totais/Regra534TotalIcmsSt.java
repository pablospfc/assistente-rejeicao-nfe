package br.com.assistentenfe.regras.totais;

import br.com.assistentenfe.dominio.ItemNFe;

import java.util.Optional;

/** 534: vST (W06) = soma de vICMSST (N23) do grupo de ICMS de cada item (Anexo I, W06). */
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
