package br.com.assistentenfe.regras.totais;

import br.com.assistentenfe.dominio.ItemNFe;
import br.com.assistentenfe.dominio.NFe;

import java.util.Optional;

/** 538: vIPI (W12) = soma de IPITrib/vIPI (O14) dos itens; só modelo 55 (Anexo I, W12). O IPINT não tem vIPI. */
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
