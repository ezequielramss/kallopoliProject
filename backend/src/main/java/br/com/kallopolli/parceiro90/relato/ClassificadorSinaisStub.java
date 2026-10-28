package br.com.kallopolli.parceiro90.relato;

import java.text.Normalizer;
import java.util.Locale;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "test", "prod", "homolog"})
public class ClassificadorSinaisStub implements ClassificadorSinais {
    @Override public ResultadoClassificacao classificar(EntradaClassificacao entrada) {
        String texto = normalizar(entrada.textoAtual());
        if (contem(texto, "assedio", "assediou", "abuso sexual", "passou a mao"))
            return resultado(Categoria.ASSEDIO, Severidade.ALTA, "A pessoa descreveu uma situação de assédio.", "Encaminhar ao RH com confidencialidade.");
        if (contem(texto, "agressao", "ameaca", "perigo", "violencia", "me machucar", "suicid"))
            return resultado(Categoria.SITUACAO_GRAVE, Severidade.ALTA, "A pessoa pediu ajuda para uma situação urgente.", "Procurar a liderança e o RH agora.");
        if (contem(texto, "chefe", "gerente", "lideranca", "supervisor") && contem(texto, "gritou", "ameac", "humilhou", "persegue"))
            return resultado(Categoria.CONFLITO_CHEFIA, Severidade.ALTA, "A pessoa relatou dificuldade com a liderança.", "Encaminhar ao RH com confidencialidade.");
        if (contem(texto, "colega", "colegas") && contem(texto, "briga", "conflito", "discut"))
            return resultado(Categoria.CONFLITO_COLEGA, Severidade.MODERADA, "A pessoa relatou dificuldade de convivência na equipe.", "Vale uma conversa acolhedora.");
        if (contem(texto, "sobrecarreg", "muita tarefa", "nao dou conta", "sem pausa"))
            return resultado(Categoria.SOBRECARGA, Severidade.MODERADA, "A pessoa pediu apoio com a organização das tarefas.", "Vale conversar sobre a distribuição das tarefas.");
        if (contem(texto, "cansad", "exaust", "sem energia", "muito cansaco"))
            return resultado(Categoria.CANSACO, Severidade.MODERADA, "A pessoa mencionou cansaço na rotina.", "Vale uma conversa de acompanhamento.");
        if (contem(texto, "quero sair", "vou pedir demissao", "nao quero continuar"))
            return resultado(Categoria.VONTADE_DE_SAIR, Severidade.ALTA, "A pessoa pediu espaço para conversar sobre sua experiência.", "Vale oferecer uma conversa sem pressão.");
        if (contem(texto, "desmotivad", "sem vontade", "desanimad"))
            return resultado(Categoria.DESMOTIVACAO, Severidade.MODERADA, "A pessoa pediu apoio com sua experiência na função.", "Vale uma conversa aberta sobre a rotina.");
        if (contem(texto, "duvida", "nao entendi", "como faco", "orientacao"))
            return resultado(Categoria.DUVIDA, Severidade.BAIXA, "A pessoa pediu orientação sobre a rotina.", "Vale oferecer orientação sobre os procedimentos.");
        return new ResultadoClassificacao(Categoria.NEUTRO, Severidade.BAIXA, 0.0, "A pessoa compartilhou uma mensagem.", "Agradecer e manter o acompanhamento regular.");
    }
    private ResultadoClassificacao resultado(Categoria categoria, Severidade severidade, String resumo, String sugestao) {
        return new ResultadoClassificacao(categoria, severidade, 0.82, resumo, sugestao);
    }
    private String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto == null ? "" : texto, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return semAcento.toLowerCase(Locale.ROOT);
    }
    private boolean contem(String texto, String... termos) {
        for (String termo : termos) if (texto.contains(termo)) return true;
        return false;
    }
}
