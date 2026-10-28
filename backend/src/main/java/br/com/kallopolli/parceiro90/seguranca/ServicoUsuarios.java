package br.com.kallopolli.parceiro90.seguranca;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ServicoUsuarios implements UserDetailsService {
    private final JdbcTemplate jdbc;
    public ServicoUsuarios(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public UserDetails loadUserByUsername(String username) {
        var usuarios = jdbc.query("SELECT id,email,senha_hash,perfil,loja_id,bloqueado_ate FROM usuario WHERE lower(email)=lower(?)",
            (rs, n) -> new UsuarioAutenticado(rs.getLong("id"), rs.getString("email"), rs.getString("perfil"),
                (Long) rs.getObject("loja_id")), username);
        if (usuarios.isEmpty()) throw new UsernameNotFoundException("Credenciais inválidas");
        var perfil = usuarios.get(0);
        var credenciais = jdbc.queryForMap("SELECT senha_hash, bloqueado_ate FROM usuario WHERE id=?", perfil.id());
        var bloqueio = (java.time.OffsetDateTime) credenciais.get("bloqueado_ate");
        boolean bloqueado = bloqueio != null && bloqueio.isAfter(java.time.OffsetDateTime.now());
        return User.withUsername(perfil.email()).password((String) credenciais.get("senha_hash"))
            .authorities("ROLE_" + perfil.perfil()).accountLocked(bloqueado).build();
    }
}
