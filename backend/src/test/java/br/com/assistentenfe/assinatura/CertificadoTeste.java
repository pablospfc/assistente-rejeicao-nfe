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

public record CertificadoTeste(PrivateKey chavePrivada, X509Certificate certificado) {

    private static final String ALIAS = "nfe-teste";

    public static CertificadoTeste padrao() {
        return Padrao.INSTANCIA;
    }

    public static CertificadoTeste gerar(int bits) {
        try {
            Path pasta = Files.createTempDirectory("certificado-teste");
            pasta.toFile().deleteOnExit();
            Path arquivo = pasta.resolve("certificado.p12");
            String senha = UUID.randomUUID().toString();

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

    static Path keytool() {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin", windows ? "keytool.exe" : "keytool");
    }

    private static final class Padrao {
        static final CertificadoTeste INSTANCIA = gerar(2048);
    }
}
