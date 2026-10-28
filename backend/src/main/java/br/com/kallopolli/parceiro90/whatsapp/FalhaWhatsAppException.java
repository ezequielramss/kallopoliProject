package br.com.kallopolli.parceiro90.whatsapp;

public class FalhaWhatsAppException extends RuntimeException {
    private final int statusHttp;
    public FalhaWhatsAppException(int statusHttp) { super("Falha no envio pelo provedor WhatsApp."); this.statusHttp = statusHttp; }
    public int statusHttp() { return statusHttp; }
    public boolean permiteRetry() { return statusHttp == 429 || statusHttp >= 500; }
    public String codigoSeguro() { return statusHttp == 429 ? "RATE_LIMIT" : statusHttp >= 500 ? "PROVEDOR_INDISPONIVEL" : "ENVIO_RECUSADO"; }
}
