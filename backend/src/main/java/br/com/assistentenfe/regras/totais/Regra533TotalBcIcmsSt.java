package br.com.assistentenfe.regras.totais;

import br.com.assistentenfe.dominio.ItemNFe;

import java.util.Optional;

/** 533: vBCST (W05) = soma de vBCST (N21) do grupo de ICMS de cada item (Anexo I, W05). */
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
