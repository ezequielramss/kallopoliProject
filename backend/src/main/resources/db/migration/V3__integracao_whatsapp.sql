ALTER TABLE colaborador
  ADD COLUMN consentimento_em TIMESTAMPTZ,
  ADD COLUMN data_admissao DATE,
  ADD COLUMN participacao_status VARCHAR(24) NOT NULL DEFAULT 'NAO_PARTICIPA',
  ADD COLUMN opt_out_em TIMESTAMPTZ,
  ADD COLUMN ultima_mensagem_entrada_em TIMESTAMPTZ,
  ADD COLUMN desligado_em TIMESTAMPTZ,
  ADD CONSTRAINT colaborador_participacao_status_ck CHECK (participacao_status IN ('PENDENTE_OPTIN','ATIVO','NAO_PARTICIPA','OPT_OUT','ENCERRADO'));

CREATE TABLE lider_contato (
  id UUID PRIMARY KEY,
  nome VARCHAR(160) NOT NULL,
  perfil VARCHAR(10) NOT NULL CHECK (perfil IN ('RH','GERENTE')),
  loja_id BIGINT REFERENCES loja(id),
  telefone_cifrado TEXT NOT NULL,
  telefone_hash CHAR(64) NOT NULL UNIQUE,
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  ultima_mensagem_entrada_em TIMESTAMPTZ,
  criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK ((perfil = 'RH' AND loja_id IS NULL) OR (perfil = 'GERENTE' AND loja_id IS NOT NULL))
);
CREATE INDEX lider_contato_loja_idx ON lider_contato(loja_id, perfil, ativo);

ALTER TABLE alerta ADD COLUMN destinatario_hash CHAR(64);
CREATE INDEX alerta_destinatario_hash_idx ON alerta(destinatario_hash);

CREATE TABLE mensagem_entrada (
  id BIGSERIAL PRIMARY KEY,
  wamid VARCHAR(160) NOT NULL UNIQUE,
  colaborador_id VARCHAR(40) REFERENCES colaborador(id),
  lider_contato_id UUID REFERENCES lider_contato(id),
  remetente_hash CHAR(64) NOT NULL,
  tipo VARCHAR(24) NOT NULL,
  conteudo_cifrado TEXT,
  categoria VARCHAR(40),
  severidade VARCHAR(20),
  resumo_neutro TEXT,
  sugestao TEXT,
  recebida_em TIMESTAMPTZ NOT NULL,
  processada_em TIMESTAMPTZ
);
CREATE INDEX mensagem_entrada_colaborador_idx ON mensagem_entrada(colaborador_id, recebida_em);
CREATE INDEX mensagem_entrada_remetente_idx ON mensagem_entrada(remetente_hash, recebida_em);
ALTER TABLE alerta ADD COLUMN mensagem_entrada_wamid VARCHAR(160) UNIQUE REFERENCES mensagem_entrada(wamid);
ALTER TABLE alerta ADD COLUMN lembrete_processando_em TIMESTAMPTZ;
CREATE TABLE alerta_destinatario (
  alerta_id VARCHAR(40) NOT NULL REFERENCES alerta(id),
  lider_contato_id UUID NOT NULL REFERENCES lider_contato(id),
  acao VARCHAR(20) NOT NULL DEFAULT 'PENDENTE' CHECK (acao IN ('PENDENTE','CONVERSEI','NAO_CONSEGUI')),
  retorno_em TIMESTAMPTZ,
  PRIMARY KEY (alerta_id, lider_contato_id)
);

CREATE TABLE mensagem_saida (
  id UUID PRIMARY KEY,
  wamid VARCHAR(160) UNIQUE,
  destinatario_hash CHAR(64) NOT NULL,
  colaborador_id VARCHAR(40) REFERENCES colaborador(id),
  lider_contato_id UUID REFERENCES lider_contato(id),
  tipo VARCHAR(40) NOT NULL,
  template_nome VARCHAR(100),
  conteudo_cifrado TEXT,
  status VARCHAR(16) NOT NULL CHECK (status IN ('PENDENTE','ENVIADA','ENTREGUE','LIDA','FALHA')),
  erro_codigo VARCHAR(60),
  criada_em TIMESTAMPTZ NOT NULL DEFAULT now(),
  enviada_em TIMESTAMPTZ,
  atualizada_em TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX mensagem_saida_status_idx ON mensagem_saida(status, criada_em);
CREATE INDEX mensagem_saida_destinatario_idx ON mensagem_saida(destinatario_hash, criada_em);

CREATE TABLE mensagem_agendada (
  id UUID PRIMARY KEY,
  colaborador_id VARCHAR(40) NOT NULL REFERENCES colaborador(id),
  tipo VARCHAR(24) NOT NULL,
  dia_jornada INTEGER NOT NULL,
  agendada_para TIMESTAMPTZ NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDENTE' CHECK (status IN ('PENDENTE','PROCESSANDO','ENVIADA','CANCELADA','FALHA')),
  tentativas INTEGER NOT NULL DEFAULT 0,
  erro_codigo VARCHAR(60),
  criada_em TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (colaborador_id, tipo, dia_jornada)
);
CREATE INDEX mensagem_agendada_pendente_idx ON mensagem_agendada(status, agendada_para);

CREATE TABLE webhook_evento (
  id BIGSERIAL PRIMARY KEY,
  wamid VARCHAR(160) UNIQUE,
  evento_chave VARCHAR(220) NOT NULL UNIQUE,
  tipo VARCHAR(24) NOT NULL,
  estado VARCHAR(16) NOT NULL DEFAULT 'RECEBIDO' CHECK (estado IN ('RECEBIDO','PROCESSANDO','PROCESSADO','ERRO')),
  recebido_em TIMESTAMPTZ NOT NULL DEFAULT now(),
  processado_em TIMESTAMPTZ,
  erro_codigo VARCHAR(60)
);
CREATE INDEX webhook_evento_pendente_idx ON webhook_evento(processado_em, recebido_em);

CREATE TABLE outbox_dev (
  id BIGSERIAL PRIMARY KEY,
  mensagem_saida_id UUID NOT NULL UNIQUE REFERENCES mensagem_saida(id),
  destinatario_hash CHAR(64) NOT NULL,
  tipo VARCHAR(40) NOT NULL,
  template_nome VARCHAR(100),
  conteudo_cifrado TEXT,
  criada_em TIMESTAMPTZ NOT NULL DEFAULT now()
);
