package br.com.kallopolli.parceiro90.whatsapp;

import java.util.UUID;

public record EventoEntrada(String wamid, String remetenteHash, String tipo, String conteudo, String textoBotao,
        String colaboradorId, UUID liderContatoId, String estadoParticipacao) { }
