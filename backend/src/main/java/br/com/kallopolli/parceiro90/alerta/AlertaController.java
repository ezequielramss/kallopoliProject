package br.com.kallopolli.parceiro90.alerta;

import br.com.kallopolli.parceiro90.comum.DashboardDados;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {
    private final DashboardDados dados;
    public AlertaController(DashboardDados dados) { this.dados = dados; }
    @GetMapping List<JsonNode> listar(Authentication auth, @RequestParam(required=false) String status, @RequestParam(required=false) String destino,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="100") int size) {
        dados.validarPaginacao(page, size); String perfil = dados.perfil(auth); String loja = "GERENTE".equals(perfil) ? dados.lojaUsuario(auth) : null;
        var resultado = new ArrayList<JsonNode>();
        for (JsonNode origem : dados.lista("alertas")) {
            String categoria = origem.path("categoria").asText(); String destinoFinal = RoteamentoAlerta.destino(categoria);
            if (destino != null && !destino.isBlank() && !destino.equals(destinoFinal)) continue;
            if ("GERENTE".equals(perfil) && (!"GERENTE".equals(destinoFinal) || !dados.colaboradorNaLoja(origem.path("colaboradorId").asText(), loja))) continue;
            ObjectNode alerta = ((ObjectNode) origem).deepCopy();
            String calculado = StatusAlerta.calcular(alerta.path("status").asText(), OffsetDateTime.parse(alerta.path("criadoEm").asText()),
                alerta.path("retorno").isNull() ? null : alerta.path("retorno").asText(), OffsetDateTime.now());
            alerta.put("status", calculado);
            if (status != null && !status.isBlank() && !status.equals(calculado)) continue;
            RedacaoAlerta.aplicar(alerta); resultado.add(alerta);
        }
        return dados.pagina(resultado, page, size);
    }
}
