# Templates do WhatsApp (pt_BR)

Submeta os seis modelos abaixo como templates de utilidade na conta WhatsApp Business. A Meta pode reclassificar ou solicitar ajustes. Use botões de resposta rápida com os payloads enviados pelo backend; não coloque texto literal de colaboradores em variáveis.

## `boas_vindas_optin`

- Idioma: `pt_BR`; categoria sugerida: utilidade.
- Texto: “Olá! Este é o Parceiro dos 90 dias, um acompanhamento voluntário da sua chegada. Você pode parar quando quiser enviando SAIR. Sua participação e suas respostas são compartilhadas com a liderança responsável: a gerência recebe a categoria e uma sugestão de acompanhamento; o RH pode receber um resumo. Relatos de assédio ou conflito com chefia são encaminhados somente ao RH. Você será avisado sempre que um alerta for encaminhado. Quer participar?”
- Variáveis: nenhuma.
- Botões: “Quero participar” (`OPTIN:{colaboradorId}:ACEITAR`) e “Agora não” (`OPTIN:{colaboradorId}:RECUSAR`). O ID é associado ao telefone cadastrado e validado no recebimento.

## `alerta_lider`

- Idioma: `pt_BR`; categoria sugerida: utilidade.
- Texto: “Alerta sobre {{1}} ({{2}}): {{3}}. Sugestão: {{4}}. Protocolo {{5}}. Não responda com informações pessoais neste canal.”
- Variáveis, nesta ordem: iniciais aprovadas pela Kallopolli, loja, categoria em linguagem acolhedora, sugestão e protocolo. Nunca inclui a mensagem original.
- Botões: “Conversei” e “Não consegui”. O payload contém o ID do alerta e a ação assinados por HMAC.

## `lembrete_lider`

- Idioma: `pt_BR`; categoria sugerida: utilidade.
- Texto: “Lembrete: há um alerta de acompanhamento sem retorno há 24 horas. Protocolo {{1}}. Quando puder, registre o retorno.”
- Variáveis: protocolo.
- Botões: “Conversei” e “Não consegui”, com payload assinado.

## `alerta_rh`

- Idioma: `pt_BR`; categoria sugerida: utilidade.
- Texto: “Alerta de acompanhamento encaminhado ao RH. Protocolo {{1}}. Categoria: {{2}}. Sugestão: {{3}}. Trate estas informações com confidencialidade.”
- Variáveis: protocolo, categoria acolhedora e sugestão. Para `ASSEDIO` e `CONFLITO_CHEFIA`, não inclua iniciais, nome, loja ou texto literal; use a categoria “relato reservado” e uma sugestão genérica. O protocolo é o único identificador.
- Botões: “Conversei” e “Não consegui”, com payload assinado.

## `aviso_colaborador_alerta`

- Idioma: `pt_BR`; categoria sugerida: utilidade.
- Texto: “Recebemos sua mensagem e encaminhamos um alerta para {{1}}. O acompanhamento é confidencial. Protocolo {{2}}. Se preferir, você pode encerrar a participação enviando SAIR.”
- Variáveis: “RH” ou “sua gerência”, protocolo.
- Botões: nenhum.

## `retomada_conversa`

- Idioma: `pt_BR`; categoria sugerida: utilidade.
- Texto: “Oi! Passando para saber como você está. Quer responder por aqui?”
- Variáveis: nenhuma.
- Botões: nenhum.

Os nomes, idiomas, quantidade de variáveis e índices dos botões precisam coincidir exatamente entre o template aprovado e a configuração da conta. Os textos acima ainda precisam da revisão de privacidade/RH e aprovação da Meta antes de produção.
