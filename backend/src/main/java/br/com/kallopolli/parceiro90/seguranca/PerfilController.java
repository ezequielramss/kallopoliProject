package br.com.kallopolli.parceiro90.seguranca;

import br.com.kallopolli.parceiro90.comum.DashboardDados;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {
    private final com.fasterxml.jackson.databind.ObjectMapper mapper;
    private final DashboardDados dados;
    public PerfilController(com.fasterxml.jackson.databind.ObjectMapper mapper, DashboardDados dados) { this.mapper = mapper; this.dados = dados; }
    @GetMapping ObjectNode perfil(Authentication auth) { ObjectNode perfil = mapper.createObjectNode(); perfil.put("perfil", dados.perfil(auth)); return perfil; }
}
