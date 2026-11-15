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

/**
 * Molde das rejeições "Total do X difere do somatório dos itens" (MOC 7.0,
 * Anexo I, grupo W). Cada código define o campo do total, o campo do item e,
 * se preciso, quais itens entram na soma. A comparação é exata, sem tolerância.
 *
 * <p>Campos opcionais ausentes no item valem zero, como manda o Anexo I. Se o
 * total faltar ou algum valor não for numérico, a regra é ignorada (D007).
 */
public abstract class RegraSomaDosItens implements RegraValidacao {

    static final String ICMS_TOT = "/NFe/infNFe/total/ICMSTot/";

    /** Nome do campo em {@code total/ICMSTot}, ex.: "vProd". */
    protected abstract String campoTotal();

    /** Valor do item que entra na soma; vazio conta como zero. */
    protected abstract Optional<String> valorDoItem(ItemNFe item);

    /** Se o item entra na soma. Por padrão, todos entram. */
    protected boolean considera(ItemNFe item) {
        return true;
    }

    /** Se a regra se aplica à NF-e (ex.: só ao modelo 55). Por padrão, sempre. */
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

    /** Verdadeiro se a NF-e é do modelo 55 (ou se o modelo não foi informado). */
    static boolean modelo55(NFe nfe) {
        return nfe.texto("/NFe/infNFe/ide/mod").map("55"::equals).orElse(true);
    }
}
