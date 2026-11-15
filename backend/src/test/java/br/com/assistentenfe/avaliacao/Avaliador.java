package br.com.assistentenfe.avaliacao;

import br.com.assistentenfe.XmlDeTeste;
import br.com.assistentenfe.assinatura.AssinadorNFe;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.estrutura.ValidadorSchema;
import br.com.assistentenfe.explicacao.CatalogoRejeicoes;
import br.com.assistentenfe.regras.RegrasDisponiveis;
import br.com.assistentenfe.validacao.ValidadorNFe;
import org.w3c.dom.Document;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Roda as mutações sobre os XMLs de base e calcula as métricas da avaliação (D023).
 * O relógio é fixo e a tolerância do 703 é a mesma da aplicação, para o resultado
 * não depender do dia em que a avaliação roda.
 */
public final class Avaliador {

    /** Instante fixo da validação: 12/11/2026 09:00 em -03:00. */
    static final Instant AGORA = Instant.parse("2026-11-12T12:00:00Z");
    /** Uma hora depois de {@link #AGORA}, no fuso dos XMLs de base (mesmo ano e mês da chave). */
    static final String DH_EMI_FUTURO = "2026-11-12T10:00:00-03:00";
    static final Duration TOLERANCIA_DH_EMI = Duration.ofMinutes(5);

    private final ValidadorNFe validador = new ValidadorNFe(ValidadorSchema.nfe400(),
            RegrasDisponiveis.todas(Clock.fixed(AGORA, ZoneOffset.UTC), TOLERANCIA_DH_EMI));
    private final CatalogoRejeicoes catalogo = CatalogoRejeicoes.carregar();

    /** Resultado de uma mutação aplicada a uma base. */
    public record Caso(Mutacao mutacao, String base, String xpathEsperado, List<Ocorrencia> ocorrencias,
                       boolean detectada, boolean codigoCorreto, boolean campoCorreto, boolean explicacaoCompleta) {

        public List<String> codigos() {
            return ocorrencias.stream().map(Ocorrencia::codigo).distinct().toList();
        }

        /** Códigos além do esperado: só informação para a discussão, fora das taxas. */
        public List<String> extras() {
            return codigos().stream().filter(c -> !c.equals(mutacao.codigo())).toList();
        }

        /** XPath da ocorrência do código esperado (a primeira), para conferência. */
        public String xpathEncontrado() {
            return ocorrencias.stream().filter(o -> o.codigo().equals(mutacao.codigo()))
                    .map(Ocorrencia::xpath).filter(Objects::nonNull).findFirst().orElse("");
        }
    }

    /** XML de base sem mutação: o controle negativo exige zero ocorrências. */
    public record ControleNegativo(String base, List<Ocorrencia> ocorrencias) {
    }

    public List<ControleNegativo> controleNegativo() {
        List<ControleNegativo> controles = new ArrayList<>();
        for (String base : XmlDeTeste.nomesValidos()) {
            controles.add(new ControleNegativo(base, validar(XmlDeTeste.valido(base))));
        }
        return controles;
    }

    /** Todos os casos aplicáveis (mutação × base em que o campo existe). */
    public List<Caso> casos(List<Mutacao> mutacoes) {
        List<Caso> casos = new ArrayList<>();
        for (Mutacao mutacao : mutacoes) {
            for (String base : XmlDeTeste.nomesValidos()) {
                avaliar(mutacao, base).ifPresent(casos::add);
            }
        }
        return casos;
    }

    Optional<Caso> avaliar(Mutacao mutacao, String base) {
        String assinado = XmlDeTeste.valido(base);
        Optional<String> mutado = mutacao.aplicacao().aplicar(assinado);
        if (mutado.isEmpty()) {
            return Optional.empty();
        }
        String xml = mutacao.assinatura() == Mutacao.Assinatura.REASSINAR
                ? AssinadorNFe.assinar(mutado.get())
                : mutado.get();
        String xpathEsperado = resolverXpath(mutacao.xpath(), assinado);
        List<Ocorrencia> ocorrencias = validar(xml);

        List<Ocorrencia> doCodigo = ocorrencias.stream().filter(o -> o.codigo().equals(mutacao.codigo())).toList();
        Optional<Ocorrencia> noCampo = doCodigo.stream()
                .filter(o -> Objects.equals(o.xpath(), xpathEsperado))
                .filter(o -> mutacao.motivo() == null || mutacao.motivo().equals(o.motivo()))
                .findFirst();
        Optional<Ocorrencia> paraExplicar = noCampo.or(() -> doCodigo.stream().findFirst());

        return Optional.of(new Caso(mutacao, base, xpathEsperado, ocorrencias,
                !ocorrencias.isEmpty(),
                !doCodigo.isEmpty(),
                noCampo.isPresent(),
                paraExplicar.map(o -> catalogo.explicar(o).completa()).orElse(false)));
    }

    private List<Ocorrencia> validar(String xml) {
        return validador.validar(xml.getBytes(StandardCharsets.UTF_8)).ocorrencias();
    }

    /** Substitui {det} (det ou det[1]) e {icms} (grupo de ICMS do 1º item) conforme a base. */
    private static String resolverXpath(String xpath, String xmlBase) {
        if (xpath == null || !xpath.contains("{")) {
            return xpath;
        }
        Document documento = Dom.ler(xmlBase);
        int itens = Dom.elemento(documento, "/NFe/infNFe").map(i -> Dom.filhos(i, "det").size()).orElse(0);
        String grupo = Dom.grupoIcmsDoPrimeiroItem(documento).map(g -> g.getLocalName()).orElse("ICMS");
        return xpath.replace("{det}", itens > 1 ? "det[1]" : "det").replace("{icms}", grupo);
    }
}
