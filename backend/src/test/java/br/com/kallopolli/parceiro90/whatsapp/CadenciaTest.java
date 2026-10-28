package br.com.kallopolli.parceiro90.whatsapp;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class CadenciaTest {
    @Test void geraOsDiasPrevistosNaJornada(){
        CadenciaProperties p=new CadenciaProperties();
        p.setDias(List.of(faixa(1,10,1),faixa(11,30,3),faixa(31,60,7),faixa(61,90,10)));
        int previstos=0;for(int d=1;d<=90;d++)if(p.diaPrevisto(d))previstos++;
        assertEquals(25,previstos);assertTrue(p.diaPrevisto(1));assertFalse(p.diaPrevisto(12));assertTrue(p.diaPrevisto(14));assertTrue(p.diaPrevisto(81));
    }
    @Test void respeitaJanelaEHorarioDeSilencio(){
        assertTrue(PoliticaHorario.permite(LocalTime.of(17,30),LocalTime.of(17,0),LocalTime.of(19,0),LocalTime.of(21,0),LocalTime.of(8,0)));
        assertFalse(PoliticaHorario.permite(LocalTime.of(21,30),LocalTime.of(17,0),LocalTime.of(22,0),LocalTime.of(21,0),LocalTime.of(8,0)));
        assertFalse(PoliticaHorario.permite(LocalTime.of(7,30),LocalTime.of(7,0),LocalTime.of(22,0),LocalTime.of(21,0),LocalTime.of(8,0)));
    }
    private CadenciaProperties.Faixa faixa(int inicio,int fim,int intervalo){var f=new CadenciaProperties.Faixa();f.setFaixa(List.of(inicio,fim));f.setIntervalo(intervalo);return f;}
}
