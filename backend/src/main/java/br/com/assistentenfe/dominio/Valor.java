package br.com.assistentenfe.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/** Leitura de valores monetários e quantidades como {@link BigDecimal}; nunca {@code double}. */
public final class Valor {

    private Valor() {
    }

    /** Converte o texto do XML (ex.: "1234.56"); vazio se não for um número. */
    public static Optional<BigDecimal> ler(String texto) {
        try {
            return Optional.of(new BigDecimal(texto.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /** Diferença absoluta entre dois valores. */
    public static BigDecimal diferenca(BigDecimal a, BigDecimal b) {
        return a.subtract(b).abs();
    }

    /** Texto com duas casas, como os valores monetários aparecem no XML. */
    public static String formatar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
