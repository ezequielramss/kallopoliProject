package br.com.kallopolli.parceiro90.relato;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizacaoRelato(@NotBlank String status, @Size(max=2000) String observacaoInterna) { }
