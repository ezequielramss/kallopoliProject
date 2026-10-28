package br.com.kallopolli.parceiro90.kpi;

import br.com.kallopolli.parceiro90.comum.DashboardDados;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kpis")
public class KpiController {
    private final DashboardDados dados;
    public KpiController(DashboardDados dados) { this.dados = dados; }
    @GetMapping JsonNode kpis() { return dados.objeto("kpis", "principal"); }
    @GetMapping("/serie-mensal") JsonNode serie() { return dados.objeto("serie-mensal", "principal"); }
}
