package br.com.kallopolli.parceiro90.whatsapp;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AssinaturaWebhookTest {
    private final byte[] corpo="{\"entry\":[]}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    @Test void aceitaAssinaturaDoCorpoBruto(){String sig=AssinaturaWebhook.calcular(corpo,"segredo-app");assertTrue(AssinaturaWebhook.validar(corpo,sig,"segredo-app"));}
    @Test void rejeitaAlteracaoEAusencia(){String sig=AssinaturaWebhook.calcular(corpo,"segredo-app");assertFalse(AssinaturaWebhook.validar("{}".getBytes(),sig,"segredo-app"));assertFalse(AssinaturaWebhook.validar(corpo,null,"segredo-app"));}
}
