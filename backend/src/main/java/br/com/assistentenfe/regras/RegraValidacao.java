package br.com.assistentenfe.regras;

import br.com.assistentenfe.dominio.NFe;
import br.com.assistentenfe.dominio.Ocorrencia;

import java.util.List;

/**
 * Uma regra de negócio, responsável por um único código de rejeição.
 *
 * <p>As regras rodam mesmo quando o XML falhou no schema (225), então precisam
 * ser seguras contra campos ausentes: se faltar um campo de que a regra precisa,
 * ela é ignorada — devolve lista vazia, sem lançar exceção.
 */
public interface RegraValidacao {

    /** Código de rejeição verificado pela regra, ex.: "564". */
    String codigoRejeicao();

    /**
     * Devolve as ocorrências encontradas; lista vazia significa que a NF-e passou
     * ou que faltam campos para avaliar a regra.
     */
    List<Ocorrencia> validar(NFe nfe);
}
