package br.com.kallopolli.parceiro90.relato;

import br.com.kallopolli.parceiro90.comum.DashboardDados;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/relatos")
public class RelatoController {
    private final DashboardDados dados;
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    public RelatoController(DashboardDados dados, JdbcTemplate jdbc, ObjectMapper mapper) { this.dados = dados; this.jdbc = jdbc; this.mapper = mapper; }

    @GetMapping List<JsonNode> listar(Authentication auth, @RequestParam(required=false) String status,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="100") int size) {
        dados.exigirRh(auth); dados.validarPaginacao(page, size); var resumos = new ArrayList<JsonNode>();
        for (JsonNode relato : dados.lista("relatos")) {
            if (status != null && !status.isBlank() && !status.equals(relato.path("status").asText())) continue;
            ObjectNode resumo = mapper.createObjectNode();
            for (String campo : List.of("protocolo", "titulo", "categoria", "severidade", "status", "criadoEm")) resumo.set(campo, relato.get(campo));
            resumos.add(resumo);
        }
        return dados.pagina(resumos, page, size);
    }
    @GetMapping("/{protocolo}") @Transactional JsonNode detalhe(@PathVariable String protocolo, Authentication auth) {
        dados.exigirRh(auth); JsonNode relato = dados.encontrar("relatos", protocolo);
        jdbc.update("INSERT INTO auditoria_acesso(usuario_id,acao,protocolo) VALUES (?,'LEITURA_RELATO',?)", dados.usuarioId(auth), protocolo);
        return relato;
    }
    @PatchMapping("/{protocolo}") @Transactional JsonNode atualizar(@PathVariable String protocolo, @Valid @RequestBody AtualizacaoRelato patch, Authentication auth) {
        dados.exigirRh(auth);
        if (!List.of("NOVO", "EM_ANALISE", "CONCLUIDO").contains(patch.status())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status de relato inválido.");
        JsonNode relatoOriginal = dados.encontrar("relatos", protocolo); ObjectNode relato = ((ObjectNode) relatoOriginal).deepCopy();
        String anterior = relato.path("status").asText(); relato.put("status", patch.status());
        long relatoId = jdbc.queryForObject("SELECT id FROM relato_confidencial WHERE protocolo=?", Long.class, protocolo);
        jdbc.update("UPDATE relato_confidencial SET status=? WHERE id=?", patch.status(), relatoId);
        OffsetDateTime agora = OffsetDateTime.now();
        if (!anterior.equals(patch.status())) jdbc.update("INSERT INTO relato_historico(relato_id,titulo,descricao,criado_em) VALUES (?,?,?,?)", relatoId, "Status atualizado", "Status alterado para " + patch.status() + ".", agora);
        if (patch.observacaoInterna() != null && !patch.observacaoInterna().isBlank()) {
            ArrayNode historico = relato.withArray("historico"); ObjectNode evento = mapper.createObjectNode();
            evento.put("titulo", "Atualização interna"); evento.put("data", agora.toString()); evento.put("descricao", patch.observacaoInterna()); historico.add(evento);
            jdbc.update("INSERT INTO relato_historico(relato_id,titulo,descricao,criado_em) VALUES (?,?,?,?)", relatoId, "Atualização interna", patch.observacaoInterna(), agora);
        }
        dados.salvar("relatos", protocolo, relato);
        jdbc.update("INSERT INTO auditoria_acesso(usuario_id,acao,protocolo) VALUES (?,'ALTERACAO_RELATO',?)", dados.usuarioId(auth), protocolo);
        return relato;
    }
}
