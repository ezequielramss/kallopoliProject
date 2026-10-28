package br.com.kallopolli.parceiro90.relato;

public record ResultadoClassificacao(Categoria categoria, Severidade severidade, double confianca, String resumoNeutro, String sugestao) { }
