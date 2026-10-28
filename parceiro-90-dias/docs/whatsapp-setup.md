# Configurar o WhatsApp Cloud API

## Desenvolvimento local

1. Rode PostgreSQL e o backend no perfil `dev`; esse perfil usa `FakeWhatsAppClient` e grava o conteúdo cifrado em `outbox_dev`.
2. Configure `APP_CRYPTO_KEY` e `APP_HASH_KEY` com segredos diferentes de pelo menos 32 caracteres. Configure o banco no `.env`.
3. Cadastre colaboradores pelo `POST /api/colaboradores` com uma sessão RH e líderes pelo `POST /api/lideres`. Telefone usa E.164 (`+55...`). O cadastro exige `consentimentoEm` e data de admissão.
4. Simule uma mensagem recebida pelo endpoint local `POST /dev/whatsapp/entrada`, corpo `{"telefone":"+5511999999999","texto":"Quero conversar"}`. O endpoint só existe nos perfis `dev` e `test`; não use fora deles.
5. Simule o retorno do líder em `POST /dev/whatsapp/retorno` com `{"alertaId":"...","liderId":"...","acao":"CONVERSEI"}` ou `NAO_CONSEGUI`. O endpoint assina o payload em nome do contato Fake e exercita o mesmo handler.
6. `APP_TEMPO_FATOR` acelera o relógio de domínio. Para uma simulação reprodutível, use `APP_TEMPO_INICIO` em ISO-8601 UTC e um fator maior que 1. O agendador continua executando no intervalo configurado por `APP_CADENCIA_INTERVALO_MS`.

## Configuração Meta para homologação e produção

1. Crie um app Meta com o produto WhatsApp, vincule uma WhatsApp Business Account e um número dedicado. Registre o Phone Number ID e gere um token de acesso de sistema com as permissões necessárias para mensagens e webhooks.
2. Configure o callback `https://SEU_HOST/webhook/whatsapp`, um `WHATSAPP_VERIFY_TOKEN` forte e exclusivo, e o App Secret do app. Inscreva o app no campo de mensagens do webhook.
3. Para desenvolvimento externo, exponha a porta do backend por um túnel HTTPS temporário e configure o callback público do túnel. Não exponha credenciais nem o banco pelo túnel.
4. Copie os nomes aprovados da conta para `docs/templates-whatsapp.md`. Submeta e aguarde a aprovação dos templates antes de usar homologação real.
5. Configure `WHATSAPP_TOKEN`, `WHATSAPP_PHONE_NUMBER_ID`, `WHATSAPP_APP_SECRET`, `WHATSAPP_VERIFY_TOKEN`, `WHATSAPP_GRAPH_VERSION`, `APP_CRYPTO_KEY`, `APP_HASH_KEY` e as credenciais PostgreSQL no ambiente do serviço. Não coloque valores reais no repositório.
6. Inicie com `SPRING_PROFILES_ACTIVE=homolog` ou `prod`. A aplicação falha ao iniciar se faltar configuração obrigatória. O webhook valida a assinatura HMAC-SHA256 do corpo bruto e rejeita assinatura ausente ou inválida.

A integração usa a WhatsApp Cloud API Graph configurável por `app.whatsapp.graph-version`. Botões rápidos usam `button_reply.id` ou `button.payload` no callback; os dois formatos são processados. Fora da janela de atendimento de 24 horas, o backend bloqueia texto livre e usa somente um template configurado.
