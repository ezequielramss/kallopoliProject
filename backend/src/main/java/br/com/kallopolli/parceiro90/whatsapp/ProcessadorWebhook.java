package br.com.kallopolli.parceiro90.whatsapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ProcessadorWebhook {
    private final ObjectMapper mapper;
    private final EventoWhatsAppHandler handler;
    public ProcessadorWebhook(ObjectMapper mapper, EventoWhatsAppHandler handler) { this.mapper = mapper; this.handler = handler; }
    @Async("webhookExecutor")
    public void enfileirar(byte[] corpoBruto) {
        try {
            JsonNode raiz = mapper.readTree(corpoBruto);
            for (JsonNode entry : raiz.path("entry")) for (JsonNode change : entry.path("changes")) {
                JsonNode value = change.path("value");
                for (JsonNode message : value.path("messages")) handler.mensagem(message);
                for (JsonNode status : value.path("statuses")) handler.status(status);
            }
        } catch (Exception ignored) {
            // Corpo inválido fica fora do log para preservar conteúdo e telefones.
        }
    }
}
