package br.com.kallopolli.parceiro90.whatsapp;

import br.com.kallopolli.parceiro90.comum.CifradorDados;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ServicoEnvioMensagens {
    private final JdbcTemplate jdbc;
    private final CifradorDados cifrador;
    private final WhatsAppClient client;
    private final Clock clock;
    private final int maxRetries;
    private final int limitePorSegundo;
    private long proximoEnvioNanos;

    public ServicoEnvioMensagens(JdbcTemplate jdbc, CifradorDados cifrador, WhatsAppClient client, Clock clock,
            @Value("${app.whatsapp.tentativas-maximas:3}") int maxRetries,
            @Value("${app.whatsapp.max-envios-por-segundo:20}") int limitePorSegundo) {
        this.jdbc = jdbc; this.cifrador = cifrador; this.client = client; this.clock = clock;
        this.maxRetries = Math.max(1, maxRetries); this.limitePorSegundo = Math.max(1, limitePorSegundo);
    }

    public ResultadoEnvio paraColaborador(String id, String tipo, String texto, String template, List<String> variaveis, List<String> botoes, boolean permiteNaoAtivo) {
        var contatos = jdbc.query("SELECT telefone_cifrado,telefone_hash,ultima_mensagem_entrada_em,participacao_status FROM colaborador WHERE id=?",
            (rs,n) -> new Destino(rs.getString(1), rs.getString(2), instante(rs.getTimestamp(3)), rs.getString(4), id, null), id);
        if (contatos.isEmpty()) throw new IllegalArgumentException("Colaborador não encontrado.");
        var contato = contatos.get(0);
        if (!permiteNaoAtivo && !"ATIVO".equals(contato.status())) return registrarFalha(contato, tipo, texto, template, "PARTICIPACAO_INATIVA");
        return enviar(contato, tipo, texto, template, variaveis, botoes);
    }

    public ResultadoEnvio paraLider(UUID id, String tipo, String texto, String template, List<String> variaveis, List<String> botoes) {
        var contatos = jdbc.query("SELECT telefone_cifrado,telefone_hash,ultima_mensagem_entrada_em FROM lider_contato WHERE id=? AND ativo=true",
            (rs,n) -> new Destino(rs.getString(1), rs.getString(2), instante(rs.getTimestamp(3)), null, null, id), id);
        if (contatos.isEmpty()) throw new IllegalArgumentException("Contato de liderança indisponível.");
        return enviar(contatos.get(0), tipo, texto, template, variaveis, botoes);
    }

    private ResultadoEnvio enviar(Destino destino, String tipo, String texto, String template, List<String> variaveis, List<String> botoes) {
        UUID id = UUID.randomUUID(); Instant agora = clock.instant();
        String fallback = tipo.startsWith("CADENCIA_") ? TemplateWhatsapp.RETOMADA_CONVERSA.nome() : null;
        String escolhido = template;
        var decisao = PoliticaJanela.decidir(destino.ultimaEntrada(), agora, escolhido == null ? fallback : escolhido);
        if (decisao == PoliticaJanela.Decisao.BLOQUEAR) return registrarFalha(destino, tipo, texto, null, "JANELA_24H_FECHADA");
        if (decisao == PoliticaJanela.Decisao.TEMPLATE && escolhido == null) escolhido = fallback;
        jdbc.update("INSERT INTO mensagem_saida(id,destinatario_hash,colaborador_id,lider_contato_id,tipo,template_nome,conteudo_cifrado,status,criada_em,atualizada_em) VALUES (?,?,?,?,?,?,?,'PENDENTE',?,?)",
            id, destino.hash(), destino.colaboradorId(), destino.liderId(), tipo, escolhido, cifrador.cifrar(texto), Timestamp.from(agora), Timestamp.from(agora));
        var mensagem = new MensagemWhatsapp(id, cifrador.decifrar(destino.telefoneCifrado()), destino.hash(), destino.colaboradorId(), destino.liderId(), tipo, escolhido, texto, variaveis, botoes);
        FalhaWhatsAppException falha = null;
        for (int tentativa = 1; tentativa <= maxRetries; tentativa++) {
            try {
                limitarTaxa(); String wamid = client.enviar(mensagem);
                jdbc.update("UPDATE mensagem_saida SET wamid=?,status='ENVIADA',enviada_em=?,atualizada_em=? WHERE id=?", wamid, Timestamp.from(clock.instant()), Timestamp.from(clock.instant()), id);
                return new ResultadoEnvio(id, true, null);
            } catch (FalhaWhatsAppException ex) {
                falha = ex;
                if (!ex.permiteRetry() || tentativa == maxRetries) break;
                aguardar(100L << (tentativa - 1));
            } catch (RuntimeException ex) {
                jdbc.update("UPDATE mensagem_saida SET status='FALHA',erro_codigo='ERRO_INTERNO',atualizada_em=? WHERE id=?", Timestamp.from(clock.instant()), id);
                return new ResultadoEnvio(id, false, "ERRO_INTERNO");
            }
        }
        String codigo = falha == null ? "PROVEDOR_INDISPONIVEL" : falha.codigoSeguro();
        jdbc.update("UPDATE mensagem_saida SET status='FALHA',erro_codigo=?,atualizada_em=? WHERE id=?", codigo, Timestamp.from(clock.instant()), id);
        return new ResultadoEnvio(id, false, codigo);
    }

    private ResultadoEnvio registrarFalha(Destino destino, String tipo, String texto, String template, String codigo) {
        UUID id = UUID.randomUUID(); Instant now = clock.instant();
        jdbc.update("INSERT INTO mensagem_saida(id,destinatario_hash,colaborador_id,lider_contato_id,tipo,template_nome,conteudo_cifrado,status,erro_codigo,criada_em,atualizada_em) VALUES (?,?,?,?,?,?,?,'FALHA',?,?,?)",
            id, destino.hash(), destino.colaboradorId(), destino.liderId(), tipo, template, cifrador.cifrar(texto), codigo, Timestamp.from(now), Timestamp.from(now));
        return new ResultadoEnvio(id, false, codigo);
    }
    private synchronized void limitarTaxa() {
        long intervalo = 1_000_000_000L / limitePorSegundo; long now = System.nanoTime();
        long espera = proximoEnvioNanos - now;
        if (espera > 0) aguardar(Math.max(1, espera / 1_000_000));
        proximoEnvioNanos = Math.max(System.nanoTime(), proximoEnvioNanos) + intervalo;
    }
    private void aguardar(long millis) {
        try { Thread.sleep(millis); }
        catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw new IllegalStateException("Envio interrompido."); }
    }
    private Instant instante(Timestamp timestamp) { return timestamp == null ? null : timestamp.toInstant(); }
    private record Destino(String telefoneCifrado, String hash, Instant ultimaEntrada, String status, String colaboradorId, UUID liderId) { }
}
