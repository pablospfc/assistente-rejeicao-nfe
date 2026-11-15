package br.com.assistentenfe.dominio;

import java.util.Optional;

/**
 * Chave de acesso composta a partir dos campos da NF-e (43 posições, sem o
 * dígito verificador). É a referência usada pelas rejeições 502 e 253.
 *
 * <pre>
 * cUF(2) AAMM(4) CNPJ(14) mod(2) serie(3) nNF(9) tpEmis(1) cNF(8) = 43
 * </pre>
 */
public record ChaveAcesso(String cUF, String aamm, String cnpj, String mod,
                          String serie, String nNF, String tpEmis, String cNF) {

    private static final String IDE = "/NFe/infNFe/ide/";

    /**
     * Compõe a chave a partir de {@code ide} e {@code emit/CNPJ}. Devolve vazio se
     * faltar algum campo ou se ele não couber na sua posição (o 225 já aponta
     * esses casos) e também se o emitente for pessoa física (D009).
     */
    public static Optional<ChaveAcesso> compor(NFe nfe) {
        Optional<String> dhEmi = nfe.texto(IDE + "dhEmi").filter(d -> d.matches("\\d{4}-\\d{2}.*"));
        Optional<String> cnpj = nfe.texto("/NFe/infNFe/emit/CNPJ").filter(c -> c.length() == 14);
        Optional<String> cUF = tamanho(nfe, "cUF", 2);
        Optional<String> mod = tamanho(nfe, "mod", 2);
        Optional<String> serie = ate(nfe, "serie", 3);
        Optional<String> nNF = ate(nfe, "nNF", 9);
        Optional<String> tpEmis = tamanho(nfe, "tpEmis", 1);
        Optional<String> cNF = tamanho(nfe, "cNF", 8);

        if (dhEmi.isEmpty() || cnpj.isEmpty() || cUF.isEmpty() || mod.isEmpty() || serie.isEmpty()
                || nNF.isEmpty() || tpEmis.isEmpty() || cNF.isEmpty()) {
            return Optional.empty();
        }
        String aamm = dhEmi.get().substring(2, 4) + dhEmi.get().substring(5, 7);
        return Optional.of(new ChaveAcesso(cUF.get(), aamm, cnpj.get(), mod.get(),
                zeros(serie.get(), 3), zeros(nNF.get(), 9), tpEmis.get(), cNF.get()));
    }

    /** As 43 posições, sem o dígito verificador. */
    public String semDigito() {
        return cUF + aamm + cnpj + mod + serie + nNF + tpEmis + cNF;
    }

    /** Dígito verificador calculado sobre {@link #semDigito()}. */
    public int digitoCalculado() {
        return DigitoVerificador.chaveAcesso(semDigito());
    }

    private static Optional<String> tamanho(NFe nfe, String campo, int tamanho) {
        return nfe.texto(IDE + campo).filter(v -> v.length() == tamanho);
    }

    private static Optional<String> ate(NFe nfe, String campo, int maximo) {
        return nfe.texto(IDE + campo).filter(v -> !v.isEmpty() && v.length() <= maximo);
    }

    private static String zeros(String valor, int tamanho) {
        return "0".repeat(tamanho - valor.length()) + valor;
    }
}
