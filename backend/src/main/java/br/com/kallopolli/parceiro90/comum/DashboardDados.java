package br.com.kallopolli.parceiro90.comum;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DashboardDados {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    public DashboardDados(JdbcTemplate jdbc, ObjectMapper mapper) { this.jdbc = jdbc; this.mapper = mapper; }

    public List<JsonNode> lista(String recurso) {
        return jdbc.query("SELECT conteudo::text FROM dashboard_dado WHERE recurso=? ORDER BY chave", (rs,n) -> ler(rs.getString(1)), recurso);
    }
    public JsonNode objeto(String recurso, String chave) {
        return jdbc.query("SELECT conteudo::text FROM dashboard_dado WHERE recurso=? AND chave=?", rs -> rs.next() ? ler(rs.getString(1)) : indisponivel(), recurso, chave);
    }
    public JsonNode encontrar(String recurso, String chave) {
        return jdbc.query("SELECT conteudo::text FROM dashboard_dado WHERE recurso=? AND chave=?", rs -> {
            if (!rs.next()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado.");
            return ler(rs.getString(1));
        }, recurso, chave);
    }
    public void salvar(String recurso, String chave, JsonNode valor) {
        try { jdbc.update("UPDATE dashboard_dado SET conteudo=?::jsonb WHERE recurso=? AND chave=?", mapper.writeValueAsString(valor), recurso, chave); }
        catch (Exception ex) { throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível salvar a alteração."); }
    }
    public String perfil(Authentication auth) { return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RH")) ? "RH" : "GERENTE"; }
    public long usuarioId(Authentication auth) { return jdbc.queryForObject("SELECT id FROM usuario WHERE lower(email)=lower(?)", Long.class, auth.getName()); }
    public String lojaUsuario(Authentication auth) { return jdbc.queryForObject("SELECT l.nome FROM usuario u JOIN loja l ON l.id=u.loja_id WHERE lower(u.email)=lower(?)", String.class, auth.getName()); }
    public boolean mesmaLoja(JsonNode pessoa, String loja) { return pessoa.path("loja").asText().equalsIgnoreCase(loja); }
    public boolean colaboradorNaLoja(String id, String loja) {
        try { return mesmaLoja(encontrar("colaboradores", id), loja); }
        catch (ResponseStatusException ex) { return false; }
    }
    public void exigirRh(Authentication auth) { if (!"RH".equals(perfil(auth))) throw proibido(); }
    public ResponseStatusException proibido() { return new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso não permitido para este perfil."); }
    public void validarPaginacao(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paginação inválida. Use page >= 0 e size entre 1 e 100.");
    }
    public <T> List<T> pagina(List<T> itens, int page, int size) {
        int inicio = (int) Math.min((long) page * size, itens.size());
        return itens.subList(inicio, Math.min(inicio + size, itens.size()));
    }
    private JsonNode ler(String json) {
        try { return mapper.readTree(json); }
        catch (Exception ex) { throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Dados indisponíveis."); }
    }
    private JsonNode indisponivel() { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Dados indisponíveis."); }
}
