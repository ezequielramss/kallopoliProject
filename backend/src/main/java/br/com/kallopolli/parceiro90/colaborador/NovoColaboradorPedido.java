package br.com.kallopolli.parceiro90.colaborador;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;

public record NovoColaboradorPedido(@NotBlank @Size(max=160) String nome, @NotBlank @Size(max=120) String funcao,
        @NotNull Long lojaId, @NotBlank @Pattern(regexp="\\+[1-9][0-9]{7,14}") String telefone,
        @NotNull @PastOrPresent Instant consentimentoEm, @NotNull @PastOrPresent LocalDate dataAdmissao) { }
