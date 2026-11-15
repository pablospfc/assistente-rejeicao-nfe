package br.com.assistentenfe.regras.totais;

import br.com.assistentenfe.dominio.ItemNFe;
import br.com.assistentenfe.dominio.NFe;

import java.util.Optional;

public final class Regra538TotalIpi extends RegraSomaDosItens {

    @Override
    public String codigoRejeicao() {
        return "538";
    }

    @Override
    protected String campoTotal() {
        return "vIPI";
    }

    @Override
    protected Optional<String> valorDoItem(ItemNFe item) {
        return item.texto("imposto/IPI/IPITrib/vIPI");
    }

    @Override
    protected boolean aplicavel(NFe nfe) {
        return modelo55(nfe);
    }
}
