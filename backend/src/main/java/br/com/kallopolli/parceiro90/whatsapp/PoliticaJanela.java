package br.com.kallopolli.parceiro90.whatsapp;

import java.time.Duration;
import java.time.Instant;

public final class PoliticaJanela {
    public enum Decisao { MENSAGEM_LIVRE, TEMPLATE, BLOQUEAR }
    private PoliticaJanela() { }
    public static Decisao decidir(Instant ultimaEntrada, Instant agora, String templateAprovado) {
        if (ultimaEntrada != null && !agora.isAfter(ultimaEntrada.plus(Duration.ofHours(24)))) return Decisao.MENSAGEM_LIVRE;
        return templateAprovado == null || templateAprovado.isBlank() ? Decisao.BLOQUEAR : Decisao.TEMPLATE;
    }
}
