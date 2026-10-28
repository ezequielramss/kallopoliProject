package br.com.kallopolli.parceiro90.config;

import br.com.kallopolli.parceiro90.seguranca.HandlerFalhaLogin;
import br.com.kallopolli.parceiro90.seguranca.HandlerSucessoLogin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SegurancaConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean SecurityFilterChain filtro(HttpSecurity http, JdbcTemplate jdbc) throws Exception {
        var csrf = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrf.setCookiePath("/");
        http.authorizeHttpRequests(a -> a
            .requestMatchers("/login.html", "/js/login.js", "/css/**", "/fonts/**", "/img/**", "/icons.svg", "/favicon.ico", "/csrf").permitAll()
            .requestMatchers("/api/**").authenticated()
            .requestMatchers("/actuator/health").authenticated()
            .anyRequest().permitAll())
          .csrf(c -> c.csrfTokenRepository(csrf).csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
          .formLogin(f -> f.loginPage("/login.html").loginProcessingUrl("/login")
              .failureHandler(new HandlerFalhaLogin(jdbc)).successHandler(new HandlerSucessoLogin(jdbc)).permitAll())
          .logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/login.html").invalidateHttpSession(true).deleteCookies("JSESSIONID"))
          .exceptionHandling(e -> e.authenticationEntryPoint((req,res,ex) -> {
              if (req.getRequestURI().startsWith("/api/")) { res.setStatus(401); res.setContentType("application/problem+json"); res.getWriter().write("{\"type\":\"about:blank\",\"title\":\"Autenticação necessária\",\"status\":401,\"detail\":\"Entre novamente para continuar.\",\"instance\":\"" + req.getRequestURI() + "\"}"); }
              else res.sendRedirect("/login.html");
          }).accessDeniedHandler((req,res,ex) -> {
              res.setStatus(403); res.setContentType("application/problem+json");
              res.getWriter().write("{\"type\":\"about:blank\",\"title\":\"Acesso não permitido\",\"status\":403,\"detail\":\"A solicitação não foi autorizada.\",\"instance\":\"" + req.getRequestURI() + "\"}");
          }))
          .headers(h -> h.contentSecurityPolicy(c -> c.policyDirectives("default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; font-src 'self'; connect-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'none'")).frameOptions(f -> f.deny()).contentTypeOptions(c -> {}).referrerPolicy(r -> r.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)));
        return http.build();
    }
}
