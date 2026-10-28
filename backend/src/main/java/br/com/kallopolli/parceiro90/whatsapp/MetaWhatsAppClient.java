package br.com.kallopolli.parceiro90.whatsapp;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@Profile({"prod", "homolog"})
public class MetaWhatsAppClient implements WhatsAppClient {
    private final RestClient http;
    private final String graphVersion;
    private final String phoneNumberId;
    public MetaWhatsAppClient(RestClient.Builder builder,
            @Value("${app.whatsapp.token:}") String token,
            @Value("${app.whatsapp.phone-number-id:}") String phoneNumberId,
            @Value("${app.whatsapp.graph-version:}") String graphVersion) {
        if (token.isBlank() || phoneNumberId.isBlank() || graphVersion.isBlank()) throw new IllegalStateException("Configure as variáveis de WhatsApp e a versão Graph API.");
        this.http = builder.baseUrl("https://graph.facebook.com/" + graphVersion).defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token).build();
        this.phoneNumberId = phoneNumberId; this.graphVersion = graphVersion;
    }
    @Override public String enviar(MensagemWhatsapp mensagem) {
        Map<String, Object> payload = mensagem.templateNome() == null ? texto(mensagem) : template(mensagem);
        try {
            JsonNode response = http.post().uri("/{id}/messages", phoneNumberId).contentType(MediaType.APPLICATION_JSON).body(payload).retrieve().body(JsonNode.class);
            String wamid = response == null ? null : response.path("messages").path(0).path("id").asText(null);
            if (wamid == null) throw new FalhaWhatsAppException(502);
            return wamid;
        } catch (RestClientResponseException ex) { throw new FalhaWhatsAppException(ex.getStatusCode().value()); }
        catch (FalhaWhatsAppException ex) { throw ex; }
        catch (Exception ex) { throw new FalhaWhatsAppException(503); }
    }
    private Map<String, Object> texto(MensagemWhatsapp mensagem) {
        return Map.of("messaging_product", "whatsapp", "recipient_type", "individual", "to", mensagem.telefone(),
            "type", "text", "text", Map.of("preview_url", false, "body", mensagem.texto()));
    }
    private Map<String, Object> template(MensagemWhatsapp mensagem) {
        Map<String, Object> tpl = new LinkedHashMap<>(); tpl.put("name", mensagem.templateNome()); tpl.put("language", Map.of("code", "pt_BR"));
        List<Map<String, Object>> componentes = new ArrayList<>();
        if (!mensagem.variaveis().isEmpty()) {
            var parametros = mensagem.variaveis().stream().map(v -> Map.<String, Object>of("type", "text", "text", v)).toList();
            componentes.add(Map.of("type", "body", "parameters", parametros));
        }
        for (int i = 0; i < mensagem.payloadsBotoes().size(); i++) {
            componentes.add(Map.of("type", "button", "sub_type", "quick_reply", "index", Integer.toString(i),
                "parameters", List.of(Map.of("type", "payload", "payload", mensagem.payloadsBotoes().get(i)))));
        }
        if (!componentes.isEmpty()) tpl.put("components", componentes);
        return Map.of("messaging_product", "whatsapp", "recipient_type", "individual", "to", mensagem.telefone(),
            "type", "template", "template", tpl);
    }
    public String graphVersion() { return graphVersion; }
}
