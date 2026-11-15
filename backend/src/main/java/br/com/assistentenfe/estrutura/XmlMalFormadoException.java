package br.com.assistentenfe.estrutura;

import br.com.assistentenfe.dominio.Ocorrencia;

public class XmlMalFormadoException extends Exception {

    private final transient Ocorrencia ocorrencia;

    public XmlMalFormadoException(Ocorrencia ocorrencia, Throwable causa) {
        super("XML mal formado: " + ocorrencia.valorEncontrado(), causa);
        this.ocorrencia = ocorrencia;
    }

    public Ocorrencia ocorrencia() {
        return ocorrencia;
    }
}
