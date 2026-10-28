package br.com.kallopolli.parceiro90.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import br.com.kallopolli.parceiro90.comum.CifradorDados;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController @RequestMapping("/dev/whatsapp") @Profile({"dev","test"})
public class SimulacaoWhatsAppController {
    private final EventoWhatsAppHandler handler; private final ObjectMapper mapper; private final JdbcTemplate jdbc; private final CifradorDados cifrador; private final AssinadorBotoes botoes;
    public SimulacaoWhatsAppController(EventoWhatsAppHandler handler,ObjectMapper mapper,JdbcTemplate jdbc,CifradorDados cifrador,AssinadorBotoes botoes){this.handler=handler;this.mapper=mapper;this.jdbc=jdbc;this.cifrador=cifrador;this.botoes=botoes;}
    @PostMapping("/entrada") public Resposta entrada(@Valid @RequestBody Pedido pedido){
        try{
            var mensagem=mapper.createObjectNode(); mensagem.put("id","dev-"+java.util.UUID.randomUUID()); mensagem.put("from",pedido.telefone().substring(1)); mensagem.put("type","text"); mensagem.putObject("text").put("body",pedido.texto());
            handler.mensagem(mensagem); return new Resposta("recebido");
        }catch(RuntimeException ex){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Não foi possível processar a mensagem de simulação.");}
    }
    @PostMapping("/retorno") public Resposta retorno(@Valid @RequestBody RetornoPedido pedido){
        var telefones=jdbc.query("SELECT telefone_cifrado FROM lider_contato WHERE id=? AND ativo=true",(rs,n)->rs.getString(1),pedido.liderId());
        if(telefones.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Contato não encontrado.");
        var mensagem=mapper.createObjectNode();mensagem.put("id","dev-"+java.util.UUID.randomUUID());mensagem.put("from",cifrador.decifrar(telefones.get(0)).substring(1));mensagem.put("type","interactive");
        mensagem.putObject("interactive").putObject("button_reply").put("id",botoes.assinar(pedido.alertaId(),pedido.acao())).put("title",pedido.acao());handler.mensagem(mensagem);return new Resposta("retorno simulado");
    }
    public record Pedido(@NotBlank @Pattern(regexp="\\+[1-9][0-9]{7,14}") String telefone,@NotBlank @Size(max=4000) String texto){}
    public record RetornoPedido(@NotBlank String alertaId,@NotNull UUID liderId,@NotBlank @Pattern(regexp="CONVERSEI|NAO_CONSEGUI") String acao){}
    public record Resposta(String status){}
}
