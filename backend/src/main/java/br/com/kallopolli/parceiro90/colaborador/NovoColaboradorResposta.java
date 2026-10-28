package br.com.kallopolli.parceiro90.colaborador;

import java.time.Instant;

public record NovoColaboradorResposta(String id, String nome, String funcao, String loja, String telefoneMascarado,
        Instant consentimentoEm, String participacaoStatus) { }
