CREATE TABLE loja (
  id BIGSERIAL PRIMARY KEY, nome VARCHAR(120) NOT NULL UNIQUE, ativa BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE usuario (
  id BIGSERIAL PRIMARY KEY, email VARCHAR(254) NOT NULL UNIQUE, senha_hash VARCHAR(100) NOT NULL,
  perfil VARCHAR(10) NOT NULL CHECK (perfil IN ('RH','GERENTE')), loja_id BIGINT REFERENCES loja(id),
  falhas_login INTEGER NOT NULL DEFAULT 0, bloqueado_ate TIMESTAMPTZ,
  CONSTRAINT usuario_perfil_loja_ck CHECK ((perfil = 'RH' AND loja_id IS NULL) OR perfil = 'GERENTE')
);
CREATE TABLE colaborador (
  id VARCHAR(40) PRIMARY KEY, loja_id BIGINT NOT NULL REFERENCES loja(id), nome VARCHAR(160) NOT NULL,
  iniciais VARCHAR(8) NOT NULL, funcao VARCHAR(120) NOT NULL, telefone_cifrado TEXT,
  telefone_hash CHAR(64), criado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX colaborador_loja_idx ON colaborador(loja_id);
CREATE UNIQUE INDEX colaborador_telefone_hash_idx ON colaborador(telefone_hash) WHERE telefone_hash IS NOT NULL;
CREATE TABLE alerta (
  id VARCHAR(40) PRIMARY KEY, colaborador_id VARCHAR(40) REFERENCES colaborador(id), loja_id BIGINT NOT NULL REFERENCES loja(id),
  protocolo VARCHAR(80) NOT NULL UNIQUE, categoria VARCHAR(40) NOT NULL, severidade VARCHAR(20) NOT NULL,
  destino VARCHAR(10) NOT NULL CHECK (destino IN ('RH','GERENTE')), status VARCHAR(10) NOT NULL CHECK(status IN ('ABERTO','ATENDIDO')),
  retorno VARCHAR(20), destinatario_nome VARCHAR(160), criado_em TIMESTAMPTZ NOT NULL,
  respondido_em TIMESTAMPTZ, lembrete_enviado_em TIMESTAMPTZ, sugestao TEXT NOT NULL, colaborador_avisado BOOLEAN NOT NULL DEFAULT false
);
CREATE INDEX alerta_status_idx ON alerta(status); CREATE INDEX alerta_destino_idx ON alerta(destino);
CREATE INDEX alerta_loja_idx ON alerta(loja_id); CREATE INDEX alerta_criado_idx ON alerta(criado_em);
CREATE TABLE alerta_evento (
  id BIGSERIAL PRIMARY KEY, alerta_id VARCHAR(40) NOT NULL REFERENCES alerta(id), tipo VARCHAR(40) NOT NULL,
  criado_em TIMESTAMPTZ NOT NULL, detalhe TEXT
);
CREATE TABLE relato_confidencial (
  id BIGSERIAL PRIMARY KEY, protocolo VARCHAR(80) NOT NULL UNIQUE, loja_id BIGINT REFERENCES loja(id),
  turno VARCHAR(40) NOT NULL, titulo VARCHAR(240) NOT NULL, categoria VARCHAR(50) NOT NULL,
  severidade VARCHAR(20) NOT NULL, status VARCHAR(20) NOT NULL, conteudo TEXT NOT NULL,
  criado_em TIMESTAMPTZ NOT NULL
);
CREATE INDEX relato_criado_idx ON relato_confidencial(criado_em); CREATE INDEX relato_status_idx ON relato_confidencial(status);
CREATE TABLE relato_historico (
  id BIGSERIAL PRIMARY KEY, relato_id BIGINT NOT NULL REFERENCES relato_confidencial(id), titulo VARCHAR(240) NOT NULL,
  descricao TEXT NOT NULL, criado_em TIMESTAMPTZ NOT NULL
);
CREATE TABLE bemestar_evento (
  id BIGSERIAL PRIMARY KEY, colaborador_id VARCHAR(40) REFERENCES colaborador(id), categoria VARCHAR(100) NOT NULL,
  resumo_neutro TEXT NOT NULL, data TIMESTAMPTZ NOT NULL, tag VARCHAR(100) NOT NULL, tipo VARCHAR(40) NOT NULL
);
CREATE INDEX bemestar_data_idx ON bemestar_evento(data);
CREATE TABLE serie_mensal (id BIGSERIAL PRIMARY KEY, mes VARCHAR(20) NOT NULL UNIQUE, estavel INTEGER NOT NULL, atencao INTEGER NOT NULL, prioridade INTEGER NOT NULL, presenca INTEGER NOT NULL);
CREATE TABLE auditoria_acesso (
  id BIGSERIAL PRIMARY KEY, usuario_id BIGINT NOT NULL REFERENCES usuario(id), acao VARCHAR(30) NOT NULL CHECK(acao IN ('LEITURA_RELATO','ALTERACAO_RELATO')),
  protocolo VARCHAR(80) NOT NULL, criado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX auditoria_protocolo_idx ON auditoria_acesso(protocolo);
CREATE TABLE dashboard_dado (
  recurso VARCHAR(40) NOT NULL, chave VARCHAR(100) NOT NULL, conteudo JSONB NOT NULL,
  PRIMARY KEY (recurso, chave)
);
