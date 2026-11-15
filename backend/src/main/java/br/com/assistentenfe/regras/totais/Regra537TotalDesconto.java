package br.com.assistentenfe.regras.totais;

import br.com.assistentenfe.dominio.ItemNFe;

import java.util.Optional;

public final class Regra537TotalDesconto extends RegraSomaDosItens {

    @Override
    public String codigoRejeicao() {
        return "537";
    }

    @Override
    protected String campoTotal() {
        return "vDesc";
    }

    @Override
    protected Optional<String> valorDoItem(ItemNFe item) {
        return item.texto("prod/vDesc");
    }
}
