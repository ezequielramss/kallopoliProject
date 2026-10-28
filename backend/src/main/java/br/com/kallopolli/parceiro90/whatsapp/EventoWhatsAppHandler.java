package br.com.kallopolli.parceiro90.whatsapp;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class EventoWhatsAppHandler {
    private final RepositorioWebhookWhatsApp repositorio;
    private final ProcessadorEntradaWhatsApp processador;
    public EventoWhatsAppHandler(RepositorioWebhookWhatsApp repositorio, ProcessadorEntradaWhatsApp processador) {
        this.repositorio = repositorio; this.processador = processador;
    }
    public void mensagem(JsonNode mensagem) {
        EventoEntrada entrada = repositorio.registrar(mensagem);
        if (entrada == null) return;
        try { processador.processar(entrada); repositorio.finalizar(entrada.wamid(), true, null); }
        catch (RuntimeException ex) { repositorio.finalizar(entrada.wamid(), false, "PROCESSAMENTO_FALHOU"); }
    }
    public void status(JsonNode status) {
        String chave = repositorio.iniciarStatus(status);
        if (chave == null) return;
        try { repositorio.atualizarStatus(status); repositorio.finalizarStatus(chave); }
        catch (RuntimeException ex) { repositorio.finalizarStatus(chave); }
    }
}
