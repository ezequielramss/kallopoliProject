package br.com.kallopolli.parceiro90.relato;

import java.util.List;

public record EntradaClassificacao(String textoAtual, int diaJornada, List<String> ultimasMensagens) {
    public EntradaClassificacao { ultimasMensagens = ultimasMensagens == null ? List.of() : List.copyOf(ultimasMensagens); }
}
