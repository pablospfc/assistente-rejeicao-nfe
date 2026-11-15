import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * Converte a planilha da Tabela CFOP do Portal da NF-e (Informe Técnico 2023.002)
 * para TSV, sem alterar o conteúdo: todas as linhas e colunas são mantidas, na
 * mesma ordem. As datas de vigência, gravadas pelo Excel como número serial, são
 * escritas no formato ISO (aaaa-mm-dd). Usa só o JDK, lendo o .xlsx como zip de XMLs.
 *
 * <p>Uso: {@code java ferramentas/ConverterTabelaCfop.java <planilha.xlsx> <saida.tsv>}
 */
public class ConverterTabelaCfop {

    private static final LocalDate EPOCA_EXCEL = LocalDate.of(1899, 12, 30);

    public static void main(String[] args) throws Exception {
        try (ZipFile xlsx = new ZipFile(args[0]);
             PrintWriter saida = new PrintWriter(Files.newBufferedWriter(Path.of(args[1]), StandardCharsets.UTF_8))) {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setNamespaceAware(true);
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            List<String> textos = new ArrayList<>();
            Document compartilhados = fabrica.newDocumentBuilder().parse(xlsx.getInputStream(xlsx.getEntry("xl/sharedStrings.xml")));
            NodeList si = compartilhados.getElementsByTagNameNS("*", "si");
            for (int i = 0; i < si.getLength(); i++) {
                StringBuilder texto = new StringBuilder();
                NodeList t = ((Element) si.item(i)).getElementsByTagNameNS("*", "t");
                for (int j = 0; j < t.getLength(); j++) {
                    texto.append(t.item(j).getTextContent());
                }
                textos.add(texto.toString());
            }

            Document planilha = fabrica.newDocumentBuilder().parse(xlsx.getInputStream(xlsx.getEntry("xl/worksheets/sheet1.xml")));
            NodeList linhas = planilha.getElementsByTagNameNS("*", "row");
            int colunas = 0;
            for (int r = 0; r < linhas.getLength(); r++) {
                String[] valores = new String[colunas == 0 ? 64 : colunas];
                NodeList celulas = ((Element) linhas.item(r)).getElementsByTagNameNS("*", "c");
                for (int c = 0; c < celulas.getLength(); c++) {
                    Element celula = (Element) celulas.item(c);
                    int coluna = indiceDaColuna(celula.getAttribute("r"));
                    NodeList v = celula.getElementsByTagNameNS("*", "v");
                    String valor = v.getLength() > 0 ? v.item(0).getTextContent() : "";
                    if ("s".equals(celula.getAttribute("t")) && !valor.isEmpty()) {
                        valor = textos.get(Integer.parseInt(valor));
                    } else if (r > 0 && (coluna == 1 || coluna == 2) && !valor.isEmpty()) {
                        valor = EPOCA_EXCEL.plusDays(Long.parseLong(valor)).toString();
                    }
                    if (coluna >= valores.length) {
                        // Células formatadas além das colunas do cabeçalho: só aceitas se estiverem vazias.
                        if (!valor.isBlank()) {
                            throw new IllegalStateException("Valor fora das colunas do cabeçalho na célula " + celula.getAttribute("r"));
                        }
                        continue;
                    }
                    if (valor.contains("\t") || valor.contains("\n") || valor.contains("\r")) {
                        throw new IllegalStateException("Valor com tabulação ou quebra de linha na célula " + celula.getAttribute("r"));
                    }
                    valores[coluna] = valor;
                }
                if (r == 0) {
                    while (colunas < valores.length && valores[colunas] != null) {
                        colunas++;
                    }
                }
                List<String> campos = new ArrayList<>();
                for (int i = 0; i < colunas; i++) {
                    campos.add(valores[i] == null ? "" : valores[i]);
                }
                saida.println(String.join("\t", campos));
            }
        }
    }

    /** "G12" → 6 (coluna A = 0). */
    private static int indiceDaColuna(String referencia) {
        int indice = 0;
        for (char ch : referencia.toCharArray()) {
            if (!Character.isLetter(ch)) {
                break;
            }
            indice = indice * 26 + (ch - 'A' + 1);
        }
        return indice - 1;
    }
}
