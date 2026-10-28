package br.com.kallopolli.parceiro90.alerta;

import java.util.Set;

public final class RoteamentoAlerta {
    private static final Set<String> RH = Set.of("CONFLITO_CHEFIA", "ASSEDIO", "SITUACAO_GRAVE");
    private RoteamentoAlerta() { }
    public static String destino(String categoria) {
        if (categoria == null) throw new IllegalArgumentException("Categoria obrigatória.");
        if (!RH.contains(categoria) && !Set.of("CANSACO", "SOBRECARGA", "DESMOTIVACAO", "VONTADE_DE_SAIR", "DUVIDA", "CONFLITO_COLEGA").contains(categoria))
            throw new IllegalArgumentException("Categoria desconhecida.");
        return RH.contains(categoria) ? "RH" : "GERENTE";
    }
}
