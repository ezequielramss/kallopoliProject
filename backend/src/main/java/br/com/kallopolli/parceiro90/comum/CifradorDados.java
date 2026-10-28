package br.com.kallopolli.parceiro90.comum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CifradorDados {
    private final String chave;
    public CifradorDados(@Value("${APP_CRYPTO_KEY:}") String chave) { this.chave = chave; }
    public String cifrar(String texto) { return CifradorAesGcm.cifrar(texto, chave); }
    public String decifrar(String texto) { return CifradorAesGcm.decifrar(texto, chave); }
}
