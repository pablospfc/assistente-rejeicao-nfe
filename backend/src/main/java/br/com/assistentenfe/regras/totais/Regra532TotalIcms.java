package br.com.assistentenfe.regras.totais;

import br.com.assistentenfe.dominio.ItemNFe;

import java.util.Optional;
import java.util.Set;

/** 532: vICMS (W04) = soma de vICMS (N17) dos itens, sem os CST 40, 41 e 50 (Anexo I, W04; NT 2010/007). */
public final class Regra532TotalIcms extends RegraSomaDosItens {

    private static final Set<String> CST_FORA_DO_TOTAL = Set.of("40", "41", "50");

    @Override
    public String codigoRejeicao() {
        return "532";
    }

    @Override
    protected String campoTotal() {
        return "vICMS";
    }

    @Override
    protected Optional<String> valorDoItem(ItemNFe item) {
        return item.noGrupoIcms("vICMS");
    }

    @Override
    protected boolean considera(ItemNFe item) {
        return item.noGrupoIcms("CST").map(cst -> !CST_FORA_DO_TOTAL.contains(cst)).orElse(true);
    }
}
