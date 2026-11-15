package br.com.assistentenfe.regras.identificacao.ie;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class EstrategiasIE {

    private static final Map<String, DigitoIE> POR_UF = List.of(
                    new IeMaranhao(), new IeSaoPaulo(), new IeParana())
            .stream()
            .collect(Collectors.toUnmodifiableMap(DigitoIE::uf, Function.identity()));

    private EstrategiasIE() {
    }

    public static Optional<DigitoIE> para(String uf) {
        return Optional.ofNullable(POR_UF.get(uf));
    }
}
