package br.com.assistentenfe.regras.totais;

import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.ItemNFe;
import br.com.assistentenfe.dominio.NFe;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.dominio.Valor;
import br.com.assistentenfe.regras.RegraValidacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class Regra528IcmsDoItem implements RegraValidacao {

    private static final Set<String> CSTS = Set.of("00", "10", "20", "70");
    private static final BigDecimal TOLERANCIA = new BigDecimal("0.01");
    private static final BigDecimal CEM = new BigDecimal("100");

    @Override
    public String codigoRejeicao() {
        return "528";
    }

    @Override
    public List<Ocorrencia> validar(NFe nfe) {
        if (!nfe.texto("/NFe/infNFe/ide/finNFe").map("1"::equals).orElse(false)) {
            return List.of();
        }
        List<Ocorrencia> ocorrencias = new ArrayList<>();
        for (ItemNFe item : nfe.itens()) {
            if (!item.noGrupoIcms("CST").map(CSTS::contains).orElse(false)) {
                continue;
            }
            Optional<BigDecimal> vBC = item.noGrupoIcms("vBC").flatMap(Valor::ler);
            Optional<BigDecimal> pICMS = item.noGrupoIcms("pICMS").flatMap(Valor::ler);
            Optional<BigDecimal> vICMS = item.noGrupoIcms("vICMS").flatMap(Valor::ler);
            if (vBC.isEmpty() || pICMS.isEmpty() || vICMS.isEmpty()) {
                continue;
            }

            BigDecimal esperado = vBC.get().multiply(pICMS.get()).divide(CEM).setScale(2, RoundingMode.HALF_UP);
            if (Valor.diferenca(vICMS.get(), esperado).compareTo(TOLERANCIA) > 0) {
                ocorrencias.add(new Ocorrencia(codigoRejeicao(), Camada.REGRAS_NEGOCIO,
                        item.xpathNoGrupoIcms("vICMS"), vICMS.get().toPlainString(), esperado.toPlainString(), null));
            }
        }
        return ocorrencias;
    }
}
