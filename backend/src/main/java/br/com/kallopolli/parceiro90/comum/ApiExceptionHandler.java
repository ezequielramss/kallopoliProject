package br.com.kallopolli.parceiro90.comum;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ProblemDetail> status(ResponseStatusException ex, HttpServletRequest req) {
        var p = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getReason() == null ? "Não foi possível concluir a solicitação." : ex.getReason());
        p.setTitle(ex.getStatusCode().is4xxClientError() ? "Solicitação inválida" : "Erro interno"); p.setInstance(URI.create(req.getRequestURI()));
        return ResponseEntity.status(ex.getStatusCode()).body(p);
    }
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ProblemDetail> entrada(Exception ex, HttpServletRequest req) {
        var p = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Verifique os campos enviados.");
        p.setTitle("Dados inválidos"); p.setInstance(URI.create(req.getRequestURI()));
        return ResponseEntity.badRequest().body(p);
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> interno(Exception ex, HttpServletRequest req) {
        var p = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível concluir a solicitação.");
        p.setTitle("Erro interno"); p.setInstance(URI.create(req.getRequestURI()));
        return ResponseEntity.internalServerError().body(p);
    }
}
