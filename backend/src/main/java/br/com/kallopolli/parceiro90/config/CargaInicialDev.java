package br.com.kallopolli.parceiro90.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

@Component
@Profile("dev")
public class CargaInicialDev implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final PasswordEncoder encoder;
    @Value("${APP_SEED_RH_EMAIL:}") private String emailRh;
    @Value("${APP_SEED_RH_PASSWORD:}") private String senhaRh;
    @Value("${APP_SEED_GERENTE_EMAIL:}") private String emailGerente;
    @Value("${APP_SEED_GERENTE_PASSWORD:}") private String senhaGerente;

    public CargaInicialDev(JdbcTemplate jdbc, ObjectMapper mapper, PasswordEncoder encoder) {
        this.jdbc = jdbc; this.mapper = mapper; this.encoder = encoder;
    }
    @Override public void run(ApplicationArguments args) throws Exception {
        var total = jdbc.queryForObject("SELECT count(*) FROM usuario", Long.class);
        if (total == 0 && emailRh != null && !emailRh.isBlank() && senhaRh != null && senhaRh.length() >= 12) {
            jdbc.update("INSERT INTO usuario(email,senha_hash,perfil) VALUES (?,?, 'RH')", emailRh, encoder.encode(senhaRh));
            if (emailGerente != null && !emailGerente.isBlank() && senhaGerente != null && senhaGerente.length() >= 12) {
                long loja = jdbc.queryForObject("INSERT INTO loja(nome) VALUES ('Centro') RETURNING id", Long.class);
                jdbc.update("INSERT INTO usuario(email,senha_hash,perfil,loja_id) VALUES (?,?, 'GERENTE',?)", emailGerente, encoder.encode(senhaGerente), loja);
            }
        }
        if (!Boolean.parseBoolean(System.getenv().getOrDefault("APP_LOAD_DEMO_DATA", "true"))) return;
        for (String filename : List.of("kpis.json", "serie-mensal.json", "colaboradores.json", "alertas.json", "relatos.json", "bemestar.json", "bemestar-gerente.json", "relatorio-exemplo.json")) {
            var resource = new ClassPathResource("static/mock/" + filename);
            if (!resource.exists()) continue;
            try (InputStream stream = resource.getInputStream()) {
                JsonNode root = mapper.readTree(stream);
                String recurso = filename.replace(".json", "");
                if (root.isArray()) {
                    for (int i = 0; i < root.size(); i++) {
                        JsonNode item = root.get(i);
                        String chave = item.has("id") ? item.get("id").asText() : item.has("protocolo") ? item.get("protocolo").asText() : Integer.toString(i);
                        jdbc.update("INSERT INTO dashboard_dado(recurso,chave,conteudo) VALUES (?,?,?::jsonb) ON CONFLICT DO NOTHING", recurso, chave, mapper.writeValueAsString(item));
                    }
                } else jdbc.update("INSERT INTO dashboard_dado(recurso,chave,conteudo) VALUES (?,?,?::jsonb) ON CONFLICT DO NOTHING", recurso, "principal", mapper.writeValueAsString(root));
            }
        }
    }
}
