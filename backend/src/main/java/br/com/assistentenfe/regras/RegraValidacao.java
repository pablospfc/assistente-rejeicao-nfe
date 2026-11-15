package br.com.assistentenfe.regras;

import br.com.assistentenfe.dominio.NFe;
import br.com.assistentenfe.dominio.Ocorrencia;

import java.util.List;

public interface RegraValidacao {

    String codigoRejeicao();

    List<Ocorrencia> validar(NFe nfe);
}
