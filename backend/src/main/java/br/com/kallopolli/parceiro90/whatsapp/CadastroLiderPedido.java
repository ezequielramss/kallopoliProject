package br.com.kallopolli.parceiro90.whatsapp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastroLiderPedido(@NotBlank @Size(max=160) String nome, @NotBlank String perfil,
        Long lojaId, @NotBlank @Pattern(regexp="\\+[1-9][0-9]{7,14}") String telefone) { }
