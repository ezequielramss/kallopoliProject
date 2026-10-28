package br.com.kallopolli.parceiro90.colaborador;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import br.com.kallopolli.parceiro90.comum.CifradorAesGcm;

@Converter
public class TelefoneCifradoConverter implements AttributeConverter<String, String> {
    @Override public String convertToDatabaseColumn(String telefone) {
        if (telefone == null || telefone.isBlank()) return null;
        return CifradorAesGcm.cifrar(telefone, System.getenv("APP_CRYPTO_KEY"));
    }
    @Override public String convertToEntityAttribute(String stored) {
        if (stored == null || stored.isBlank()) return null;
        return CifradorAesGcm.decifrar(stored, System.getenv("APP_CRYPTO_KEY"));
    }
}
