package br.com.kallopolli.parceiro90.whatsapp;

import java.time.LocalTime;

public final class PoliticaHorario {
    private PoliticaHorario() { }
    public static boolean permite(LocalTime hora, LocalTime inicio, LocalTime fim, LocalTime silencioInicio, LocalTime silencioFim) {
        boolean janela=!hora.isBefore(inicio)&&hora.isBefore(fim);
        boolean silencio=silencioInicio.isAfter(silencioFim)?!hora.isBefore(silencioInicio)||hora.isBefore(silencioFim):!hora.isBefore(silencioInicio)&&hora.isBefore(silencioFim);
        return janela&&!silencio;
    }
}
