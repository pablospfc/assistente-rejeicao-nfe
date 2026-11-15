package br.com.assistentenfe.regras.identificacao;

import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.DigitoVerificador;
import br.com.assistentenfe.dominio.NFe;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.regras.RegraValidacao;

import java.util.List;

/** 207: CNPJ do emitente (numérico ou alfanumérico) com dígito inválido. Emitente com CPF é ignorado (D009). */
public final class Regra207CnpjEmitente implements RegraValidacao {

    private static final String CAMPO = "/NFe/infNFe/emit/CNPJ";

    @Override
    public String codigoRejeicao() {
        return "207";
    }

    @Override
    public List<Ocorrencia> validar(NFe nfe) {
        return nfe.texto(CAMPO)
                .filter(cnpj -> !DigitoVerificador.cnpjValido(cnpj))
                .map(cnpj -> List.of(new Ocorrencia(codigoRejeicao(), Camada.REGRAS_NEGOCIO,
                        CAMPO, cnpj, null, null)))
                .orElse(List.of());
    }
}
