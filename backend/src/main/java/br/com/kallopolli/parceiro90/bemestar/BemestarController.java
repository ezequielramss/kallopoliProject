package br.com.kallopolli.parceiro90.bemestar;

import br.com.kallopolli.parceiro90.comum.DashboardDados;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bem-estar")
public class BemestarController {
    private final DashboardDados dados;
    public BemestarController(DashboardDados dados) { this.dados = dados; }
    @GetMapping JsonNode listar(Authentication auth) {
        return dados.objeto("GERENTE".equals(dados.perfil(auth)) ? "bemestar-gerente" : "bemestar", "principal");
    }
}
