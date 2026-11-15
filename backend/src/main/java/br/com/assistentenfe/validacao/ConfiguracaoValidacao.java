package br.com.assistentenfe.validacao;

import br.com.assistentenfe.estrutura.ValidadorSchema;
import br.com.assistentenfe.explicacao.CatalogoRejeicoes;
import br.com.assistentenfe.regras.RegrasDisponiveis;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;

/**
 * Liga as classes de validação ao Spring. Elas próprias não dependem do
 * framework, o que permite testá-las com um simples {@code new}.
 */
@Configuration
public class ConfiguracaoValidacao {

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    ValidadorNFe validadorNFe(Clock relogio,
                              @Value("${validacao.tolerancia-dh-emi:PT5M}") Duration toleranciaDhEmi) {
        return new ValidadorNFe(ValidadorSchema.nfe400(), RegrasDisponiveis.todas(relogio, toleranciaDhEmi));
    }

    @Bean
    CatalogoRejeicoes catalogoRejeicoes() {
        return CatalogoRejeicoes.carregar();
    }
}
