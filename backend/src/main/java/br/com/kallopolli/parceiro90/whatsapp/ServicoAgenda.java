package br.com.kallopolli.parceiro90.whatsapp;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class ServicoAgenda {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transacoes;
    private final Clock clock;
    private final CadenciaProperties config;
    private final ConteudoCadencia conteudo;
    private final ServicoEnvioMensagens envios;
    public ServicoAgenda(JdbcTemplate jdbc, TransactionTemplate transacoes, Clock clock, CadenciaProperties config,
            ConteudoCadencia conteudo, ServicoEnvioMensagens envios) {
        this.jdbc = jdbc; this.transacoes = transacoes; this.clock = clock; this.config = config; this.conteudo = conteudo; this.envios = envios;
    }

    public int agendarExpiracaoOptin(String colaboradorId) {
        Instant quando = clock.instant().plus(config.getExpiraOptinHoras(), ChronoUnit.HOURS);
        return jdbc.update("INSERT INTO mensagem_agendada(id,colaborador_id,tipo,dia_jornada,agendada_para,status) VALUES (?,?, 'OPTIN_EXPIRA',0,?,'PENDENTE') ON CONFLICT(colaborador_id,tipo,dia_jornada) DO NOTHING",
            UUID.randomUUID(), colaboradorId, Timestamp.from(quando));
    }

    public int ativarJornada(String colaboradorId, LocalDate dataAdmissao) {
        Instant agora = clock.instant(); var zone = configZone(); int criadas = 0, indice = 0;
        for (int dia = 1; dia <= 90; dia++) {
            boolean ponte=config.getPonteDias().contains(dia);
            if (!ponte && !config.diaPrevisto(dia)) continue;
            indice++;
            LocalDate dataMensagem = dataAdmissao.plusDays(dia - 1L);
            Instant quando = dataMensagem.atTime(config.getInicioJanela()).atZone(zone).toInstant();
            if (quando.isBefore(agora)) continue;
            String tipo = ponte ? "PONTE" : indice % 4 == 0 ? "PILULA" : "CHECKIN";
            criadas += jdbc.update("INSERT INTO mensagem_agendada(id,colaborador_id,tipo,dia_jornada,agendada_para,status) VALUES (?,?,?,?,?,'PENDENTE') ON CONFLICT(colaborador_id,tipo,dia_jornada) DO NOTHING",
                UUID.randomUUID(), colaboradorId, tipo, dia, Timestamp.from(quando));
        }
        return criadas;
    }

    @Scheduled(fixedDelayString="${app.cadencia.intervalo-ms:60000}")
    public void enviarPendentes() {
        var itens = transacoes.execute(status -> jdbc.query(
            "WITH selecionadas AS (SELECT id FROM mensagem_agendada WHERE status='PENDENTE' AND agendada_para<=? ORDER BY agendada_para LIMIT 25 FOR UPDATE SKIP LOCKED) " +
            "UPDATE mensagem_agendada m SET status='PROCESSANDO',tentativas=tentativas+1 FROM selecionadas s WHERE m.id=s.id " +
            "RETURNING m.id,m.colaborador_id,m.tipo,m.dia_jornada",
            (rs,n) -> new Item(rs.getObject("id", UUID.class), rs.getString("colaborador_id"), rs.getString("tipo"), rs.getInt("dia_jornada")), Timestamp.from(clock.instant())));
        if (itens == null) return;
        for (Item item : itens) {
            try { processar(item); }
            catch (RuntimeException ex) { marcar(item, "PENDENTE", "FALHA_PROCESSAMENTO"); }
        }
    }

    private void processar(Item item) {
        if ("OPTIN_EXPIRA".equals(item.tipo())) {
            jdbc.update("UPDATE colaborador SET participacao_status='NAO_PARTICIPA' WHERE id=? AND participacao_status='PENDENTE_OPTIN'", item.colaboradorId());
            jdbc.update("UPDATE mensagem_agendada SET status='ENVIADA' WHERE id=?", item.id()); return;
        }
        var estado = jdbc.query("SELECT participacao_status FROM colaborador WHERE id=?", (rs,n) -> rs.getString(1), item.colaboradorId());
        if (estado.isEmpty() || !"ATIVO".equals(estado.get(0))) { marcar(item, "CANCELADA", null); return; }
        Instant agora = clock.instant();
        if (!janelaPermitida(agora)) {
            jdbc.update("UPDATE mensagem_agendada SET status='PENDENTE',agendada_para=? WHERE id=?", Timestamp.from(proximaJanela(agora)), item.id()); return;
        }
        String texto; String tipoEnvio;
        if ("PONTE".equals(item.tipo())) { texto = conteudo.ponte(item.dia()); tipoEnvio = "CADENCIA_PONTE"; }
        else if ("PILULA".equals(item.tipo()) && podeEnviarPilula(item.colaboradorId())) { texto = conteudo.pilula(item.dia()); tipoEnvio = "CADENCIA_PILULA"; }
        else { texto = conteudo.checkin(item.dia(),proximaVariacao(item.colaboradorId())); tipoEnvio = "CADENCIA_CHECKIN"; }
        ResultadoEnvio resultado = envios.paraColaborador(item.colaboradorId(), tipoEnvio, texto, null, List.of(), List.of(), false);
        marcar(item, resultado.enviado() ? "ENVIADA" : "FALHA", resultado.motivo());
        if (resultado.enviado()) atualizarProximoContato(item.colaboradorId());
    }

    private boolean podeEnviarPilula(String colaboradorId) {
        Integer depois = jdbc.queryForObject("SELECT count(*) FROM mensagem_saida WHERE colaborador_id=? AND status IN ('ENVIADA','ENTREGUE','LIDA') AND enviada_em > COALESCE((SELECT max(enviada_em) FROM mensagem_saida WHERE colaborador_id=? AND tipo='CADENCIA_PILULA'),to_timestamp(0)) AND tipo<>'CADENCIA_PILULA'", Integer.class, colaboradorId, colaboradorId);
        return depois != null && depois >= 3;
    }
    private int proximaVariacao(String colaboradorId) {
        Integer enviados=jdbc.queryForObject("SELECT count(*) FROM mensagem_saida WHERE colaborador_id=? AND tipo='CADENCIA_CHECKIN' AND status IN ('ENVIADA','ENTREGUE','LIDA')",Integer.class,colaboradorId);
        return enviados==null?0:enviados;
    }
    private boolean janelaPermitida(Instant instante) {
        LocalTime hora = instante.atZone(configZone()).toLocalTime();
        return PoliticaHorario.permite(hora,config.getInicioJanela(),config.getFimJanela(),config.getInicioSilencio(),config.getFimSilencio());
    }
    private Instant proximaJanela(Instant instante) {
        var atual = instante.atZone(configZone()); LocalDate data = atual.toLocalDate();
        if (atual.toLocalTime().isBefore(config.getInicioJanela())) return data.atTime(config.getInicioJanela()).atZone(configZone()).toInstant();
        return data.plusDays(1).atTime(config.getInicioJanela()).atZone(configZone()).toInstant();
    }
    private java.time.ZoneId configZone() { return java.time.ZoneId.of(config.getFuso()); }
    private void marcar(Item item, String status, String erro) { jdbc.update("UPDATE mensagem_agendada SET status=?,erro_codigo=? WHERE id=?", status, erro, item.id()); }
    private void atualizarProximoContato(String id) {
        var proximo = jdbc.query("SELECT agendada_para FROM mensagem_agendada WHERE colaborador_id=? AND status='PENDENTE' ORDER BY agendada_para LIMIT 1", (rs,n) -> rs.getTimestamp(1).toInstant(), id);
        String proximoText = proximo.isEmpty() ? null : proximo.get(0).atZone(configZone()).toLocalDate().toString();
        jdbc.update("UPDATE dashboard_dado SET conteudo=jsonb_set(jsonb_set(conteudo,'{ultimoContato}',to_jsonb(?::text),true),'{proximoCheckin}',COALESCE(to_jsonb(?::text),'null'::jsonb),true) WHERE recurso='colaboradores' AND chave=?",
            clock.instant().toString(), proximoText, id);
    }
    private record Item(UUID id, String colaboradorId, String tipo, int dia) { }
}
