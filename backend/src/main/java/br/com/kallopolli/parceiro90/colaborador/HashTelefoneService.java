package br.com.kallopolli.parceiro90.colaborador;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HashTelefoneService {
    private final String chave;
    public HashTelefoneService(@Value("${APP_HASH_KEY:}") String chave) { this.chave = chave; }
    public String gerar(String telefone) { return HashTelefone.gerar(telefone, chave); }
}
