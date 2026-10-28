package br.com.kallopolli.parceiro90.relatorio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PedidoRelatorio(@NotBlank @Size(max=40) String escopo, @NotBlank @Size(max=40) String publico, @Size(max=40) String colaboradorId) { }
