package br.com.kallopolli.parceiro90.alerta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class StatusAlertaTest {
    private final OffsetDateTime criado = OffsetDateTime.parse("2026-10-01T10:00:00-03:00");
    @Test void calculaVinteEQuatroHorasSemPersistirEstadoDerivado() {
        assertEquals("ABERTO", StatusAlerta.calcular("ABERTO", criado, null, criado.plusHours(23)));
        assertEquals("SEM_RETORNO_24H", StatusAlerta.calcular("ABERTO", criado, null, criado.plusHours(24)));
        assertEquals("SEM_RETORNO_24H", StatusAlerta.calcular("ABERTO", criado, null, criado.plusHours(25)));
        assertEquals("ATENDIDO", StatusAlerta.calcular("ABERTO", criado, "CONVERSEI", criado.plusHours(25)));
    }
}
