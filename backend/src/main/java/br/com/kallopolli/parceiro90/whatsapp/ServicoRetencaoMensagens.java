package br.com.kallopolli.parceiro90.whatsapp;

import java.sql.Timestamp;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ServicoRetencaoMensagens {
    private final JdbcTemplate jdbc; private final Clock clock; private final int dias;
    public ServicoRetencaoMensagens(JdbcTemplate jdbc,Clock clock,@Value("${app.retencao.mensagens-dias:120}") int dias){this.jdbc=jdbc;this.clock=clock;this.dias=Math.max(1,dias);}
    @Scheduled(cron="0 15 3 * * *", zone="America/Sao_Paulo")
    public void expurgar(){
        Timestamp limite=Timestamp.from(clock.instant().minusSeconds(dias*86400L));
        jdbc.update("UPDATE mensagem_entrada SET conteudo_cifrado=NULL WHERE recebida_em<? AND conteudo_cifrado IS NOT NULL",limite);
        jdbc.update("UPDATE mensagem_saida SET conteudo_cifrado=NULL WHERE criada_em<? AND conteudo_cifrado IS NOT NULL",limite);
        jdbc.update("UPDATE outbox_dev o SET conteudo_cifrado=NULL FROM mensagem_saida m WHERE o.mensagem_saida_id=m.id AND m.criada_em<? AND o.conteudo_cifrado IS NOT NULL",limite);
        jdbc.update("UPDATE colaborador SET participacao_status='ENCERRADO' WHERE participacao_status='ATIVO' AND data_admissao<=?",clock.instant().atZone(java.time.ZoneId.of("America/Sao_Paulo")).toLocalDate().minusDays(90));
        jdbc.update("UPDATE mensagem_agendada a SET status='CANCELADA' FROM colaborador c WHERE a.colaborador_id=c.id AND c.participacao_status='ENCERRADO' AND a.status IN ('PENDENTE','PROCESSANDO')");
    }
}
