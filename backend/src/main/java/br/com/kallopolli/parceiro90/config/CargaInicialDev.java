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
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

@Component
@Profile({"dev", "test"})
public class CargaInicialDev implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    public CargaInicialDev(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc; this.mapper = mapper;
    }
    @Override public void run(ApplicationArguments args) throws Exception {
        if (!Boolean.parseBoolean(System.getenv().getOrDefault("APP_LOAD_DEMO_DATA", "true"))) return;
        for (String filename : List.of("kpis.json", "serie-mensal.json", "colaboradores.json", "alertas.json", "relatos.json", "bemestar.json", "bemestar-gerente.json", "relatorio-exemplo.json")) {
            var resource = new ClassPathResource("dev-mock/" + filename);
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
        carregarModeloRelacional();
    }

    private void carregarModeloRelacional() throws Exception {
        for (JsonNode pessoa : list("colaboradores")) {
            String loja = pessoa.path("loja").asText(); long lojaId = ensureLoja(loja);
            jdbc.update("INSERT INTO colaborador(id,loja_id,nome,iniciais,funcao) VALUES (?,?,?,?,?) ON CONFLICT(id) DO NOTHING",
                pessoa.path("id").asText(), lojaId, pessoa.path("nome").asText(), pessoa.path("iniciais").asText(), pessoa.path("funcao").asText());
        }
        for (JsonNode alerta : list("alertas")) {
            String colaborador = alerta.hasNonNull("colaboradorId") ? alerta.path("colaboradorId").asText() : null;
            long lojaId = colaborador == null ? ensureLoja("Centro") : jdbc.queryForObject("SELECT loja_id FROM colaborador WHERE id=?", Long.class, colaborador);
            jdbc.update("INSERT INTO alerta(id,colaborador_id,loja_id,protocolo,categoria,severidade,destino,status,retorno,destinatario_nome,criado_em,respondido_em,lembrete_enviado_em,sugestao,colaborador_avisado) VALUES (?,?,?,?,?,?,?, ?,?,?,?,?,?,?,?) ON CONFLICT(id) DO NOTHING",
                alerta.path("id").asText(), colaborador, lojaId, alerta.path("protocolo").asText(), alerta.path("categoria").asText(), alerta.path("severidade").asText(), alerta.path("destino").asText(),
                List.of("ABERTO", "ATENDIDO").contains(alerta.path("status").asText()) ? alerta.path("status").asText() : "ABERTO", nullable(alerta, "retorno"), alerta.path("destinatarioNome").asText(),
                java.time.OffsetDateTime.parse(alerta.path("criadoEm").asText()), date(alerta, "respondidoEm"), date(alerta, "lembreteEnviadoEm"), alerta.path("sugestao").asText(), alerta.path("colaboradorAvisado").asBoolean());
            var created = java.time.OffsetDateTime.parse(alerta.path("criadoEm").asText());
            event(alerta.path("id").asText(), "SINAL_IDENTIFICADO", created);
            if (alerta.path("colaboradorAvisado").asBoolean()) event(alerta.path("id").asText(), "COLABORADOR_AVISADO", created);
            event(alerta.path("id").asText(), "ALERTA_ENVIADO", created);
            if (!alerta.path("lembreteEnviadoEm").isNull()) event(alerta.path("id").asText(), "LEMBRETE_24H", date(alerta, "lembreteEnviadoEm"));
            if (!alerta.path("respondidoEm").isNull()) event(alerta.path("id").asText(), "RETORNO", date(alerta, "respondidoEm"));
        }
        for (JsonNode relato : list("relatos")) {
            Long lojaId = ensureLoja(relato.path("loja").asText());
            long id = jdbc.queryForObject("INSERT INTO relato_confidencial(protocolo,loja_id,turno,titulo,categoria,severidade,status,conteudo,criado_em) VALUES (?,?,?,?,?,?,?,?,?) ON CONFLICT(protocolo) DO UPDATE SET protocolo=EXCLUDED.protocolo RETURNING id",
                Long.class, relato.path("protocolo").asText(), lojaId, relato.path("turno").asText(), relato.path("titulo").asText(), relato.path("categoria").asText(), relato.path("severidade").asText(), relato.path("status").asText(), relato.path("conteudo").asText(), java.time.OffsetDateTime.parse(relato.path("criadoEm").asText()));
            for (JsonNode evento : relato.path("historico")) jdbc.update("INSERT INTO relato_historico(relato_id,titulo,descricao,criado_em) SELECT ?,?,?,? WHERE NOT EXISTS (SELECT 1 FROM relato_historico WHERE relato_id=? AND titulo=? AND criado_em=?)",
                id, evento.path("titulo").asText(), evento.path("descricao").asText(), java.time.OffsetDateTime.parse(evento.path("data").asText()), id, evento.path("titulo").asText(), java.time.OffsetDateTime.parse(evento.path("data").asText()));
        }
        JsonNode serie = object("serie-mensal");
        for (JsonNode item : serie.path("itens")) jdbc.update("INSERT INTO serie_mensal(mes,estavel,atencao,prioridade,presenca) VALUES (?,?,?,?,?) ON CONFLICT(mes) DO NOTHING", item.path("mes").asText(), item.path("estavel").asInt(), item.path("atencao").asInt(), item.path("prioridade").asInt(), item.path("presenca").asInt());
        JsonNode wellbeing = object("bemestar");
        for (JsonNode pessoa : wellbeing.path("pessoas")) for (JsonNode evento : pessoa.path("historico")) jdbc.update("INSERT INTO bemestar_evento(colaborador_id,categoria,resumo_neutro,data,tag,tipo) VALUES (?,?,?,?,?,?) ON CONFLICT DO NOTHING",
            pessoa.path("colaboradorId").asText(), evento.path("categoria").asText(), evento.path("resumoNeutro").asText(), java.time.OffsetDateTime.parse(evento.path("data").asText()), evento.path("tag").asText(), evento.path("tipo").asText());
    }
    private List<JsonNode> list(String recurso) { return jdbc.query("SELECT conteudo::text FROM dashboard_dado WHERE recurso=? ORDER BY chave", (rs,n) -> read(rs.getString(1)), recurso); }
    private JsonNode read(String json) { try { return mapper.readTree(json); } catch (Exception ex) { throw new IllegalStateException("Fixture inválida."); } }
    private JsonNode object(String recurso) throws Exception { return mapper.readTree(jdbc.queryForObject("SELECT conteudo::text FROM dashboard_dado WHERE recurso=? AND chave='principal'", String.class, recurso)); }
    private long ensureLoja(String nome) { jdbc.update("INSERT INTO loja(nome) VALUES (?) ON CONFLICT(nome) DO NOTHING", nome); return jdbc.queryForObject("SELECT id FROM loja WHERE nome=?", Long.class, nome); }
    private void event(String alerta, String tipo, Object timestamp) { jdbc.update("INSERT INTO alerta_evento(alerta_id,tipo,criado_em) VALUES (?,?,?) ON CONFLICT DO NOTHING", alerta, tipo, timestamp); }
    private Object nullable(JsonNode node, String key) { return node.path(key).isNull() ? null : node.path(key).asText(); }
    private Object date(JsonNode node, String key) { return node.path(key).isNull() ? null : java.time.OffsetDateTime.parse(node.path(key).asText()); }
}
