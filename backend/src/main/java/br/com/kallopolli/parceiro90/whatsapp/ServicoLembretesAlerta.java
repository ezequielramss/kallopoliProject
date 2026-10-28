package br.com.kallopolli.parceiro90.whatsapp;

import java.sql.Timestamp;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class ServicoLembretesAlerta {
    private final JdbcTemplate jdbc; private final TransactionTemplate tx; private final Clock clock; private final ServicoEnvioMensagens envios; private final AssinadorBotoes botoes;
    private final int maxLembretes; private final boolean escalonar;
    public ServicoLembretesAlerta(JdbcTemplate jdbc,TransactionTemplate tx,Clock clock,ServicoEnvioMensagens envios,AssinadorBotoes botoes,
            @Value("${app.alertas.max-lembretes:1}") int maxLembretes,@Value("${app.alertas.escalonar-rh:false}") boolean escalonar){
        this.jdbc=jdbc;this.tx=tx;this.clock=clock;this.envios=envios;this.botoes=botoes;this.maxLembretes=maxLembretes;this.escalonar=escalonar;
    }
    @Scheduled(fixedDelayString="${app.cadencia.intervalo-ms:60000}")
    public void processar(){
        if(maxLembretes<=0){if(escalonar)escalonarRh();return;}
        var ids=tx.execute(s->jdbc.query("WITH candidatos AS (SELECT id FROM alerta WHERE status='ABERTO' AND criado_em<=? AND lembrete_enviado_em IS NULL AND (lembrete_processando_em IS NULL OR lembrete_processando_em<?) ORDER BY criado_em LIMIT 25 FOR UPDATE SKIP LOCKED) UPDATE alerta a SET lembrete_processando_em=? FROM candidatos c WHERE a.id=c.id RETURNING a.id",(rs,n)->rs.getString(1),Timestamp.from(clock.instant().minusSeconds(24*3600)),Timestamp.from(clock.instant().minusSeconds(600)),Timestamp.from(clock.instant())));
        if(ids!=null) for(String id:ids) enviar(id);
        if(escalonar) escalonarRh();
    }
    private void enviar(String alertaId){
        try{
            var alerta=jdbc.query("SELECT protocolo FROM alerta WHERE id=? AND status='ABERTO'",(rs,n)->rs.getString(1),alertaId);
            if(alerta.isEmpty())return;
            var lideres=jdbc.query("SELECT l.id,l.perfil FROM alerta_destinatario ad JOIN lider_contato l ON l.id=ad.lider_contato_id WHERE ad.alerta_id=? AND ad.acao='PENDENTE'",(rs,n)->new Destino(rs.getObject(1,UUID.class),rs.getString(2)),alertaId);
            boolean enviou=false;
            for(var lider:lideres){var r=envios.paraLider(lider.id(),"LEMBRETE_ALERTA","Lembrete de acompanhamento. Protocolo "+alerta.get(0)+".",TemplateWhatsapp.LEMBRETE_LIDER.nome(),List.of(alerta.get(0)),List.of(botoes.assinar(alertaId,"CONVERSEI"),botoes.assinar(alertaId,"NAO_CONSEGUI")));enviou|=r.enviado();}
            if(enviou){jdbc.update("UPDATE alerta SET lembrete_enviado_em=?,lembrete_processando_em=NULL WHERE id=?",Timestamp.from(clock.instant()),alertaId);jdbc.update("UPDATE dashboard_dado SET conteudo=jsonb_set(conteudo,'{lembreteEnviadoEm}',to_jsonb(?::text),true) WHERE recurso='alertas' AND chave=?",clock.instant().toString(),alertaId);jdbc.update("INSERT INTO alerta_evento(alerta_id,tipo,criado_em) VALUES (?,'LEMBRETE_24H',?) ON CONFLICT DO NOTHING",alertaId,Timestamp.from(clock.instant()));}
        }finally{jdbc.update("UPDATE alerta SET lembrete_processando_em=NULL WHERE id=?",alertaId);}
    }
    private void escalonarRh(){
        jdbc.update("INSERT INTO alerta_destinatario(alerta_id,lider_contato_id) SELECT a.id,l.id FROM alerta a JOIN lider_contato l ON l.perfil='RH' AND l.ativo=true WHERE a.status='ABERTO' AND a.criado_em<=? AND NOT EXISTS (SELECT 1 FROM alerta_evento e WHERE e.alerta_id=a.id AND e.tipo='ESCALONADO_RH') ON CONFLICT DO NOTHING",Timestamp.from(clock.instant().minusSeconds(48*3600)));
        var alertas=jdbc.query("SELECT a.id,a.protocolo FROM alerta a WHERE a.status='ABERTO' AND a.criado_em<=? AND NOT EXISTS (SELECT 1 FROM alerta_evento e WHERE e.alerta_id=a.id AND e.tipo='ESCALONADO_RH')",(rs,n)->new Alerta(rs.getString(1),rs.getString(2)),Timestamp.from(clock.instant().minusSeconds(48*3600)));
        for(var a:alertas){var rh=jdbc.query("SELECT ad.lider_contato_id FROM alerta_destinatario ad JOIN lider_contato l ON l.id=ad.lider_contato_id WHERE ad.alerta_id=? AND l.perfil='RH'",(rs,n)->rs.getObject(1,UUID.class),a.id());for(UUID id:rh)envios.paraLider(id,"ESCALONAMENTO_RH","Alerta sem retorno após 48 horas. Protocolo "+a.protocolo()+".",TemplateWhatsapp.ALERTA_RH.nome(),List.of(a.protocolo(),"acompanhamento", "Verificar a situação com confidencialidade."),List.of());jdbc.update("INSERT INTO alerta_evento(alerta_id,tipo,criado_em) VALUES (?,'ESCALONADO_RH',?) ON CONFLICT DO NOTHING",a.id(),Timestamp.from(clock.instant()));}
    }
    private record Destino(UUID id,String perfil){} private record Alerta(String id,String protocolo){}
}
