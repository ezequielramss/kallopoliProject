package br.com.kallopolli.parceiro90.kpi;

import br.com.kallopolli.parceiro90.comum.DashboardDados;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.sql.Timestamp;
import java.time.Clock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kpis")
public class KpiController {
    private final DashboardDados dados;
    private final JdbcTemplate jdbc; private final Clock clock;
    public KpiController(DashboardDados dados,JdbcTemplate jdbc,Clock clock) { this.dados = dados; this.jdbc=jdbc; this.clock=clock; }
    @GetMapping JsonNode kpis() {
        ObjectNode out=((ObjectNode)dados.objeto("kpis","principal")).deepCopy();
        Integer ativos=jdbc.queryForObject("SELECT count(*) FROM colaborador WHERE participacao_status='ATIVO'",Integer.class);
        Integer responderam=jdbc.queryForObject("SELECT count(DISTINCT c.id) FROM colaborador c JOIN mensagem_entrada m ON m.colaborador_id=c.id WHERE c.participacao_status='ATIVO' AND c.data_admissao IS NOT NULL AND m.recebida_em>=c.data_admissao::timestamptz AND m.recebida_em<c.data_admissao::timestamptz+interval '10 days'",Integer.class);
        Integer abertas=jdbc.queryForObject("SELECT count(*) FROM alerta WHERE status='ABERTO'",Integer.class);
        Integer atendidos=jdbc.queryForObject("SELECT count(*) FROM alerta WHERE status='ATENDIDO'",Integer.class);
        Integer semRetorno=jdbc.queryForObject("SELECT count(*) FROM alerta WHERE status='ABERTO' AND criado_em<=?",Integer.class,Timestamp.from(clock.instant().minusSeconds(86400)));
        Double media=jdbc.queryForObject("SELECT COALESCE(avg(extract(epoch from (respondido_em-criado_em))/3600),0) FROM alerta WHERE respondido_em IS NOT NULL",Double.class);
        out.put("emJornada",ativos==null?0:ativos); out.put("taxaResposta10d",ativos==null||ativos==0?0:Math.round(100.0*(responderam==null?0:responderam)/ativos));
        out.put("alertasAbertos",abertas==null?0:abertas); out.put("atendidos",atendidos==null?0:atendidos); out.put("semRetorno24h",semRetorno==null?0:semRetorno); out.put("tempoMedioRetornoHoras",media==null?0:Math.round(media*10.0)/10.0);
        return out;
    }
    @GetMapping("/serie-mensal") JsonNode serie() { return dados.objeto("serie-mensal", "principal"); }
}
