package br.com.assistentenfe.assinatura;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

/**
 * Certificado autoassinado gerado na própria execução dos testes, com o keytool do
 * JDK em uso ({@code java.home}/bin), sem depender do PATH nem de arquivo da máquina
 * do autor (D022). A pasta temporária é apagada ao fim da JVM.
 */
public record CertificadoTeste(PrivateKey chavePrivada, X509Certificate certificado) {

    private static final String ALIAS = "nfe-teste";

    /** Certificado padrão dos testes: gerado uma vez por execução. */
    public static CertificadoTeste padrao() {
        return Padrao.INSTANCIA;
    }

    /** Gera um novo certificado (outra chave), ex.: para simular troca de certificado. */
    public static CertificadoTeste gerar(int bits) {
        try {
            Path pasta = Files.createTempDirectory("certificado-teste");
            pasta.toFile().deleteOnExit();
            Path arquivo = pasta.resolve("certificado.p12");
            String senha = UUID.randomUUID().toString();

            // Idioma fixo em inglês: no JDK 25, o aviso de chave fraca do keytool em pt-BR tem um
            // erro de formatação (MissingFormatArgumentException) e o comando termina com falha (D028).
            Process keytool = new ProcessBuilder(List.of(keytool().toString(), "-J-Duser.language=en",
                    "-genkeypair", "-alias", ALIAS, "-keyalg", "RSA", "-keysize", String.valueOf(bits),
                    "-sigalg", "SHA256withRSA", "-validity", "3650",
                    "-dname", "CN=EMPRESA FICTICIA DE TESTES LTDA, OU=Certificado de teste autoassinado, C=BR",
                    "-storetype", "PKCS12", "-keystore", arquivo.toString(),
                    "-storepass", senha, "-keypass", senha))
                    .redirectErrorStream(true)
                    .start();
            String saida = new String(keytool.getInputStream().readAllBytes());
            if (keytool.waitFor() != 0) {
                throw new IllegalStateException("keytool falhou: " + saida);
            }
            arquivo.toFile().deleteOnExit();

            KeyStore loja = KeyStore.getInstance("PKCS12");
            try (InputStream in = Files.newInputStream(arquivo)) {
                loja.load(in, senha.toCharArray());
            }
            return new CertificadoTeste((PrivateKey) loja.getKey(ALIAS, senha.toCharArray()),
                    (X509Certificate) loja.getCertificate(ALIAS));
        } catch (IOException | InterruptedException | GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível gerar o certificado de teste", e);
        }
    }

    /** keytool do JDK que está rodando os testes. */
    static Path keytool() {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin", windows ? "keytool.exe" : "keytool");
    }

    private static final class Padrao {
        static final CertificadoTeste INSTANCIA = gerar(2048);
    }
}
