package br.com.kallopolli.parceiro90.alerta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class RoteamentoAlertaTest {
    @Test void encaminhaCategoriasSensiviesAoRh() {
        assertEquals("RH", RoteamentoAlerta.destino("ASSEDIO"));
        assertEquals("RH", RoteamentoAlerta.destino("CONFLITO_CHEFIA"));
        assertEquals("RH", RoteamentoAlerta.destino("SITUACAO_GRAVE"));
    }
    @Test void encaminhaDemaisCategoriasAoGerente() {
        for (String categoria : new String[]{"CANSACO", "SOBRECARGA", "DESMOTIVACAO", "VONTADE_DE_SAIR", "DUVIDA", "CONFLITO_COLEGA"})
            assertEquals("GERENTE", RoteamentoAlerta.destino(categoria));
    }
}
