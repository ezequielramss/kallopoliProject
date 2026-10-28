package br.com.kallopolli.parceiro90.colaborador;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class HashTelefone {
    private HashTelefone() { }
    public static String gerar(String telefone) {
        return gerar(telefone, System.getenv("APP_HASH_KEY"));
    }
    public static String gerar(String telefone, String key) {
        if (telefone == null || telefone.isBlank()) return null;
        try {
            if (key == null || key.isBlank()) throw new IllegalStateException("APP_HASH_KEY não configurada.");
            String normalizado = telefone.replaceAll("\\D", "");
            Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(normalizado.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(bytes);
        } catch (IllegalStateException ex) { throw ex; }
        catch (Exception ex) { throw new IllegalStateException("Não foi possível proteger a busca de telefone."); }
    }
}
