package br.com.kallopolli.parceiro90.whatsapp;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="app.cadencia")
public class CadenciaProperties {
    private String fuso = "America/Sao_Paulo";
    private LocalTime inicioJanela = LocalTime.of(17, 0);
    private LocalTime fimJanela = LocalTime.of(19, 0);
    private LocalTime inicioSilencio = LocalTime.of(21, 0);
    private LocalTime fimSilencio = LocalTime.of(8, 0);
    private int expiraOptinHoras = 48;
    private long intervaloMs = 60000;
    private List<Integer> ponteDias = new ArrayList<>(List.of(7,15,30,45,60,90));
    private List<Faixa> dias = new ArrayList<>();
    public String getFuso() { return fuso; } public void setFuso(String v) { fuso = v; }
    public LocalTime getInicioJanela() { return inicioJanela; } public void setInicioJanela(LocalTime v) { inicioJanela = v; }
    public LocalTime getFimJanela() { return fimJanela; } public void setFimJanela(LocalTime v) { fimJanela = v; }
    public LocalTime getInicioSilencio() { return inicioSilencio; } public void setInicioSilencio(LocalTime v) { inicioSilencio = v; }
    public LocalTime getFimSilencio() { return fimSilencio; } public void setFimSilencio(LocalTime v) { fimSilencio = v; }
    public int getExpiraOptinHoras() { return expiraOptinHoras; } public void setExpiraOptinHoras(int v) { expiraOptinHoras = v; }
    public long getIntervaloMs() { return intervaloMs; } public void setIntervaloMs(long v) { intervaloMs = v; }
    public List<Integer> getPonteDias() { return ponteDias; } public void setPonteDias(List<Integer> v) { ponteDias = v; }
    public List<Faixa> getDias() { return dias; } public void setDias(List<Faixa> v) { dias = v; }
    public boolean diaPrevisto(int dia) {
        for (Faixa faixa : dias) if (faixa.previsto(dia)) return true;
        return false;
    }
    public static class Faixa {
        private List<Integer> faixa = new ArrayList<>();
        private int intervalo = 1;
        public List<Integer> getFaixa() { return faixa; } public void setFaixa(List<Integer> v) { faixa = v; }
        public int getIntervalo() { return intervalo; } public void setIntervalo(int v) { intervalo = v; }
        boolean previsto(int dia) { return faixa.size() == 2 && intervalo > 0 && dia >= faixa.get(0) && dia <= faixa.get(1) && (dia - faixa.get(0)) % intervalo == 0; }
    }
}
