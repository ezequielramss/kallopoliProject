package br.com.kallopolli.parceiro90.whatsapp;

import br.com.kallopolli.parceiro90.colaborador.HashTelefoneService;
import br.com.kallopolli.parceiro90.comum.CifradorDados;
import br.com.kallopolli.parceiro90.comum.DashboardDados;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController @RequestMapping("/api/lideres")
public class LiderController {
    private final DashboardDados dados; private final JdbcTemplate jdbc; private final HashTelefoneService hashes; private final CifradorDados cifrador;
    public LiderController(DashboardDados dados,JdbcTemplate jdbc,HashTelefoneService hashes,CifradorDados cifrador){this.dados=dados;this.jdbc=jdbc;this.hashes=hashes;this.cifrador=cifrador;}
    @PostMapping public Resposta criar(@Valid @RequestBody CadastroLiderPedido pedido, Authentication auth){
        dados.exigirRh(auth); String perfil=pedido.perfil().toUpperCase(java.util.Locale.ROOT);
        if(!perfil.equals("RH")&&!perfil.equals("GERENTE"))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Perfil inválido.");
        if((perfil.equals("RH")&&pedido.lojaId()!=null)||(perfil.equals("GERENTE")&&pedido.lojaId()==null))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Loja incompatível com o perfil.");
        if(pedido.lojaId()!=null&&jdbc.queryForObject("SELECT count(*) FROM loja WHERE id=? AND ativa=true",Integer.class,pedido.lojaId())==0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Loja não encontrada.");
        String hash=hashes.gerar(pedido.telefone()); if(jdbc.queryForObject("SELECT count(*) FROM lider_contato WHERE telefone_hash=?",Integer.class,hash)>0)throw new ResponseStatusException(HttpStatus.CONFLICT,"Telefone já cadastrado.");
        UUID id=UUID.randomUUID(); jdbc.update("INSERT INTO lider_contato(id,nome,perfil,loja_id,telefone_cifrado,telefone_hash) VALUES (?,?,?,?,?,?)",id,pedido.nome().trim(),perfil,pedido.lojaId(),cifrador.cifrar(pedido.telefone()),hash);
        return new Resposta(id,pedido.nome().trim(),perfil,pedido.lojaId(),mascarar(pedido.telefone()));
    }
    private String mascarar(String tel){String d=tel.replaceAll("\\D","");return "+"+"•".repeat(Math.max(0,d.length()-4))+d.substring(Math.max(0,d.length()-4));}
    public record Resposta(UUID id,String nome,String perfil,Long lojaId,String telefoneMascarado){}
}
