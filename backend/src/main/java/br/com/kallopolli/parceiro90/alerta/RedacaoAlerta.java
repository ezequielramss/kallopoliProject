package br.com.kallopolli.parceiro90.alerta;

import com.fasterxml.jackson.databind.node.ObjectNode;

public final class RedacaoAlerta {
    private RedacaoAlerta() { }
    public static ObjectNode aplicar(ObjectNode alerta) {
        String categoria = alerta.path("categoria").asText();
        if ("ASSEDIO".equals(categoria) || "CONFLITO_CHEFIA".equals(categoria)) {
            removerIdentificadores(alerta);
        }
        return alerta;
    }
    private static void removerIdentificadores(ObjectNode objeto) {
        objeto.remove("colaboradorId"); objeto.remove("nome"); objeto.remove("iniciais");
        var filhos = objeto.elements();
        while (filhos.hasNext()) {
            var filho = filhos.next();
            if (filho.isObject()) removerIdentificadores((ObjectNode) filho);
            else if (filho.isArray()) filho.forEach(item -> { if (item.isObject()) removerIdentificadores((ObjectNode) item); });
        }
    }
}
