# Segurança e privacidade — Parceiro dos 90 dias

Este documento descreve o protótipo técnico da Fase 3A. A base legal aplicável e os prazos de retenção devem ser definidos e aprovados pelo RH e pelas áreas responsáveis antes de uso com dados reais.

## Dados tratados

- Conta de acesso: e-mail, hash BCrypt da senha, perfil, loja do gerente, contador de falhas e bloqueio temporário.
- Colaborador: identificador interno, nome, iniciais, função e loja. O modelo reserva campos para telefone cifrado com AES-GCM e hash HMAC-SHA256; não armazene telefone em claro.
- Alerta: categoria, severidade, destino, protocolo, status, retorno, timestamps e sugestão de acompanhamento.
- Relato confidencial: protocolo, categoria, severidade, status, loja, turno, título e conteúdo. O relato não contém telefone ou identificador de colaborador.
- Bem-estar: categoria, resumo neutro, data e tag; o resumo só é acessível ao RH.
- Auditoria: conta de acesso, leitura/alteração, protocolo e data/hora. Não copia conteúdo do relato.

Os exemplos incluídos são fictícios. O loader deles só é ativado no perfil `dev`; produção não carrega esses exemplos.

## Acessos

- RH: colaboradores, alertas, relatos e detalhe de bem-estar com resumo neutro.
- Gerente: colaboradores da própria loja e alertas encaminhados ao gerente; bem-estar limitado a categoria e sugestão. A rota de relatos responde com proibição.
- Anônimo: nenhuma API; apenas a página estática de login e a inicialização do token CSRF.
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

## Pendências para uso real

- Validar base legal e transparência com assessoria responsável e RH.
- Definir retenção, descarte, resposta a solicitações de titulares, revisão periódica de acessos e processo de incidente.
- Definir custódia, rotação e recuperação segura das chaves; a aplicação não implementa rotação de chave nesta fase.
- Revisar perfis, textos de consentimento e fluxo de relato antes de inserir dados reais.
