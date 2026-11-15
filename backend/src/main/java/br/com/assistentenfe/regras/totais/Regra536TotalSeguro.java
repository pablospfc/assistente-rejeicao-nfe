package br.com.assistentenfe.regras.totais;

import br.com.assistentenfe.dominio.ItemNFe;

import java.util.Optional;

/** 536: vSeg (W09) = soma de prod/vSeg (I16) dos itens (Anexo I, W12a-10). */
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
