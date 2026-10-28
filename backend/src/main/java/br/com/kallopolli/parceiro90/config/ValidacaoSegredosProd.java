package br.com.kallopolli.parceiro90.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"prod", "homolog"})
public class ValidacaoSegredosProd {
    @Bean ApplicationRunner validarSegredos(@Value("${APP_CRYPTO_KEY:}") String crypto, @Value("${APP_HASH_KEY:}") String hash,
            @Value("${DATABASE_PASSWORD:}") String databasePassword, @Value("${app.whatsapp.token:}") String token,
            @Value("${app.whatsapp.phone-number-id:}") String numberId, @Value("${app.whatsapp.app-secret:}") String appSecret,
            @Value("${app.whatsapp.verify-token:}") String verifyToken) {
        return args -> {
            if (crypto == null || crypto.length() < 32 || crypto.startsWith("SUBSTITUA") || hash == null || hash.length() < 32 || hash.startsWith("SUBSTITUA"))
                throw new IllegalStateException("APP_CRYPTO_KEY e APP_HASH_KEY devem estar configuradas com pelo menos 32 caracteres.");
            if (databasePassword == null || databasePassword.length() < 20 || databasePassword.startsWith("SUBSTITUA"))
                throw new IllegalStateException("DATABASE_PASSWORD deve ser configurada com pelo menos 20 caracteres em produção.");
            if (token.isBlank() || numberId.isBlank() || appSecret.isBlank() || verifyToken.isBlank())
                throw new IllegalStateException("Configure WHATSAPP_TOKEN, WHATSAPP_PHONE_NUMBER_ID, WHATSAPP_APP_SECRET e WHATSAPP_VERIFY_TOKEN.");
        };
    }
}
