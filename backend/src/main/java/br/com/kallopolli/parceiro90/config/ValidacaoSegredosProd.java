package br.com.kallopolli.parceiro90.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class ValidacaoSegredosProd {
    @Bean ApplicationRunner validarSegredos(@Value("${APP_CRYPTO_KEY:}") String crypto, @Value("${APP_HASH_KEY:}") String hash) {
        return args -> {
            if (crypto == null || crypto.length() < 32 || hash == null || hash.length() < 32)
                throw new IllegalStateException("APP_CRYPTO_KEY e APP_HASH_KEY devem estar configuradas com pelo menos 32 caracteres.");
        };
    }
}
