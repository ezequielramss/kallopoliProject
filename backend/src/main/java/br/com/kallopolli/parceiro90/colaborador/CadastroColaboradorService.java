package br.com.kallopolli.parceiro90.colaborador;

import br.com.kallopolli.parceiro90.comum.CifradorDados;
import br.com.kallopolli.parceiro90.whatsapp.ServicoAgenda;
import br.com.kallopolli.parceiro90.whatsapp.ServicoEnvioMensagens;
import br.com.kallopolli.parceiro90.whatsapp.TemplateWhatsapp;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class CadastroColaboradorService {
    private final JdbcTemplate jdbc;
    private final HashTelefoneService hashes;
    private final CifradorDados cifrador;
    private final ServicoEnvioMensagens envios;
    private final ServicoAgenda agenda;
    private final ObjectMapper mapper;
    private final Clock clock;
    public CadastroColaboradorService(JdbcTemplate jdbc, HashTelefoneService hashes, CifradorDados cifrador,
            ServicoEnvioMensagens envios, ServicoAgenda agenda, ObjectMapper mapper, Clock clock) {
        this.jdbc = jdbc; this.hashes = hashes; this.cifrador = cifrador; this.envios = envios; this.agenda = agenda; this.mapper = mapper; this.clock = clock;
    }
    public NovoColaboradorResposta cadastrar(NovoColaboradorPedido pedido) {
        var lojas = jdbc.query("SELECT nome FROM loja WHERE id=? AND ativa=true", (rs,n) -> rs.getString(1), pedido.lojaId());
        if (lojas.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loja não encontrada.");
        String telefoneHash = hashes.gerar(pedido.telefone());
        if (!jdbc.query("SELECT id FROM colaborador WHERE telefone_hash=?", (rs,n) -> rs.getString(1), telefoneHash).isEmpty())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este telefone já está cadastrado.");
        String id = UUID.randomUUID().toString(); String telefoneCifrado = cifrador.cifrar(pedido.telefone());
        jdbc.update("INSERT INTO colaborador(id,loja_id,nome,iniciais,funcao,telefone_cifrado,telefone_hash,consentimento_em,data_admissao,participacao_status) VALUES (?,?,?,?,?,?,?,?,?,'PENDENTE_OPTIN')",
            id, pedido.lojaId(), pedido.nome().trim(), iniciais(pedido.nome()), pedido.funcao().trim(), telefoneCifrado, telefoneHash,
            Timestamp.from(pedido.consentimentoEm()), pedido.dataAdmissao());
        salvarProjecao(id, pedido, lojas.get(0));
        String aceita = "OPTIN:" + id + ":ACEITAR", recusa = "OPTIN:" + id + ":RECUSAR";
        envios.paraColaborador(id, "BOAS_VINDAS_OPTIN", "Boas-vindas e convite voluntário para participar.",
            TemplateWhatsapp.BOAS_VINDAS_OPTIN.nome(), List.of(), List.of(aceita, recusa), true);
        agenda.agendarExpiracaoOptin(id);
        return new NovoColaboradorResposta(id, pedido.nome().trim(), pedido.funcao().trim(), lojas.get(0), mascarar(pedido.telefone()), pedido.consentimentoEm(), "PENDENTE_OPTIN");
    }
    private void salvarProjecao(String id, NovoColaboradorPedido pedido, String loja) {
        ObjectNode item = mapper.createObjectNode(); item.put("id", id); item.put("nome", pedido.nome().trim()); item.put("iniciais", iniciais(pedido.nome()));
        item.put("funcao", pedido.funcao().trim()); item.put("loja", loja); item.put("diaJornada", 1); item.put("presenca", 100); item.put("faltas", 0);
        item.putNull("ultimoContato"); item.put("nivelAtencao", "Estável"); item.put("motivoAtencao", "Aguardando resposta voluntária ao convite de participação."); item.putNull("proximoCheckin");
        try { jdbc.update("INSERT INTO dashboard_dado(recurso,chave,conteudo) VALUES ('colaboradores',?,?::jsonb) ON CONFLICT(recurso,chave) DO UPDATE SET conteudo=EXCLUDED.conteudo", id, mapper.writeValueAsString(item)); }
        catch (Exception ex) { throw new IllegalStateException("Não foi possível registrar o colaborador."); }
    }
    private String iniciais(String nome) { return java.util.Arrays.stream(nome.trim().split("\\s+")).filter(p -> !p.isBlank()).limit(2).map(p -> p.substring(0,1).toUpperCase(java.util.Locale.ROOT)).reduce("", String::concat); }
    private String mascarar(String telefone) { String digitos = telefone.replaceAll("\\D", ""); return "+" + "•".repeat(Math.max(0, digitos.length() - 4)) + digitos.substring(Math.max(0, digitos.length() - 4)); }
}
