package br.com.assistentenfe.regras.totais;

import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.ItemNFe;
import br.com.assistentenfe.dominio.NFe;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.dominio.Valor;
import br.com.assistentenfe.regras.RegraValidacao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public abstract class RegraSomaDosItens implements RegraValidacao {

    static final String ICMS_TOT = "/NFe/infNFe/total/ICMSTot/";

    protected abstract String campoTotal();

    protected abstract Optional<String> valorDoItem(ItemNFe item);

    protected boolean considera(ItemNFe item) {
        return true;
    }

    protected boolean aplicavel(NFe nfe) {
        return true;
    }

    @Override
    public List<Ocorrencia> validar(NFe nfe) {
        Optional<BigDecimal> total = nfe.decimal(ICMS_TOT + campoTotal());
        if (!aplicavel(nfe) || total.isEmpty()) {
            return List.of();
        }

        BigDecimal soma = BigDecimal.ZERO;
        for (ItemNFe item : nfe.itens()) {
            Optional<String> texto = considera(item) ? valorDoItem(item) : Optional.empty();
            if (texto.isPresent()) {
                Optional<BigDecimal> valor = Valor.ler(texto.get());
                if (valor.isEmpty()) {
                    return List.of();
                }
                soma = soma.add(valor.get());
            }
        }

        if (total.get().compareTo(soma) == 0) {
            return List.of();
        }
        return List.of(new Ocorrencia(codigoRejeicao(), Camada.REGRAS_NEGOCIO,
                ICMS_TOT + campoTotal(), total.get().toPlainString(), Valor.formatar(soma), null));
    }

    static boolean modelo55(NFe nfe) {
        return nfe.texto("/NFe/infNFe/ide/mod").map("55"::equals).orElse(true);
    }
}
