package br.com.kallopolli.parceiro90.whatsapp;

import br.com.kallopolli.parceiro90.alerta.RoteamentoAlerta;
import br.com.kallopolli.parceiro90.relato.Categoria;
import br.com.kallopolli.parceiro90.relato.ResultadoClassificacao;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ServicoCicloAlerta {
    private final JdbcTemplate jdbc; private final Clock clock; private final ServicoEnvioMensagens envios; private final AssinadorBotoes botoes; private final ObjectMapper mapper;
    private final boolean escalonarRh; private final String textoGrave; private final double limiar;
    public ServicoCicloAlerta(JdbcTemplate jdbc, Clock clock, ServicoEnvioMensagens envios, AssinadorBotoes botoes,ObjectMapper mapper,
            @Value("${app.alertas.escalonar-rh:false}") boolean escalonarRh,
            @Value("${app.alertas.resposta-situacao-grave:Se você estiver em perigo imediato, procure o serviço de emergência local ou uma pessoa de confiança agora.}") String textoGrave,
            @Value("${app.alertas.limiar-confianca:0.7}") double limiar) {
        this.jdbc=jdbc; this.clock=clock; this.envios=envios; this.botoes=botoes; this.mapper=mapper; this.escalonarRh=escalonarRh; this.textoGrave=textoGrave; this.limiar=limiar;
    }

    public void abrir(EventoEntrada entrada, ResultadoClassificacao resultado) {
        if(resultado.categoria()!=Categoria.SITUACAO_GRAVE && resultado.confianca()<limiar)return;
        String categoria=resultado.categoria().name(); String destino=RoteamentoAlerta.destino(categoria);
        var pessoa=jdbc.query("SELECT c.loja_id,c.iniciais,c.nome,l.nome FROM colaborador c JOIN loja l ON l.id=c.loja_id WHERE c.id=?", (rs,n)->new Pessoa(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4)), entrada.colaboradorId());
        if (pessoa.isEmpty()) return;
        String id=UUID.randomUUID().toString(); String protocolo="K90-"+UUID.randomUUID().toString().substring(0,8).toUpperCase(java.util.Locale.ROOT);
        jdbc.update("INSERT INTO alerta(id,colaborador_id,loja_id,protocolo,categoria,severidade,destino,status,criado_em,sugestao,colaborador_avisado,mensagem_entrada_wamid) VALUES (?,?,?,?,?,?,?,'ABERTO',?,?,false,?)",
                id, entrada.colaboradorId(), pessoa.get(0).lojaId(), protocolo, categoria, resultado.severidade().name(), destino, Timestamp.from(clock.instant()), resultado.sugestao(), entrada.wamid());
        projetar(id,entrada.colaboradorId(),protocolo,categoria,resultado,pessoa.get(0),destino);
        evento(id,"SINAL_IDENTIFICADO");
        if ("RH".equals(destino) || Categoria.SITUACAO_GRAVE.name().equals(categoria)) adicionarLideres(id,"RH",null);
        if ("GERENTE".equals(destino) || Categoria.SITUACAO_GRAVE.name().equals(categoria)) adicionarLideres(id,"GERENTE",pessoa.get(0).lojaId());
        var aviso=envios.paraColaborador(entrada.colaboradorId(),"AVISO_COLABORADOR_ALERTA","Recebemos sua mensagem e encaminhamos para acompanhamento. O registro é confidencial.",TemplateWhatsapp.AVISO_COLABORADOR_ALERTA.nome(),List.of("RH".equals(destino)?"RH":"sua gerência",protocolo),List.of(),false);
        if(aviso.enviado()){jdbc.update("UPDATE alerta SET colaborador_avisado=true WHERE id=?",id);jdbc.update("UPDATE dashboard_dado SET conteudo=jsonb_set(conteudo,'{colaboradorAvisado}','true'::jsonb,true) WHERE recurso='alertas' AND chave=?",id);evento(id,"COLABORADOR_AVISADO");}
        var contatos=jdbc.query("SELECT l.id,l.perfil FROM alerta_destinatario ad JOIN lider_contato l ON l.id=ad.lider_contato_id WHERE ad.alerta_id=?",(rs,n)->new DestinoLider(rs.getObject(1,UUID.class),rs.getString(2)),id);
        for(var l:contatos){
            String payload1=botoes.assinar(id,"CONVERSEI"), payload2=botoes.assinar(id,"NAO_CONSEGUI");
            String template="RH".equals(l.perfil())?TemplateWhatsapp.ALERTA_RH.nome():TemplateWhatsapp.ALERTA_LIDER.nome();
            List<String> vars="RH".equals(l.perfil())?List.of(protocolo,rotulo(categoria),resultado.sugestao()):List.of(pessoa.get(0).iniciais(),pessoa.get(0).loja(),rotulo(categoria),resultado.sugestao(),protocolo);
            var enviado=envios.paraLider(l.id(),"ALERTA_"+l.perfil(),"Novo alerta de acompanhamento. Protocolo "+protocolo+".",template,vars,List.of(payload1,payload2));
            if(enviado.enviado()) evento(id,"ALERTA_ENVIADO");
        }
        if(Categoria.SITUACAO_GRAVE.name().equals(categoria)) envios.paraColaborador(entrada.colaboradorId(),"APOIO_SITUACAO_GRAVE",textoGrave,null,List.of(),List.of(),false);
    }

    public boolean registrarRetorno(String alertaId,UUID liderId,String acao){
        int mudou=jdbc.update("UPDATE alerta_destinatario SET acao=?,retorno_em=? WHERE alerta_id=? AND lider_contato_id=? AND acao='PENDENTE'",acao,Timestamp.from(clock.instant()),alertaId,liderId);
        if(mudou==0)return false;
        jdbc.update("UPDATE alerta SET status='ATENDIDO',retorno=?,respondido_em=? WHERE id=? AND status='ABERTO'",acao,Timestamp.from(clock.instant()),alertaId);
        jdbc.update("UPDATE dashboard_dado SET conteudo=jsonb_set(jsonb_set(jsonb_set(conteudo,'{status}',to_jsonb('ATENDIDO'::text),true),'{retorno}',to_jsonb(?::text),true),'{respondidoEm}',to_jsonb(?::text),true) WHERE recurso='alertas' AND chave=?",acao,clock.instant().toString(),alertaId);
        evento(alertaId,"RETORNO_RECEBIDO");
        return true;
    }
    private void adicionarLideres(String alertaId,String perfil,Long loja){
        if(loja==null) jdbc.update("INSERT INTO alerta_destinatario(alerta_id,lider_contato_id) SELECT ?,id FROM lider_contato WHERE perfil=? AND ativo=true ON CONFLICT DO NOTHING",alertaId,perfil);
        else jdbc.update("INSERT INTO alerta_destinatario(alerta_id,lider_contato_id) SELECT ?,id FROM lider_contato WHERE perfil=? AND loja_id=? AND ativo=true ON CONFLICT DO NOTHING",alertaId,perfil,loja);
    }
    private void evento(String id,String tipo){jdbc.update("INSERT INTO alerta_evento(alerta_id,tipo,criado_em) VALUES (?,?,?) ON CONFLICT DO NOTHING",id,tipo,Timestamp.from(clock.instant()));}
    private void projetar(String id,String colaborador,String protocolo,String categoria,ResultadoClassificacao r,Pessoa pessoa,String destino){
        ObjectNode o=mapper.createObjectNode();o.put("id",id);o.put("colaboradorId",colaborador);o.put("protocolo",protocolo);o.put("categoria",categoria);o.put("severidade",r.severidade().name());o.put("destino",destino);o.put("destinatarioNome","");o.put("criadoEm",clock.instant().toString());o.put("status","ABERTO");o.putNull("retorno");o.putNull("respondidoEm");o.putNull("lembreteEnviadoEm");o.put("sugestao",r.sugestao());o.put("colaboradorAvisado",false);
        try{jdbc.update("INSERT INTO dashboard_dado(recurso,chave,conteudo) VALUES ('alertas',?,?::jsonb) ON CONFLICT(recurso,chave) DO UPDATE SET conteudo=EXCLUDED.conteudo",id,mapper.writeValueAsString(o));}catch(Exception ex){throw new IllegalStateException("Falha ao atualizar projeção do alerta.");}
    }
    private String rotulo(String c){return switch(c){case "CANSACO"->"cansaço na rotina";case "SOBRECARGA"->"apoio com tarefas";case "DESMOTIVACAO"->"experiência na função";case "VONTADE_DE_SAIR"->"conversa sobre a experiência";case "DUVIDA"->"pedido de orientação";case "CONFLITO_COLEGA"->"convivência na equipe";case "CONFLITO_CHEFIA"->"relato reservado";case "ASSEDIO"->"relato reservado";default->"pedido de apoio urgente";};}
    private record Pessoa(long lojaId,String iniciais,String nome,String loja){} private record DestinoLider(UUID id,String perfil){}
}
