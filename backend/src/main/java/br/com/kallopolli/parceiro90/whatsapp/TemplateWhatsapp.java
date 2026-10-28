package br.com.kallopolli.parceiro90.whatsapp;

public enum TemplateWhatsapp {
    BOAS_VINDAS_OPTIN("boas_vindas_optin"), ALERTA_LIDER("alerta_lider"), LEMBRETE_LIDER("lembrete_lider"),
    ALERTA_RH("alerta_rh"), AVISO_COLABORADOR_ALERTA("aviso_colaborador_alerta"), RETOMADA_CONVERSA("retomada_conversa");
    private final String nome;
    TemplateWhatsapp(String nome) { this.nome = nome; }
    public String nome() { return nome; }
}
