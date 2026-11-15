package br.com.assistentenfe.regras.identificacao;

import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.NFe;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.regras.RegraValidacao;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * 703: a data-hora de emissão não pode ser posterior ao horário de recebimento.
 * Como a validação é offline, o "recebimento" é o instante da validação, lido do
 * {@link Clock} (D012). O MOC 7.0 (Anexo I, grupo B) aceita até 5 minutos de
 * diferença pelo sincronismo de relógio; a tolerância é configurável e esse é o padrão.
 */
public final class Regra703DataEmissao implements RegraValidacao {

    private final Clock relogio;
    private final Duration tolerancia;

    public Regra703DataEmissao(Clock relogio, Duration tolerancia) {
        this.relogio = relogio;
        this.tolerancia = tolerancia;
    }

    @Override
    public String codigoRejeicao() {
        return "703";
    }

    @Override
    public List<Ocorrencia> validar(NFe nfe) {
        Optional<String> texto = nfe.texto("/NFe/infNFe/ide/dhEmi");
        Optional<OffsetDateTime> dhEmi = texto.flatMap(Regra703DataEmissao::ler);
        if (dhEmi.isEmpty()) {
            return List.of();
        }

        Instant recebimento = Instant.now(relogio);
        if (!dhEmi.get().toInstant().isAfter(recebimento.plus(tolerancia))) {
            return List.of();
        }
        // O instante da validação é mostrado no mesmo fuso do dhEmi, para facilitar a comparação.
        String agora = OffsetDateTime.ofInstant(recebimento, dhEmi.get().getOffset())
                .truncatedTo(ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return List.of(new Ocorrencia(codigoRejeicao(), Camada.REGRAS_NEGOCIO,
                "/NFe/infNFe/ide/dhEmi", texto.get(), agora, null));
    }

    private static Optional<OffsetDateTime> ler(String texto) {
        try {
            return Optional.of(OffsetDateTime.parse(texto));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
}
