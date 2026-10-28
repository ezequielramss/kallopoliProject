package br.com.kallopolli.parceiro90.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "prod", "homolog"})
public class SeedUsuarios implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    @Value("${APP_SEED_RH_EMAIL:}") private String emailRh;
    @Value("${APP_SEED_RH_PASSWORD:}") private String senhaRh;
    @Value("${APP_SEED_GERENTE_EMAIL:}") private String emailGerente;
    @Value("${APP_SEED_GERENTE_PASSWORD:}") private String senhaGerente;
    @Value("${APP_SEED_GERENTE_LOJA:Centro}") private String lojaGerente;
    public SeedUsuarios(JdbcTemplate jdbc, PasswordEncoder encoder) { this.jdbc = jdbc; this.encoder = encoder; }
    @Override public void run(ApplicationArguments args) {
        if (jdbc.queryForObject("SELECT count(*) FROM usuario", Long.class) > 0) return;
        validarEmail(emailRh, "APP_SEED_RH_EMAIL"); validarSenha(senhaRh, "APP_SEED_RH_PASSWORD");
        jdbc.update("INSERT INTO usuario(email,senha_hash,perfil) VALUES (?,?, 'RH')", emailRh, encoder.encode(senhaRh));
        if (emailGerente != null && !emailGerente.isBlank()) {
            validarEmail(emailGerente, "APP_SEED_GERENTE_EMAIL");
            validarSenha(senhaGerente, "APP_SEED_GERENTE_PASSWORD");
            jdbc.update("INSERT INTO loja(nome) VALUES (?) ON CONFLICT(nome) DO NOTHING", lojaGerente);
            Long lojaId = jdbc.queryForObject("SELECT id FROM loja WHERE nome=?", Long.class, lojaGerente);
            jdbc.update("INSERT INTO usuario(email,senha_hash,perfil,loja_id) VALUES (?,?, 'GERENTE',?)", emailGerente, encoder.encode(senhaGerente), lojaId);
        }
    }
    private void validarEmail(String valor, String nome) {
        if (valor == null || !valor.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || valor.startsWith("SUBSTITUA"))
            throw new IllegalStateException(nome + " deve conter um endereço de e-mail válido para criar a conta inicial.");
    }
    private void validarSenha(String senha, String nome) {
        if (senha == null || senha.startsWith("SUBSTITUA") || senha.length() < 12 || !senha.matches(".*[a-z].*") || !senha.matches(".*[A-Z].*") || !senha.matches(".*[0-9].*") || !senha.matches(".*[^A-Za-z0-9].*"))
            throw new IllegalStateException(nome + " precisa ter pelo menos 12 caracteres com maiúscula, minúscula, número e símbolo.");
    }
}
