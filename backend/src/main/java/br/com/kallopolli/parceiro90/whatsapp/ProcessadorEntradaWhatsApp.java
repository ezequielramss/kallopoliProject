package br.com.kallopolli.parceiro90.whatsapp;

import br.com.kallopolli.parceiro90.comum.CifradorDados;
import br.com.kallopolli.parceiro90.relato.ClassificadorSinais;
import br.com.kallopolli.parceiro90.relato.EntradaClassificacao;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProcessadorEntradaWhatsApp {
    private final JdbcTemplate jdbc;
    private final Clock clock;
    private final ServicoEnvioMensagens envios;
    private final ClassificadorSinais classificador;
    private final CifradorDados cifrador;
    private final AssinadorBotoes botoes;
    private final ServicoCicloAlerta alertas;
    private final ServicoAgenda agenda;

    public ProcessadorEntradaWhatsApp(JdbcTemplate jdbc, Clock clock, ServicoEnvioMensagens envios,
            ClassificadorSinais classificador, CifradorDados cifrador, AssinadorBotoes botoes, ServicoCicloAlerta alertas, ServicoAgenda agenda) {
        this.jdbc=jdbc; this.clock=clock; this.envios=envios; this.classificador=classificador; this.cifrador=cifrador; this.botoes=botoes; this.alertas=alertas; this.agenda=agenda;
    }

    public void processar(EventoEntrada e) {
        if (e.colaboradorId() != null) { processarColaborador(e); return; }
        if (e.liderContatoId() != null) processarLider(e);
    }

    private void processarColaborador(EventoEntrada e) {
        String texto = normalizarComando(e.conteudo());
        if (List.of("sair", "parar", "cancelar").contains(texto)) {
            jdbc.update("UPDATE colaborador SET participacao_status='OPT_OUT',opt_out_em=? WHERE id=?", Timestamp.from(clock.instant()), e.colaboradorId());
            jdbc.update("UPDATE mensagem_agendada SET status='CANCELADA' WHERE colaborador_id=? AND status IN ('PENDENTE','PROCESSANDO')", e.colaboradorId());
            envios.paraColaborador(e.colaboradorId(), "CONFIRMACAO_OPT_OUT", "Tudo bem. Sua participação foi encerrada e as mensagens foram interrompidas.", null, List.of(), List.of(), true); return;
        }
        if (e.estadoParticipacao() == null || "PENDENTE_OPTIN".equals(e.estadoParticipacao())) {
            processarOptin(e); return;
        }
        if (!"ATIVO".equals(e.estadoParticipacao())) return;
        if (texto.equals("ajuda")) {
            envios.paraColaborador(e.colaboradorId(), "AJUDA", "Sou o Parceiro dos 90 dias e acompanho sua chegada ao trabalho. Se você compartilhar algo que precise de apoio, a gerência recebe categoria e sugestão; situações de assédio ou conflito com chefia vão ao RH. Sua participação é voluntária: envie SAIR para encerrar.", null, List.of(), List.of(), false); return;
        }
        if (!"text".equals(e.tipo())) {
            envios.paraColaborador(e.colaboradorId(), "FORMATO_NAO_SUPORTADO", "Por enquanto consigo acompanhar mensagens de texto. Se quiser, escreva o que gostaria de compartilhar.", null, List.of(), List.of(), false); return;
        }
        var dados = jdbc.query("SELECT data_admissao FROM colaborador WHERE id=?", (rs,n) -> rs.getDate(1).toLocalDate(), e.colaboradorId());
        if (dados.isEmpty()) return;
        int dia = Math.max(1, Math.min(90, (int) java.time.temporal.ChronoUnit.DAYS.between(dados.get(0), clock.instant().atZone(java.time.ZoneId.of("America/Sao_Paulo")).toLocalDate()) + 1));
        var ultimas = jdbc.query("SELECT conteudo_cifrado FROM mensagem_entrada WHERE colaborador_id=? AND wamid<>? AND tipo='text' ORDER BY recebida_em DESC LIMIT 5", (rs,n) -> rs.getString(1), e.colaboradorId(), e.wamid());
        var contexto = ultimas.stream().map(cifrador::decifrar).toList();
        var r = classificador.classificar(new EntradaClassificacao(e.conteudo(), dia, contexto));
        jdbc.update("UPDATE mensagem_entrada SET categoria=?,severidade=?,resumo_neutro=?,sugestao=?,processada_em=? WHERE wamid=?", r.categoria().name(), r.severidade().name(), r.resumoNeutro(), r.sugestao(), Timestamp.from(clock.instant()), e.wamid());
        if (r.categoria() != br.com.kallopolli.parceiro90.relato.Categoria.NEUTRO) alertas.abrir(e, r);
    }

    private void processarOptin(EventoEntrada e) {
        String payload = e.tipo().equals("interactive") ? e.conteudo() : "";
        String[] partes = payload.split(":", 3);
        if (partes.length != 3 || !"OPTIN".equals(partes[0]) || !partes[1].equals(e.colaboradorId())) return;
        String status = "ACEITAR".equals(partes[2]) ? "ATIVO" : "RECUSAR".equals(partes[2]) ? "NAO_PARTICIPA" : null;
        if (status == null) return;
        jdbc.update("UPDATE colaborador SET participacao_status=? WHERE id=? AND participacao_status='PENDENTE_OPTIN'", status, e.colaboradorId());
        jdbc.update("UPDATE mensagem_agendada SET status='CANCELADA' WHERE colaborador_id=? AND tipo='OPTIN_EXPIRA' AND status='PENDENTE'", e.colaboradorId());
        if ("ATIVO".equals(status)) {
            var admissao = jdbc.queryForObject("SELECT data_admissao FROM colaborador WHERE id=?", java.time.LocalDate.class, e.colaboradorId());
            agenda.ativarJornada(e.colaboradorId(), admissao);
        } else envios.paraColaborador(e.colaboradorId(), "OPTIN_RECUSADO", "Obrigado por responder. Sua decisão foi registrada e você não receberá mensagens.", null, List.of(), List.of(), true);
    }

    private void processarLider(EventoEntrada e) {
        AcaoBotao acao = botoes.validar(e.conteudo());
        if (acao == null) return;
        var permitido = jdbc.query("SELECT 1 FROM alerta_destinatario WHERE alerta_id=? AND lider_contato_id=?", (rs,n) -> true, acao.alertaId(), e.liderContatoId());
        if (permitido.isEmpty()) return;
        if(alertas.registrarRetorno(acao.alertaId(), e.liderContatoId(), acao.acao()))
            envios.paraLider(e.liderContatoId(), "CONFIRMACAO_RETORNO", "Obrigado, retorno registrado.", null, List.of(), List.of());
    }
    static String normalizarComando(String valor) { return Normalizer.normalize(valor == null ? "" : valor, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").replaceAll("[.!?,;:]", "").trim().toLowerCase(Locale.ROOT); }
}
