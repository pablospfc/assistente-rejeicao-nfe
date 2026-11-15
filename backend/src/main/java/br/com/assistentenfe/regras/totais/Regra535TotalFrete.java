package br.com.assistentenfe.regras.totais;

import br.com.assistentenfe.dominio.ItemNFe;

import java.util.Optional;

/** 535: vFrete (W08) = soma de prod/vFrete (I15) dos itens (Anexo I, W12-10). */
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
