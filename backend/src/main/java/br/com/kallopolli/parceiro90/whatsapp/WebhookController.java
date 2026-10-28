package br.com.kallopolli.parceiro90.whatsapp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook/whatsapp")
public class WebhookController {
    private final ProcessadorWebhook processador;
    private final String verifyToken;
    private final String appSecret;
    public WebhookController(ProcessadorWebhook processador,
            @Value("${app.whatsapp.verify-token:}") String verifyToken,
            @Value("${app.whatsapp.app-secret:}") String appSecret) {
        this.processador = processador; this.verifyToken = verifyToken; this.appSecret = appSecret;
    }
    @GetMapping String verificar(@RequestParam(name="hub.mode", required=false) String mode,
            @RequestParam(name="hub.verify_token", required=false) String token,
            @RequestParam(name="hub.challenge", required=false) String challenge) {
        if (!"subscribe".equals(mode) || !igual(token, verifyToken) || challenge == null) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Verificação do webhook recusada.");
        return challenge;
    }
    @PostMapping ResponseEntity<Map<String, String>> receber(@RequestBody byte[] corpoBruto,
            @RequestHeader(name="X-Hub-Signature-256", required=false) String assinatura) {
        if (!AssinaturaWebhook.validar(corpoBruto, assinatura, appSecret)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        processador.enfileirar(corpoBruto.clone());
        return ResponseEntity.ok(Map.of("status", "recebido"));
    }
    private boolean igual(String recebido, String esperado) {
        if (recebido == null || esperado == null || esperado.isBlank()) return false;
        return MessageDigest.isEqual(recebido.getBytes(StandardCharsets.UTF_8), esperado.getBytes(StandardCharsets.UTF_8));
    }
}
