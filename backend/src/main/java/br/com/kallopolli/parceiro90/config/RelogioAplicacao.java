package br.com.kallopolli.parceiro90.config;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public final class RelogioAplicacao extends Clock {
    private final Clock real = Clock.systemUTC();
    private final Instant inicioReal;
    private final Instant inicioSimulado;
    private final double fator;
    private final ZoneId zona;
    public RelogioAplicacao(double fator, String inicio, ZoneId zona) {
        if (fator < 1 || !Double.isFinite(fator)) throw new IllegalArgumentException("APP_TEMPO_FATOR precisa ser >= 1.");
        this.inicioReal = real.instant(); this.inicioSimulado = inicio == null || inicio.isBlank() ? inicioReal : Instant.parse(inicio);
        this.fator = fator; this.zona = zona;
    }
    @Override public ZoneId getZone() { return zona; }
    @Override public Clock withZone(ZoneId zone) { return new RelogioAplicacao(fator, inicioSimulado.toString(), zone); }
    @Override public Instant instant() {
        long elapsed = Duration.between(inicioReal, real.instant()).toMillis();
        return inicioSimulado.plusMillis((long) (elapsed * fator));
    }
}
