# Decisões de produto e privacidade

- O assistente não faz diagnóstico nem substitui conversa humana. A participação é voluntária; a interface fala em acompanhamento e sinais, sem linguagem de vigilância.
- Os níveis de atenção são explicáveis e sempre acompanhados do motivo. Não existe pontuação percentual de risco por pessoa.
- Assédio, humilhação e conflito com chefia são encaminhados somente a RH/direção. Esses alertas não exibem nome de colaborador.
- Relatos confidenciais mostram loja e turno somente no detalhe, nunca na lista. A comunicação usa “confidencial”, sem promessa de anonimato absoluto.
- O dashboard não oferece botão para iniciar um relato. O canal de relato é separado.
- Bem-estar exibirá categoria e resumo neutro, nunca a mensagem literal. O texto de opt-in do canal deverá informar esse uso antes da implementação da Fase 2.
- O uso do logo McDonald’s e da fonte Speedee foi indicado como autorizado pela Kallopolli no briefing; os arquivos locais são usados apenas neste protótipo.
- A pasta `referencia/` não estava presente no workspace. Os estilos, ícones e conteúdo disponíveis em `src/` foram usados como referência; a comparação visual com `referencia/dist/index.html` fica pendente.
- O painel de alertas é somente leitura: o retorno vem do WhatsApp e não existe ação manual para marcar caso como resolvido.

## Fase 2

- No Bem-estar, o RH pode ver resumos neutros das conversas; o perfil gerente vê somente a categoria e a sugestão de acompanhamento. O opt-in deve explicar essa diferença antes da coleta.
- Loja e turno aparecem apenas no detalhe de um relato confidencial, com aviso de que essas informações podem permitir inferir a identidade de quem relatou.
- Nenhuma tela usa diagnóstico, pontuação individual de risco ou linguagem de vigilância.
- A identidade de marca e a fonte Speedee seguem a autorização indicada pela Kallopolli.
- Relatórios assistidos por IA mostram o aviso “Gerado por IA — revise antes de compartilhar.”; nomes individuais são omitidos para o público Direção.
- O PATCH de relato persiste o status e a observação interna, acrescenta histórico e grava auditoria pelo backend.

## Backend Fase 3A

- O `GET /api/relatos` devolve apenas campos de resumo. O mock anterior misturava resumo e detalhe no mesmo array; foi criado `relatos-lista.json` e `relatos.json` permanece como fixture de detalhe.
- Em alertas `ASSEDIO` e `CONFLITO_CHEFIA`, o backend remove `colaboradorId`, nome e iniciais da resposta. O mock foi alinhado a essa regra de privacidade.
- O CSRF usa `CookieCsrfTokenRepository`: o endpoint público `GET /csrf` inicializa o cookie `XSRF-TOKEN`; chamadas de alteração enviam o cookie no cabeçalho `X-XSRF-TOKEN`. A sessão usa `JSESSIONID` HttpOnly, SameSite=Lax e expiração por inatividade de 30 minutos; produção ativa Secure.
- Os dados demonstrativos são copiados idempotentemente dos JSONs para o PostgreSQL no perfil `dev`; o contrato da API lê do banco. O relatório permanece fixture e é devolvido por stub enquanto `app.ia.enabled=false`.