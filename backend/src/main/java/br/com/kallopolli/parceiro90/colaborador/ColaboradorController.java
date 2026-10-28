package br.com.kallopolli.parceiro90.colaborador;

import br.com.kallopolli.parceiro90.comum.DashboardDados;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/colaboradores")
public class ColaboradorController {
    private final DashboardDados dados;
    private final ObjectMapper mapper;
    public ColaboradorController(DashboardDados dados, ObjectMapper mapper) { this.dados = dados; this.mapper = mapper; }
    @GetMapping List<JsonNode> listar(Authentication auth, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="100") int size) {
        dados.validarPaginacao(page, size); var pessoas = dados.lista("colaboradores");
        if ("GERENTE".equals(dados.perfil(auth))) pessoas.removeIf(p -> !dados.mesmaLoja(p, dados.lojaUsuario(auth)));
        return dados.pagina(pessoas, page, size);
    }
    @GetMapping("/{id}/linha-do-tempo") JsonNode linha(@PathVariable String id, Authentication auth) {
        JsonNode pessoa = dados.encontrar("colaboradores", id);
        if ("GERENTE".equals(dados.perfil(auth)) && !dados.mesmaLoja(pessoa, dados.lojaUsuario(auth))) throw dados.proibido();
        ObjectNode evento = mapper.createObjectNode(); evento.put("tipo", "CHECKIN"); evento.put("titulo", "Check-in de acompanhamento");
        evento.put("criadoEm", pessoa.path("ultimoContato").asText()); evento.put("resumo", "Acompanhamento registrado.");
        ObjectNode resposta = mapper.createObjectNode(); resposta.put("colaboradorId", id); resposta.set("eventos", mapper.createArrayNode().add(evento)); return resposta;
    }
}
