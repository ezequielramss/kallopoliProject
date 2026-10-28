package br.com.kallopolli.parceiro90.whatsapp;

import br.com.kallopolli.parceiro90.colaborador.HashTelefoneService;
import br.com.kallopolli.parceiro90.comum.CifradorDados;
import com.fasterxml.jackson.databind.JsonNode;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepositorioWebhookWhatsApp {
    private final JdbcTemplate jdbc;
    private final HashTelefoneService hashes;
    private final CifradorDados cifrador;
    private final Clock clock;
    public RepositorioWebhookWhatsApp(JdbcTemplate jdbc, HashTelefoneService hashes, CifradorDados cifrador, Clock clock) {
        this.jdbc = jdbc; this.hashes = hashes; this.cifrador = cifrador; this.clock = clock;
    }

    @Transactional public EventoEntrada registrar(JsonNode mensagem) {
        String wamid = mensagem.path("id").asText(); String from = mensagem.path("from").asText();
        if (wamid.isBlank() || from.isBlank()) return null;
        String tipo = mensagem.path("type").asText("unsupported"); String conteudo = conteudo(mensagem, tipo);
        String botao = textoBotao(mensagem, tipo); String hash = hashes.gerar(from);
        int novo = jdbc.update("INSERT INTO webhook_evento(wamid,evento_chave,tipo,estado) VALUES (?,?, 'MENSAGEM','PROCESSANDO') ON CONFLICT DO NOTHING", wamid, wamid);
        if (novo == 0) {
            var estados = jdbc.query("SELECT estado,recebido_em FROM webhook_evento WHERE wamid=? FOR UPDATE", (rs,n) -> new Object[]{rs.getString(1),rs.getTimestamp(2).toInstant()}, wamid);
            if (estados.isEmpty() || estados.get(0)[0].equals("PROCESSADO")) return null;
            if (estados.get(0)[0].equals("PROCESSANDO") && ((java.time.Instant) estados.get(0)[1]).isAfter(clock.instant().minusSeconds(600))) return null;
            jdbc.update("UPDATE webhook_evento SET estado='PROCESSANDO',recebido_em=?,erro_codigo=NULL WHERE wamid=?", Timestamp.from(clock.instant()), wamid);
        }
        var colaboradores = jdbc.query("SELECT id,participacao_status FROM colaborador WHERE telefone_hash=?", (rs,n) -> new String[]{rs.getString(1),rs.getString(2)}, hash);
        String colaboradorId = colaboradores.isEmpty() ? null : colaboradores.get(0)[0];
        String participacao = colaboradores.isEmpty() ? null : colaboradores.get(0)[1];
        List<UUID> lideres = jdbc.query("SELECT id FROM lider_contato WHERE telefone_hash=? AND ativo=true", (rs,n) -> rs.getObject(1, UUID.class), hash);
        UUID liderId = lideres.isEmpty() ? null : lideres.get(0);
        jdbc.update("INSERT INTO mensagem_entrada(wamid,colaborador_id,lider_contato_id,remetente_hash,tipo,conteudo_cifrado,recebida_em) VALUES (?,?,?,?,?,?,?) ON CONFLICT(wamid) DO NOTHING",
            wamid, colaboradorId, liderId, hash, tipo, cifrador.cifrar(conteudo), Timestamp.from(clock.instant()));
        if (colaboradorId != null) jdbc.update("UPDATE colaborador SET ultima_mensagem_entrada_em=? WHERE id=?", Timestamp.from(clock.instant()), colaboradorId);
        if (liderId != null) jdbc.update("UPDATE lider_contato SET ultima_mensagem_entrada_em=? WHERE id=?", Timestamp.from(clock.instant()), liderId);
        return new EventoEntrada(wamid, hash, tipo, conteudo, botao, colaboradorId, liderId, participacao);
    }

    @Transactional public String iniciarStatus(JsonNode status) {
        String wamid = status.path("id").asText(); String nomeStatus = status.path("status").asText();
        if (wamid.isBlank() || nomeStatus.isBlank()) return null;
        String chave = "STATUS:" + wamid + ":" + nomeStatus + ":" + status.path("timestamp").asText();
        int insert = jdbc.update("INSERT INTO webhook_evento(wamid,evento_chave,tipo,estado) VALUES (NULL,?,?, 'PROCESSANDO') ON CONFLICT DO NOTHING", chave, "STATUS");
        return insert > 0 ? chave : null;
    }

    @Transactional public void atualizarStatus(JsonNode status) {
        String wamid = status.path("id").asText(); String recebido = status.path("status").asText();
        String statusDb = switch (recebido) { case "sent" -> "ENVIADA"; case "delivered" -> "ENTREGUE"; case "read" -> "LIDA"; case "failed" -> "FALHA"; default -> null; };
        if (statusDb == null) return;
        String erro = status.path("errors").path(0).path("code").asText(null);
        jdbc.update("UPDATE mensagem_saida SET status=?,erro_codigo=?,atualizada_em=? WHERE wamid=?", statusDb, erro, Timestamp.from(clock.instant()), wamid);
    }
    @Transactional public void finalizar(String wamid, boolean sucesso, String codigo) {
        jdbc.update("UPDATE webhook_evento SET estado=?,processado_em=?,erro_codigo=? WHERE wamid=?",
            sucesso ? "PROCESSADO" : "ERRO", sucesso ? Timestamp.from(clock.instant()) : null, codigo, wamid);
    }
    @Transactional public void finalizarStatus(String chave) {
        jdbc.update("UPDATE webhook_evento SET estado='PROCESSADO',processado_em=? WHERE evento_chave=?", Timestamp.from(clock.instant()), chave);
    }
    private String conteudo(JsonNode mensagem, String tipo) {
        return switch (tipo) {
            case "text" -> mensagem.path("text").path("body").asText("");
            case "button" -> mensagem.path("button").path("payload").asText(mensagem.path("button").path("text").asText(""));
            case "interactive" -> mensagem.path("interactive").path("button_reply").path("id").asText(mensagem.path("interactive").path("button_reply").path("title").asText(""));
            default -> "";
        };
    }
    private String textoBotao(JsonNode mensagem, String tipo) {
        return switch (tipo) {
            case "button" -> mensagem.path("button").path("text").asText("");
            case "interactive" -> mensagem.path("interactive").path("button_reply").path("title").asText("");
            default -> "";
        };
    }
}
