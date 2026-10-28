package br.com.kallopolli.parceiro90.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableScheduling
public class ExecucaoAssincronaConfig {
    @Bean Clock clock(@Value("${app.tempo.fator:1}") double fator, @Value("${app.tempo.inicio:}") String inicio,
            @Value("${app.cadencia.fuso:America/Sao_Paulo}") String fuso) {
        return new RelogioAplicacao(fator, inicio, ZoneId.of(fuso));
    }
    @Bean(name="webhookExecutor") ThreadPoolTaskExecutor webhookExecutor() {
        var executor = new ThreadPoolTaskExecutor(); executor.setCorePoolSize(2); executor.setMaxPoolSize(8);
        executor.setQueueCapacity(1000); executor.setThreadNamePrefix("webhook-"); executor.initialize(); return executor;
    }
}
