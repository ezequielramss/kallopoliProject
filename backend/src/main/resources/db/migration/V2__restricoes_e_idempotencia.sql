ALTER TABLE usuario DROP CONSTRAINT usuario_perfil_loja_ck;
ALTER TABLE usuario ADD CONSTRAINT usuario_perfil_loja_ck
  CHECK ((perfil = 'RH' AND loja_id IS NULL) OR (perfil = 'GERENTE' AND loja_id IS NOT NULL));

DO $migration$
BEGIN
  IF EXISTS (SELECT 1 FROM relato_confidencial WHERE loja_id IS NULL) THEN
    RAISE EXCEPTION 'Relatos existentes precisam de loja associada antes da migração V2.';
  END IF;
END
$migration$;
ALTER TABLE relato_confidencial ALTER COLUMN loja_id SET NOT NULL;

DELETE FROM bemestar_evento a
USING bemestar_evento b
WHERE a.colaborador_id = b.colaborador_id
  AND a.data = b.data AND a.tipo = b.tipo AND a.tag = b.tag
  AND a.id > b.id;
CREATE UNIQUE INDEX bemestar_evento_idempotencia_idx ON bemestar_evento(colaborador_id, data, tipo, tag);
CREATE UNIQUE INDEX alerta_evento_idempotencia_idx ON alerta_evento(alerta_id, tipo, criado_em);
