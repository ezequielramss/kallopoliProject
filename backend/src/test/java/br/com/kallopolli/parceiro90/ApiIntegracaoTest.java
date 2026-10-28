package br.com.kallopolli.parceiro90;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import br.com.kallopolli.parceiro90.alerta.RoteamentoAlerta;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class ApiIntegracaoTest {
    @Container static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");
    @DynamicPropertySource static void banco(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;

    @BeforeEach void usuariosTeste() {
        jdbc.update("INSERT INTO loja(nome) VALUES ('Centro') ON CONFLICT(nome) DO NOTHING");
        Long store = jdbc.queryForObject("SELECT id FROM loja WHERE nome='Centro'", Long.class);
        jdbc.update("INSERT INTO usuario(email,senha_hash,perfil) VALUES ('rh@test.local','x','RH') ON CONFLICT(email) DO NOTHING");
        jdbc.update("INSERT INTO usuario(email,senha_hash,perfil,loja_id) VALUES ('gerente@test.local','x','GERENTE',?) ON CONFLICT(email) DO NOTHING", store);
    }

    @Test @WithMockUser(username="rh@test.local", roles="RH") void endpointsPrincipaisRespondemComDados() throws Exception {
        mvc.perform(get("/api/perfil")).andExpect(status().isOk()).andExpect(jsonPath("$.perfil").value("RH"));
        mvc.perform(get("/api/kpis")).andExpect(status().isOk()).andExpect(jsonPath("$.emJornada").exists());
        mvc.perform(get("/api/kpis/serie-mensal")).andExpect(status().isOk()).andExpect(jsonPath("$.itens").isArray());
        mvc.perform(get("/api/colaboradores")).andExpect(status().isOk()).andExpect(jsonPath("$[0].motivoAtencao").exists());
        mvc.perform(get("/api/colaboradores/c01/linha-do-tempo")).andExpect(status().isOk()).andExpect(jsonPath("$.eventos").isArray());
        mvc.perform(get("/api/alertas")).andExpect(status().isOk()).andExpect(jsonPath("$[0].categoria").exists());
        mvc.perform(get("/api/relatos")).andExpect(status().isOk()).andExpect(jsonPath("$[0].conteudo").doesNotExist());
        mvc.perform(get("/api/relatos/AN-1048")).andExpect(status().isOk()).andExpect(jsonPath("$.conteudo").exists());
        mvc.perform(get("/api/bem-estar")).andExpect(status().isOk()).andExpect(jsonPath("$.pessoas[0].historico[0].resumoNeutro").exists());
        mvc.perform(post("/api/relatorios/gerar").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
            .contentType("application/json").content("{\"escopo\":\"EQUIPE\",\"publico\":\"DIRECAO\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.resumoExecutivo").exists());
        String relatorioDirecao = mvc.perform(post("/api/relatorios/gerar").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
            .contentType("application/json").content("{\"escopo\":\"FUNCIONARIO\",\"publico\":\"DIRECAO\",\"colaboradorId\":\"c01\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertFalse(relatorioDirecao.contains("Marina Silva"));
        mvc.perform(post("/api/relatorios/gerar").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
            .contentType("application/json").content("{\"escopo\":\"EQUIPE\",\"publico\":\"RH\",\"campoExtra\":true}"))
            .andExpect(status().isBadRequest());
    }

    @Test @WithMockUser(username="rh@test.local", roles="RH") void patchExigeCsrfEAuditaAlteracao() throws Exception {
        mvc.perform(patch("/api/relatos/AN-1048").contentType("application/json").content("{\"status\":\"EM_ANALISE\"}"))
            .andExpect(status().isForbidden());
        mvc.perform(patch("/api/relatos/AN-1048").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
            .contentType("application/json").content("{\"status\":\"EM_ANALISE\",\"observacaoInterna\":\"Análise iniciada.\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("EM_ANALISE"));
        mvc.perform(patch("/api/relatos/AN-1048").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
            .contentType("application/json").content("{\"status\":\"EM_ANALISE\",\"extra\":true}"))
            .andExpect(status().isBadRequest());
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM auditoria_acesso WHERE acao='ALTERACAO_RELATO' AND protocolo='AN-1048'", Integer.class));
    }

    @Test @WithMockUser(username="rh@test.local", roles="RH") void abrirDetalheGravaAuditoria() throws Exception {
        mvc.perform(get("/api/relatos/AN-1048")).andExpect(status().isOk());
        assertTrue(jdbc.queryForObject("SELECT count(*) FROM auditoria_acesso WHERE acao='LEITURA_RELATO' AND protocolo='AN-1048'", Integer.class) >= 1);
    }

    @Test @WithMockUser(username="gerente@test.local", roles="GERENTE") void gerenteSoVeUnidadeERotasPermitidas() throws Exception {
        mvc.perform(get("/api/relatos")).andExpect(status().isForbidden());
        JsonNode employees = mapper.readTree(mvc.perform(get("/api/colaboradores")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        for (JsonNode employee : employees) assertEquals("Centro", employee.path("loja").asText());
        JsonNode alerts = mapper.readTree(mvc.perform(get("/api/alertas")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        for (JsonNode alerta : alerts) {
            assertEquals("GERENTE", alerta.path("destino").asText());
            String employeeId = alerta.path("colaboradorId").asText();
            if (!employeeId.isBlank()) assertEquals("Centro", findMockEmployee(employeeId).path("loja").asText());
        }
        JsonNode wellness = mapper.readTree(mvc.perform(get("/api/bem-estar")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertFalse(wellness.toString().contains("resumoNeutro"));
    }

    @Test void anonimoRecebe401() throws Exception { mvc.perform(get("/api/kpis")).andExpect(status().isUnauthorized()); }
    @Test void arquivosDosMocksNaoFicamPublicos() throws Exception { mvc.perform(get("/mock/relatos.json")).andExpect(status().isNotFound()); }

    @Test void webhookValidaAssinaturaDeduplicaEGuardaTextoCifrado() throws Exception {
        String textoPrivado="conteudo-privado-"+java.util.UUID.randomUUID();
        String wamid="wamid.test."+java.util.UUID.randomUUID();
        String bruto="{\"entry\":[{\"changes\":[{\"value\":{\"messages\":[{\"from\":\"5511999990000\",\"id\":\""+wamid+"\",\"type\":\"text\",\"text\":{\"body\":\""+textoPrivado+"\"}}]}}]}]}";
        byte[] corpo=bruto.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        mvc.perform(post("/webhook/whatsapp").contentType("application/json").content(corpo)).andExpect(status().isForbidden());
        assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM webhook_evento WHERE wamid=?",Integer.class,wamid));
        String assinatura=br.com.kallopolli.parceiro90.whatsapp.AssinaturaWebhook.calcular(corpo,"test-only-meta-app-secret");
        for(int i=0;i<2;i++)mvc.perform(post("/webhook/whatsapp").header("X-Hub-Signature-256",assinatura).contentType("application/json").content(corpo))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(textoPrivado))));
        long limite=System.currentTimeMillis()+5000;
        while(System.currentTimeMillis()<limite&&jdbc.queryForObject("SELECT count(*) FROM webhook_evento WHERE wamid=? AND estado='PROCESSADO'",Integer.class,wamid)==0)Thread.sleep(20);
        assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM mensagem_entrada WHERE wamid=?",Integer.class,wamid));
        String cifrado=jdbc.queryForObject("SELECT conteudo_cifrado FROM mensagem_entrada WHERE wamid=?",String.class,wamid);
        assertNotEquals(textoPrivado,cifrado);assertFalse(cifrado.contains(textoPrivado));
    }

    @Test @WithMockUser(username="rh@test.local", roles="RH") void cadastroExigeConsentimentoEDevolveTelefoneMascarado() throws Exception {
        String telefone="+55119"+String.format(java.util.Locale.ROOT,"%08d",new java.util.Random().nextInt(100_000_000));
        Long loja=jdbc.queryForObject("SELECT id FROM loja WHERE nome='Centro'",Long.class);
        String body="{\"nome\":\"Pessoa Teste\",\"funcao\":\"Atendente\",\"lojaId\":"+loja+",\"telefone\":\""+telefone+"\",\"consentimentoEm\":\"2026-10-01T10:00:00Z\",\"dataAdmissao\":\"2026-10-01\"}";
        String resposta=mvc.perform(post("/api/colaboradores").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType("application/json").content(body)).andExpect(status().isOk()).andExpect(jsonPath("$.participacaoStatus").value("PENDENTE_OPTIN"))
                .andExpect(jsonPath("$.telefoneMascarado").exists()).andReturn().getResponse().getContentAsString();
        assertFalse(resposta.contains(telefone));
    }

    @Test @WithMockUser(username="rh@test.local", roles="RH") void contratosPreservamChavesETiposDosMocks() throws Exception {
        compararMock("/api/kpis", "kpis.json", false);
        compararMock("/api/kpis/serie-mensal", "serie-mensal.json", false);
        compararMock("/api/colaboradores", "colaboradores.json", true);
        compararMock("/api/alertas", "alertas.json", true);
        compararMock("/api/relatos", "relatos-lista.json", true);
        compararMock("/api/relatos/AN-1048", "relato-detalhe.json", false);
        compararMock("/api/bem-estar", "bemestar.json", false);
        compararMock("/api/relatorios/gerar", "relatorio-exemplo.json", false, "POST");
    }

    private void compararMock(String endpoint, String mock, boolean array) throws Exception { compararMock(endpoint, mock, array, "GET"); }
    private void compararMock(String endpoint, String mock, boolean array, String method) throws Exception {
        JsonNode expected;
        try (InputStream in = new ClassPathResource("dev-mock/" + mock).getInputStream()) { expected = mapper.readTree(in); }
        var request = method.equals("POST") ? post(endpoint).with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()).contentType("application/json").content("{\"escopo\":\"EQUIPE\",\"publico\":\"RH\"}") : get(endpoint);
        JsonNode actual = mapper.readTree(mvc.perform(request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        if (array) { assertTrue(actual.isArray()); assertTrue(expected.isArray()); compareShape(expected.get(0), actual.get(0)); }
        else compareShape(expected, actual);
    }
    private void compareShape(JsonNode expected, JsonNode actual) {
        assertEquals(expected.getNodeType(), actual.getNodeType());
        if (expected.isObject()) {
            Set<String> keys = iterator(expected.fieldNames()); assertEquals(keys, iterator(actual.fieldNames()));
            expected.fieldNames().forEachRemaining(key -> compareShape(expected.get(key), actual.get(key)));
        } else if (expected.isArray() && !expected.isEmpty()) compareShape(expected.get(0), actual.get(0));
    }
    private Set<String> iterator(Iterator<String> it) { var keys = new java.util.HashSet<String>(); it.forEachRemaining(keys::add); return keys; }
    private JsonNode findMockEmployee(String id) throws Exception {
        try (InputStream in = new ClassPathResource("dev-mock/colaboradores.json").getInputStream()) {
            for (JsonNode employee : mapper.readTree(in)) if (id.equals(employee.path("id").asText())) return employee;
        }
        return mapper.createObjectNode();
    }
}
