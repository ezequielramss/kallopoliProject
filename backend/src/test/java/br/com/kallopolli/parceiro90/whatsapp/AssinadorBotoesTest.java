package br.com.kallopolli.parceiro90.whatsapp;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AssinadorBotoesTest {
    @Test void rejeitaPayloadAdulterado(){var signer=new AssinadorBotoes("chave-hmac-de-teste-com-mais-de-trinta-dois-caracteres");String token=signer.assinar("alerta-1","CONVERSEI");assertEquals("alerta-1",signer.validar(token).alertaId());byte[] raw=java.util.Base64.getUrlDecoder().decode(token);raw[0]^=1;assertNull(signer.validar(java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(raw)));}
}
