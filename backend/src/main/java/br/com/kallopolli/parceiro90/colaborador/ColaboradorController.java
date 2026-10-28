package br.com.kallopolli.parceiro90.colaborador;

import br.com.kallopolli.parceiro90.comum.DashboardDados;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/colaboradores")
public class ColaboradorController {
    private final DashboardDados dados;
    private final ObjectMapper mapper;
    private final CadastroColaboradorService cadastro;
    private final JdbcTemplate jdbc;
    public ColaboradorController(DashboardDados dados, ObjectMapper mapper, CadastroColaboradorService cadastro,JdbcTemplate jdbc) { this.dados = dados; this.mapper = mapper; this.cadastro=cadastro;this.jdbc=jdbc; }
    @PostMapping NovoColaboradorResposta cadastrar(@jakarta.validation.Valid @RequestBody NovoColaboradorPedido pedido, Authentication auth) {
        dados.exigirRh(auth); return cadastro.cadastrar(pedido);
    }
    @GetMapping List<JsonNode> listar(Authentication auth, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="100") int size) {
        dados.validarPaginacao(page, size); var pessoas = dados.lista("colaboradores");
        if ("GERENTE".equals(dados.perfil(auth))) pessoas.removeIf(p -> !dados.mesmaLoja(p, dados.lojaUsuario(auth)));
        return dados.pagina(pessoas, page, size);
    }
    @GetMapping("/{id}/linha-do-tempo") JsonNode linha(@PathVariable String id, Authentication auth) {
        JsonNode pessoa = dados.encontrar("colaboradores", id);
        if ("GERENTE".equals(dados.perfil(auth)) && !dados.mesmaLoja(pessoa, dados.lojaUsuario(auth))) throw dados.proibido();
        var eventos=mapper.createArrayNode();
        jdbc.query("SELECT e.tipo,e.criado_em FROM alerta_evento e JOIN alerta a ON a.id=e.alerta_id WHERE a.colaborador_id=? ORDER BY e.criado_em DESC",rs->{
            ObjectNode ev=mapper.createObjectNode();String tipo=rs.getString(1);ev.put("tipo",tipo);ev.put("criadoEm",rs.getTimestamp(2).toInstant().toString());
            ev.put("titulo",switch(tipo){case "LEMBRETE_24H"->"Lembrete enviado";case "RETORNO_RECEBIDO"->"Retorno registrado";case "ALERTA_ENVIADO"->"Alerta encaminhado";case "COLABORADOR_AVISADO"->"Aviso enviado";default->"Acompanhamento registrado";});
            ev.put("resumo",switch(tipo){case "LEMBRETE_24H"->"O alerta recebeu um lembrete após 24 horas sem retorno.";case "RETORNO_RECEBIDO"->"A liderança registrou um retorno.";default->"Acompanhamento registrado.";});eventos.add(ev);
        },id);
        jdbc.query("SELECT tipo,enviada_em FROM mensagem_saida WHERE colaborador_id=? AND status IN ('ENVIADA','ENTREGUE','LIDA') ORDER BY enviada_em DESC",rs->{ObjectNode ev=mapper.createObjectNode();ev.put("tipo",rs.getString(1));ev.put("titulo","Mensagem de acompanhamento");ev.put("criadoEm",rs.getTimestamp(2).toInstant().toString());ev.put("resumo","Mensagem da jornada enviada.");eventos.add(ev);},id);
        ObjectNode resposta = mapper.createObjectNode(); resposta.put("colaboradorId", id); resposta.set("eventos", eventos); return resposta;
    }
}
