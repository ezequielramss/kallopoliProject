package br.com.kallopolli.parceiro90.whatsapp;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PoliticaJanelaTest {
    private final Instant entrada=Instant.parse("2026-10-01T12:00:00Z");
    @Test void janelaEInclusivaAteVinteEQuatroHoras(){assertEquals(PoliticaJanela.Decisao.MENSAGEM_LIVRE,PoliticaJanela.decidir(entrada,entrada.plusSeconds(86400),null));assertEquals(PoliticaJanela.Decisao.MENSAGEM_LIVRE,PoliticaJanela.decidir(entrada,entrada.plusSeconds(3600),null));}
    @Test void foraDaJanelaExigeTemplate(){assertEquals(PoliticaJanela.Decisao.BLOQUEAR,PoliticaJanela.decidir(entrada,entrada.plusSeconds(86401),null));assertEquals(PoliticaJanela.Decisao.TEMPLATE,PoliticaJanela.decidir(entrada,entrada.plusSeconds(86401),"retomada_conversa"));}
}
