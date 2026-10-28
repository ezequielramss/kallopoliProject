package br.com.kallopolli.parceiro90.whatsapp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class AssinaturaWebhook {
    private AssinaturaWebhook() { }
    public static boolean validar(byte[] corpoBruto, String assinatura, String segredo) {
        if (corpoBruto == null || assinatura == null || segredo == null || segredo.isBlank() || !assinatura.matches("sha256=[0-9a-fA-F]{64}")) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] esperado = ("sha256=" + java.util.HexFormat.of().formatHex(mac.doFinal(corpoBruto))).getBytes(StandardCharsets.US_ASCII);
            return MessageDigest.isEqual(esperado, assinatura.getBytes(StandardCharsets.US_ASCII));
        } catch (Exception ex) { return false; }
    }
    public static String calcular(byte[] corpoBruto, String segredo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return "sha256=" + java.util.HexFormat.of().formatHex(mac.doFinal(corpoBruto));
        } catch (Exception ex) { throw new IllegalStateException("Não foi possível verificar a assinatura."); }
    }
}
