# Orientações do projeto

- Empresa: Kallopolli. Commits em português, no padrão Conventional Commits.
- Frontend: HTML/CSS/JavaScript simples em `parceiro-90-dias/dashboard`; preserve o contrato JSON e a acessibilidade existente.
- Backend: Java 21, Spring Boot 3, PostgreSQL, pacotes por funcionalidade; sem Lombok; DTOs preferencialmente `record`.
- Linguagem respeitosa, sem vigilância, diagnóstico ou expressões como “risco de saída”/“chance de saída”. Sempre explicar `motivoAtencao`.
- Roteamento de alertas por categoria conforme `RoteamentoAlerta`; relatos não têm vínculo com colaborador. Loja e turno de relato aparecem só no detalhe.
- Use “confidencial”, nunca prometa “100% anônimo”. Resumos de bem-estar só podem ser vistos pelo RH.
- Não registre dados pessoais, telefones, relatos ou conteúdo de bem-estar em logs nem prompts de IA. Segredos apenas em variáveis de ambiente.
