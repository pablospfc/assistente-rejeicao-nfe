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

import static br.com.assistentenfe.regras.totais.RegraSomaDosItens.ICMS_TOT;

/**
 * 610: o vNF deve ser igual a (MOC 7.0, Anexo I, W16-10):
 * <pre>
 *   vProd − vDesc − vICMSDeson + vST + vFCPST + vFrete + vSeg + vOutro
 *   + vII + vIPI + vIPIDevol + vServ
 * </pre>
 * <ul>
 *   <li>Exceção 1: faturamento direto de veículos novos ({@code tpOp = 2}) não soma vST, vFCPST e vIPIDevol.</li>
 *   <li>Exceção 2: não se aplica a operações de importação (algum CFOP iniciado por 3).</li>
 *   <li>Exceção 3: não rejeita se o vICMSDeson não tiver sido subtraído.</li>
 * </ul>
 * Tolerância de R$ 0,50 para mais ou para menos, aplicada à regra inteira (nota (*3), D016).
 * Premissa da Reforma Tributária: a fórmula não inclui IBS, CBS nem IS (D015).
 */
public final class Regra610TotalNF implements RegraValidacao {

    private static final BigDecimal TOLERANCIA = new BigDecimal("0.50");
    private static final List<String> SOMA = List.of(
            "vProd", "vST", "vFCPST", "vFrete", "vSeg", "vOutro", "vII", "vIPI", "vIPIDevol");
    private static final List<String> SOMA_VEICULOS = List.of(
            "vProd", "vFrete", "vSeg", "vOutro", "vII", "vIPI");

    @Override
    public String codigoRejeicao() {
        return "610";
    }

    @Override
    public List<Ocorrencia> validar(NFe nfe) {
        List<ItemNFe> itens = nfe.itens();
        if (itens.stream().anyMatch(i -> i.texto("prod/CFOP").map(c -> c.startsWith("3")).orElse(false))) {
            return List.of();
        }
        boolean veiculoNovo = itens.stream().anyMatch(i -> i.texto("prod/veicProd/tpOp").map("2"::equals).orElse(false));

        Optional<BigDecimal> vNF = nfe.decimal(ICMS_TOT + "vNF");
        Optional<BigDecimal> vDesc = nfe.decimal(ICMS_TOT + "vDesc");
        Optional<BigDecimal> vICMSDeson = nfe.decimal(ICMS_TOT + "vICMSDeson");
        Optional<BigDecimal> vServ = vServ(nfe);
        if (vNF.isEmpty() || vDesc.isEmpty() || vICMSDeson.isEmpty() || vServ.isEmpty()) {
            return List.of();
        }

        BigDecimal calculado = vServ.get().subtract(vDesc.get()).subtract(vICMSDeson.get());
        for (String parcela : veiculoNovo ? SOMA_VEICULOS : SOMA) {
            Optional<BigDecimal> valor = nfe.decimal(ICMS_TOT + parcela);
            if (valor.isEmpty()) {
                return List.of();
            }
            calculado = calculado.add(valor.get());
        }

        BigDecimal semSubtrairDesoneracao = calculado.add(vICMSDeson.get());
        if (dentroDaTolerancia(vNF.get(), calculado) || dentroDaTolerancia(vNF.get(), semSubtrairDesoneracao)) {
            return List.of();
        }
        return List.of(new Ocorrencia(codigoRejeicao(), Camada.REGRAS_NEGOCIO,
                ICMS_TOT + "vNF", vNF.get().toPlainString(), Valor.formatar(calculado), null));
    }

    private static boolean dentroDaTolerancia(BigDecimal informado, BigDecimal calculado) {
        return Valor.diferenca(informado, calculado).compareTo(TOLERANCIA) <= 0;
    }

    /** vServ (W18) fica em ISSQNtot, que é opcional: ausente vale zero; presente e inválido ignora a regra. */
    private static Optional<BigDecimal> vServ(NFe nfe) {
        Optional<String> texto = nfe.texto("/NFe/infNFe/total/ISSQNtot/vServ");
        return texto.isEmpty() ? Optional.of(BigDecimal.ZERO) : texto.flatMap(Valor::ler);
    }
}
