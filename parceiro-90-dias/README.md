# Parceiro dos 90 dias — Kallopolli

Dashboard estático para acompanhamento de pessoas no período inicial de 90 dias. A interface usa HTML semântico, CSS e JavaScript nativo (ES modules), sem framework, bundler, CDN ou chamadas externas.

## Como rodar

Na pasta `parceiro-90-dias/`, execute `python -m http.server 8000 --directory dashboard` e abra `http://localhost:8000`. Também é possível servir `dashboard/` com qualquer servidor estático.

## Estrutura

- `dashboard/index.html`: shell e ponto de entrada.
- `dashboard/css/`: reset e estilos da interface.
- `dashboard/js/`: inicialização, API, utilitários seguros e telas.
- `dashboard/mock/`: dados demonstrativos fictícios.
- `docs/`: contrato proposto para integração e decisões de produto/privacidade.

## Convenções

- A navegação usa hash routes para Visão geral, Funcionários, Alertas, Relatos, Bem-estar e Relatórios. A rota padrão é `#/visao-geral`.
- `dashboard/js/api.js` define `USE_MOCK`. Com `true`, carrega JSON local; com `false`, acessa somente `/api/*`.
- O perfil de demonstração é `MOCK_PERFIL` em `dashboard/js/api.js` (`RH` ou `GERENTE`). O perfil GERENTE não solicita dados de relatos confidenciais.
- Conteúdo dinâmico é inserido com `textContent` e `createElement` via helper `h()`; não use `innerHTML` para dados.
- Ícones e fontes são servidos localmente. Mantenha os exemplos de pessoas e alertas como fictícios.
- Os mocks deste repositório são exclusivamente fictícios e não representam colaboradores reais.

## Fases entregues

Visão geral, Funcionários, Alertas, Relatos confidenciais, Bem-estar e Relatórios. Alterações de status e observações dos relatos são locais; geração de análise usa um mock com atraso simulado. Não existe integração real com backend nesta entrega.

## Backend Fase 3A

O backend Spring Boot 3.5.16/Java 21 está em `backend/`; o Maven copia o dashboard de `parceiro-90-dias/dashboard/` para o jar. A API e o painel usam a mesma origem.

### Docker Compose

1. Copie `.env.example` para `.env` e substitua os campos de e-mail/senha inicial e chaves. As senhas devem ter ao menos 12 caracteres, maiúscula, minúscula, número e símbolo. Nunca compartilhe o `.env`.
2. Na raiz do repositório, execute `docker compose up --build`.
3. Acesse `http://localhost:8080`. A conta RH vem de `APP_SEED_RH_EMAIL` e `APP_SEED_RH_PASSWORD`; a conta gerente é opcional.
4. O banco dev carrega os exemplos fictícios de forma idempotente. O seed de usuários só ocorre quando não há usuários.

Para executar o backend sem Docker, inicie PostgreSQL 16 e rode `backend/mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev` no Windows (ou `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` em Linux/macOS), com as variáveis do `.env` exportadas.

### Verificação

No diretório `backend/`, execute `./mvnw verify` (Windows: `./mvnw.cmd verify`). Os testes de integração usam PostgreSQL Testcontainers e precisam de Docker. O workflow de CI roda o mesmo comando no push e em pull requests.

Para produção, use o perfil `prod`, configure credenciais e segredos fortes por ambiente, configure HTTPS e não habilite carga fictícia. `APP_CRYPTO_KEY` e `APP_HASH_KEY` são obrigatórias. Consulte [`docs/seguranca-lgpd.md`](docs/seguranca-lgpd.md) e [`docs/decisoes.md`](docs/decisoes.md).
