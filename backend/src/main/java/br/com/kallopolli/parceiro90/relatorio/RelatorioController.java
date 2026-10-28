package br.com.kallopolli.parceiro90.relatorio;

import br.com.kallopolli.parceiro90.comum.DashboardDados;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {
    private final DashboardDados dados;
    private final ObjectMapper mapper;
    public RelatorioController(DashboardDados dados, ObjectMapper mapper) { this.dados = dados; this.mapper = mapper; }
    @PostMapping("/gerar") JsonNode gerar(@Valid @RequestBody PedidoRelatorio pedido, Authentication auth) {
        dados.exigirRh(auth);
        if (!List.of("EQUIPE", "FUNCIONARIO").contains(pedido.escopo()) || !List.of("RH", "GERENTE", "DIRECAO").contains(pedido.publico()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escopo ou público inválido.");
        JsonNode report = dados.objeto("relatorio-exemplo", "principal");
        if ("DIRECAO".equals(pedido.publico()) && pedido.colaboradorId() != null) {
            String nome = dados.encontrar("colaboradores", pedido.colaboradorId()).path("nome").asText(); ObjectNode semNome = ((ObjectNode) report).deepCopy();
            semNome.fields().forEachRemaining(field -> { if (field.getValue().isTextual()) field.setValue(mapper.getNodeFactory().textNode(field.getValue().asText().replace(nome, "uma pessoa da equipe"))); });
            return semNome;
        }
        return report;
    }
}
