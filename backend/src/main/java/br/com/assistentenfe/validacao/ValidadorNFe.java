package br.com.assistentenfe.validacao;

import br.com.assistentenfe.dominio.NFe;
import br.com.assistentenfe.dominio.Ocorrencia;
import br.com.assistentenfe.dominio.ResultadoValidacao;
import br.com.assistentenfe.estrutura.ValidadorSchema;
import br.com.assistentenfe.estrutura.VerificadorAssinatura;
import br.com.assistentenfe.estrutura.VerificadorNamespace;
import br.com.assistentenfe.estrutura.VerificadorXmlBemFormado;
import br.com.assistentenfe.estrutura.XmlMalFormadoException;
import br.com.assistentenfe.regras.RegraValidacao;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Orquestra a validação: estrutura primeiro, depois as regras de negócio.
 * Se o XML não for bem formado (243), nada mais roda. O XML só existe em memória.
 */
public final class ValidadorNFe {

    private final VerificadorXmlBemFormado leitor = new VerificadorXmlBemFormado();
    private final VerificadorNamespace namespace = new VerificadorNamespace();
    private final VerificadorAssinatura assinatura = new VerificadorAssinatura();
    private final ValidadorSchema schema;
    private final List<RegraValidacao> regras;

    public ValidadorNFe(ValidadorSchema schema, List<RegraValidacao> regras) {
        this.schema = schema;
        this.regras = List.copyOf(regras);
    }

    public ResultadoValidacao validar(byte[] xml) {
        Document documento;
        try {
            documento = leitor.ler(xml);
        } catch (XmlMalFormadoException e) {
            return new ResultadoValidacao(List.of(e.ocorrencia()));
        }

        List<Ocorrencia> ocorrencias = new ArrayList<>();
        ocorrencias.addAll(namespace.verificar(documento));
        ocorrencias.addAll(schema.validar(documento));
        ocorrencias.addAll(assinatura.verificar(documento));

        // As regras rodam mesmo com erro de schema, para mostrar todos os problemas de uma vez.
        NFe nfe = new NFe(documento);
        for (RegraValidacao regra : regras) {
            ocorrencias.addAll(regra.validar(nfe));
        }
        return new ResultadoValidacao(ocorrencias);
    }
}
