package br.com.assistentenfe.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public final class Valor {

    private Valor() {
    }

    public static Optional<BigDecimal> ler(String texto) {
        try {
            return Optional.of(new BigDecimal(texto.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static BigDecimal diferenca(BigDecimal a, BigDecimal b) {
        return a.subtract(b).abs();
    }

    public static String formatar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
