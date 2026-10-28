package br.com.kallopolli.parceiro90.whatsapp;

import java.util.List;
import java.util.UUID;

public record MensagemWhatsapp(UUID id, String telefone, String destinatarioHash, String colaboradorId,
        UUID liderContatoId, String tipo, String templateNome, String texto, List<String> variaveis,
        List<String> payloadsBotoes) {
    public MensagemWhatsapp {
        variaveis = variaveis == null ? List.of() : List.copyOf(variaveis);
        payloadsBotoes = payloadsBotoes == null ? List.of() : List.copyOf(payloadsBotoes);
    }
}
