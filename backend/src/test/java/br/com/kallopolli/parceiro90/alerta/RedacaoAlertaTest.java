package br.com.kallopolli.parceiro90.alerta;

import static org.junit.jupiter.api.Assertions.assertFalse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class RedacaoAlertaTest {
    @Test void naoRetornaIdentificadorEmCategoriasConfidenciais() throws Exception {
        var mapper = new ObjectMapper();
        for (String categoria : new String[]{"ASSEDIO", "CONFLITO_CHEFIA"}) {
            var item = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree("{\"categoria\":\"" + categoria + "\",\"colaboradorId\":\"c01\",\"nome\":\"Pessoa Exemplo\",\"iniciais\":\"PE\",\"eventos\":[{\"colaboradorId\":\"c01\",\"nome\":\"Pessoa Exemplo\"}]}");
            var response = RedacaoAlerta.aplicar(item);
            assertFalse(response.has("colaboradorId")); assertFalse(response.has("nome")); assertFalse(response.has("iniciais"));
            assertFalse(response.path("eventos").get(0).has("colaboradorId")); assertFalse(response.path("eventos").get(0).has("nome"));
        }
    }
}
