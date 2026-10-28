package br.com.kallopolli.parceiro90.seguranca;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

public class HandlerFalhaLogin implements AuthenticationFailureHandler {
    private final JdbcTemplate jdbc;
    public HandlerFalhaLogin(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Override public void onAuthenticationFailure(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex)
            throws IOException, ServletException {
        String email = req.getParameter("username");
        if (email != null && email.length() <= 254) jdbc.update("UPDATE usuario SET falhas_login=falhas_login+1, bloqueado_ate=CASE WHEN falhas_login+1 >= 5 THEN now()+interval '15 minutes' ELSE bloqueado_ate END WHERE lower(email)=lower(?)", email);
        res.sendRedirect("/login.html?erro=1");
    }
}
