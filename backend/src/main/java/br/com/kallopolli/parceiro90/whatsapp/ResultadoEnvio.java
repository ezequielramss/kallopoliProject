package br.com.kallopolli.parceiro90.whatsapp;

import java.util.UUID;

public record ResultadoEnvio(UUID id, boolean enviado, String motivo) { }
