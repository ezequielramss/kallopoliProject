package br.com.kallopolli.parceiro90.seguranca;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

public class HandlerSucessoLogin implements AuthenticationSuccessHandler {
    private final JdbcTemplate jdbc;
    public HandlerSucessoLogin(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Override public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth)
            throws IOException, ServletException {
        jdbc.update("UPDATE usuario SET falhas_login=0,bloqueado_ate=NULL WHERE lower(email)=lower(?)", auth.getName());
        res.sendRedirect("/");
    }
}
