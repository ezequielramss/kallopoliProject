package br.com.kallopolli.parceiro90.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class ConteudoCadencia {
    private final List<Fase> fases;
    private final List<String> pontes;
    private final List<Pilula> pilulas;
    public ConteudoCadencia() {
        try {
            var mapper = new ObjectMapper(new YAMLFactory());
            var companheiro = mapper.readValue(new ClassPathResource("conteudo/companheiro.yml").getInputStream(), Companheiro.class);
            var material = mapper.readValue(new ClassPathResource("conteudo/pilulas.yml").getInputStream(), MaterialPilulas.class);
            this.fases = List.copyOf(companheiro.fases()); this.pontes = List.copyOf(companheiro.ponte()); this.pilulas = List.copyOf(material.pilulas());
            validar();
        } catch (Exception ex) { throw new IllegalStateException("Conteúdo local de cadência inválido."); }
    }
    public String checkin(int dia) {
        Fase fase = fases.stream().filter(f -> dia >= f.inicio() && dia <= f.fim()).findFirst().orElseThrow();
        return fase.mensagens().get(Math.floorMod(dia - fase.inicio(), fase.mensagens().size()));
    }
    public String ponte(int dia) { return pontes.get(Math.floorMod(dia, pontes.size())); }
    public String pilula(int numero) { return pilulas.get(Math.floorMod(numero, pilulas.size())).texto(); }
    private void validar() {
        if (pontes.isEmpty() || pilulas.size() != 5 || pilulas.stream().anyMatch(p -> !"RASCUNHO".equals(p.status()))) throw new IllegalStateException();
        for (Fase fase : fases) for (String msg : fase.mensagens()) if (msg.length() > 280 || msg.chars().filter(c -> c == '?').count() > 1) throw new IllegalStateException();
    }
    private record Companheiro(List<Fase> fases, List<String> ponte) { }
    private record Fase(int inicio, int fim, List<String> mensagens) { }
    private record MaterialPilulas(List<Pilula> pilulas) { }
    private record Pilula(String id, String status, String texto) { }
}
