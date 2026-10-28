package br.com.kallopolli.parceiro90.whatsapp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AssinadorBotoes {
    private final String chave;
    public AssinadorBotoes(@Value("${APP_HASH_KEY:}") String chave) { this.chave = chave; }
    public String assinar(String id, String acao) {
        try {
            String corpo = id + ":" + acao;
            return Base64.getUrlEncoder().withoutPadding().encodeToString((corpo + ":" + hmac(corpo)).getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) { throw new IllegalStateException("Não foi possível assinar o botão."); }
    }
    public AcaoBotao validar(String payload) {
        try {
            String[] partes = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8).split(":", 3);
            if (partes.length != 3 || !MessageDigest.isEqual(hmac(partes[0] + ":" + partes[1]).getBytes(StandardCharsets.US_ASCII), partes[2].getBytes(StandardCharsets.US_ASCII))) return null;
            if (!partes[1].equals("CONVERSEI") && !partes[1].equals("NAO_CONSEGUI")) return null;
            return new AcaoBotao(partes[0], partes[1]);
        } catch (Exception ex) { return null; }
    }
    private String hmac(String valor) throws Exception {
        if (chave == null || chave.length() < 32 || chave.startsWith("SUBSTITUA")) throw new IllegalStateException("APP_HASH_KEY ausente ou inválida.");
        Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(chave.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return java.util.HexFormat.of().formatHex(mac.doFinal(valor.getBytes(StandardCharsets.UTF_8)));
    }
}
