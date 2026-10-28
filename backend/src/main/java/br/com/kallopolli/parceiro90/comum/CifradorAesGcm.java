package br.com.kallopolli.parceiro90.comum;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class CifradorAesGcm {
    private static final SecureRandom RANDOM = new SecureRandom();
    private CifradorAesGcm() { }
    public static String cifrar(String valor, String segredo) {
        if (valor == null) return null;
        try {
            byte[] iv = new byte[12]; RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE, chave(segredo), new GCMParameterSpec(128, iv));
            byte[] conteudo = cipher.doFinal(valor.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.length + conteudo.length).put(iv).put(conteudo).array());
        } catch (Exception ex) { throw new IllegalStateException("Não foi possível proteger o dado."); }
    }
    public static String decifrar(String valor, String segredo) {
        if (valor == null) return null;
        try {
            ByteBuffer bytes = ByteBuffer.wrap(Base64.getDecoder().decode(valor)); byte[] iv = new byte[12]; bytes.get(iv);
            byte[] conteudo = new byte[bytes.remaining()]; bytes.get(conteudo);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.DECRYPT_MODE, chave(segredo), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(conteudo), StandardCharsets.UTF_8);
        } catch (Exception ex) { throw new IllegalStateException("Não foi possível ler o dado protegido."); }
    }
    private static SecretKeySpec chave(String segredo) throws Exception {
        if (segredo == null || segredo.length() < 32 || segredo.startsWith("SUBSTITUA")) throw new IllegalStateException("APP_CRYPTO_KEY ausente ou inválida.");
        return new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(segredo.getBytes(StandardCharsets.UTF_8)), "AES");
    }
}
