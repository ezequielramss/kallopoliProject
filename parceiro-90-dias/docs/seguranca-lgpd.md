# Segurança e privacidade — Parceiro dos 90 dias

Este documento descreve as proteções técnicas das Fases 3A e 3B. A base legal aplicável, os textos de consentimento e os prazos devem ser definidos e aprovados pelo RH e pelas áreas responsáveis antes de uso com dados reais.

## Dados tratados

- Conta de acesso: e-mail, hash BCrypt da senha, perfil, loja do gerente, contador de falhas e bloqueio temporário.
- Colaborador: identificador interno, nome, iniciais, função e loja. O modelo reserva campos para telefone cifrado com AES-GCM e hash HMAC-SHA256; não armazene telefone em claro.
- Alerta: categoria, severidade, destino, protocolo, status, retorno, timestamps e sugestão de acompanhamento.
- Relato confidencial: protocolo, categoria, severidade, status, loja, turno, título e conteúdo. O relato não contém telefone ou identificador de colaborador.
- Bem-estar: categoria, resumo neutro, data e tag; o resumo só é acessível ao RH.
- Auditoria: conta de acesso, leitura/alteração, protocolo e data/hora. Não copia conteúdo do relato.
- WhatsApp: telefone do colaborador e de contatos de RH/gerência cifrados com AES-GCM; HMAC-SHA256 permite localizar o destinatário sem guardar telefone em claro. Mensagens de entrada e saída e a caixa Fake são cifradas em repouso. Categoria, sugestão e protocolo são armazenados sem conteúdo literal.
- Webhook: IDs `wamid`, hash do remetente, tipo e datas de processamento. O corpo bruto não é persistido nem registrado em log; a assinatura é validada antes de enfileirar o processamento.
- Consentimento e participação: timestamp do consentimento presencial, data de admissão, estado de participação e opt-out. O registro não substitui a revisão da base legal.

Os exemplos incluídos são fictícios. O loader deles só é ativado no perfil `dev`; produção não carrega esses exemplos.

## Acessos

- RH: colaboradores, alertas, relatos e detalhe de bem-estar com resumo neutro.
- Gerente: colaboradores da própria loja e alertas encaminhados ao gerente; bem-estar limitado a categoria e sugestão. A rota de relatos responde com proibição.
- Anônimo: nenhuma API de negócio; apenas páginas públicas de autenticação e webhook validado por assinatura. `/dev/whatsapp/**` só existe em `dev` e `test`.
- Relatos são confidenciais. Loja e turno aparecem apenas no detalhe autorizado e podem facilitar inferência de identidade.

## Proteções implementadas

- Senhas BCrypt; usuário inicial obtido por variáveis de ambiente somente se a tabela de usuários estiver vazia.
- Cinco falhas de autenticação iniciam bloqueio de 15 minutos; a resposta de login é genérica.
- Sessão no cookie HttpOnly, SameSite=Lax, timeout configurável (30 minutos por padrão) e Secure em produção.
- CSRF obrigatório para login e operações que alteram estado.
- Telefone preparado para AES-GCM e índice de busca HMAC-SHA256; produção recusa iniciar sem `APP_CRYPTO_KEY` e `APP_HASH_KEY`.
- Auditoria para abertura e alteração de relato; chaves de produção só por variáveis de ambiente.
- CSP, X-Content-Type-Options, Referrer-Policy, X-Frame-Options e HSTS em HTTPS no perfil de produção.
- Respostas não incluem mensagens internas nem stack trace. A aplicação não registra corpo de requisição, telefone, resumo de bem-estar nem conteúdo de relato.
- Conteúdo de mensagens é expurgado após `app.retencao.mensagens-dias` (120 dias por padrão), inclusive da caixa Fake. A participação ativa termina após 90 dias da admissão e mensagens agendadas pendentes são canceladas.
- `SAIR`, `PARAR` ou `CANCELAR` encerra a participação e cancela mensagens agendadas. Telefones são mascarados nas respostas de cadastro.

## Pendências para uso real

- Validar base legal e transparência com assessoria responsável e RH.
- Confirmar base legal, prazos e procedimentos de descarte de atributos de cadastro, resposta a solicitações de titulares, revisão periódica de acessos e processo de incidente.
- Definir custódia, rotação e recuperação segura das chaves; a aplicação não implementa rotação de chave nesta fase.
- Revisar templates, perfis, textos de consentimento e procedimento humano para alertas graves antes de inserir dados reais. Validar números e orientações de apoio com o RH.
