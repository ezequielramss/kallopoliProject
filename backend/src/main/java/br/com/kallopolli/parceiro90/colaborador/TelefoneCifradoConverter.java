package br.com.kallopolli.parceiro90.colaborador;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Converter
public class TelefoneCifradoConverter implements AttributeConverter<String, String> {
    private static final SecureRandom RANDOM = new SecureRandom();
    @Override public String convertToDatabaseColumn(String telefone) {
        if (telefone == null || telefone.isBlank()) return null;
        try {
            byte[] iv = new byte[12]; RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(telefone.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array());
        } catch (Exception ex) { throw new IllegalStateException("Não foi possível proteger o telefone."); }
    }
    @Override public String convertToEntityAttribute(String stored) {
        if (stored == null || stored.isBlank()) return null;
        try {
            byte[] data = Base64.getDecoder().decode(stored); ByteBuffer buffer = ByteBuffer.wrap(data);
            byte[] iv = new byte[12]; buffer.get(iv); byte[] encrypted = new byte[buffer.remaining()]; buffer.get(encrypted);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception ex) { throw new IllegalStateException("Não foi possível ler o telefone protegido."); }
    }
    private SecretKeySpec key() throws Exception {
        String secret = System.getenv("APP_CRYPTO_KEY");
        if (secret == null || secret.isBlank()) throw new IllegalStateException("APP_CRYPTO_KEY não configurada.");
        return new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8)), "AES");
    }
}
