package br.com.kallopolli.parceiro90.whatsapp;

import br.com.kallopolli.parceiro90.comum.CifradorDados;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "test"})
public class FakeWhatsAppClient implements WhatsAppClient {
    private final JdbcTemplate jdbc;
    private final CifradorDados cifrador;
    public FakeWhatsAppClient(JdbcTemplate jdbc, CifradorDados cifrador) { this.jdbc = jdbc; this.cifrador = cifrador; }
    @Override public String enviar(MensagemWhatsapp mensagem) {
        jdbc.update("INSERT INTO outbox_dev(mensagem_saida_id,destinatario_hash,tipo,template_nome,conteudo_cifrado) VALUES (?,?,?,?,?)",
            mensagem.id(), mensagem.destinatarioHash(), mensagem.tipo(), mensagem.templateNome(), cifrador.cifrar(mensagem.texto()));
        return "fake-" + mensagem.id();
    }
}
