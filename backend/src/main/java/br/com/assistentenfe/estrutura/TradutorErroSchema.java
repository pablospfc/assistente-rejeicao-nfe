package br.com.assistentenfe.estrutura;

import br.com.assistentenfe.dominio.Camada;
import br.com.assistentenfe.dominio.Ocorrencia;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TradutorErroSchema {

    public static final String CODIGO = "225";

    private static final Pattern ARGUMENTO = Pattern.compile("'([^']*)'");

    private static final Pattern NAMESPACE = Pattern.compile("\"[^\"]*\":|[{}]");

    public Ocorrencia traduzir(String mensagem, String xpath) {
        String chave = mensagem.substring(0, Math.max(mensagem.indexOf(':'), 0));
        List<String> args = argumentos(mensagem);

        return switch (chave) {
            case "cvc-pattern-valid" -> ocorrencia(xpath, arg(args, 0), arg(args, 1), "formato");
            case "cvc-enumeration-valid" -> ocorrencia(xpath, arg(args, 0), semColchetes(arg(args, 1)), "lista");
            case "cvc-maxLength-valid" -> ocorrencia(xpath, arg(args, 0), arg(args, 2), "tamanhoMaximo");
            case "cvc-minLength-valid" -> ocorrencia(xpath, arg(args, 0), arg(args, 2), "tamanhoMinimo");
            case "cvc-length-valid" -> ocorrencia(xpath, arg(args, 0), arg(args, 2), "tamanhoExato");
            case "cvc-datatype-valid.1.2.1" -> ocorrencia(xpath, arg(args, 0), arg(args, 1), "tipo");
            case "cvc-complex-type.2.4.a" -> ocorrencia(xpath, arg(args, 0), arg(args, 1), "elementoInesperado");
            case "cvc-complex-type.2.4.b" -> ocorrencia(xpath, null, arg(args, 1), "elementoFaltando");
            case "cvc-complex-type.2.4.d" -> ocorrencia(xpath, arg(args, 0), null, "elementoNaoPermitido");
            case "cvc-complex-type.2.3" -> ocorrencia(xpath, null, null, "textoNaoPermitido");
            case "cvc-complex-type.3.2.2" -> ocorrencia(xpath + "/@" + arg(args, 0), arg(args, 0), null, "atributoNaoPermitido");
            case "cvc-complex-type.4" -> ocorrencia(xpath + "/@" + arg(args, 0), null, arg(args, 0), "atributoFaltando");
            case "cvc-elt.1.a" -> ocorrencia("/" + arg(args, 0), arg(args, 0), "NFe", "raizDesconhecida");
            default -> ocorrencia(xpath, null, null, "generico");
        };
    }

    public boolean ehRepeticao(String mensagem) {
        return mensagem.startsWith("cvc-type.3.1.3")
                || mensagem.startsWith("cvc-attribute.3")
                || mensagem.startsWith("cvc-elt.5.2.2")
                || mensagem.startsWith("cvc-complex-type.2.2");
    }

    public String atributoCitado(String mensagem) {
        return mensagem.startsWith("cvc-attribute.3") ? arg(argumentos(mensagem), 1) : null;
    }

    private static Ocorrencia ocorrencia(String xpath, String encontrado, String esperado, String motivo) {
        return new Ocorrencia(CODIGO, Camada.ESTRUTURA, xpath, encontrado, esperado, motivo);
    }

    private static List<String> argumentos(String mensagem) {
        List<String> args = new ArrayList<>();
        Matcher m = ARGUMENTO.matcher(mensagem);
        while (m.find()) {
            args.add(NAMESPACE.matcher(m.group(1)).replaceAll(""));
        }
        return args;
    }

    private static String arg(List<String> args, int i) {
        return i < args.size() ? args.get(i) : null;
    }

    private static String semColchetes(String lista) {
        return lista == null ? null : lista.replaceAll("^\\[|]$", "");
    }
}
