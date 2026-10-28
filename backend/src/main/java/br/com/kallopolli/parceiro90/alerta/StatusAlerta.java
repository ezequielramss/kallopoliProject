package br.com.kallopolli.parceiro90.alerta;

import java.time.OffsetDateTime;

public final class StatusAlerta {
    private StatusAlerta() { }
    public static String calcular(String persistido, OffsetDateTime criadoEm, String retorno, OffsetDateTime agora) {
        if ("ATENDIDO".equals(persistido) || retorno != null) return "ATENDIDO";
        return !criadoEm.plusHours(24).isAfter(agora) ? "SEM_RETORNO_24H" : "ABERTO";
    }
}
