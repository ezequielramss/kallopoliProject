package br.com.kallopolli.parceiro90.whatsapp;

import static org.junit.jupiter.api.Assertions.*;
import br.com.kallopolli.parceiro90.relato.ClassificadorSinaisStub;
import br.com.kallopolli.parceiro90.relato.Categoria;
import br.com.kallopolli.parceiro90.relato.EntradaClassificacao;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassificadorSinaisStubTest {
    private final ClassificadorSinaisStub classificador=new ClassificadorSinaisStub();
    @Test void padraoPermaneceNeutro(){assertEquals(Categoria.NEUTRO,classificador.classificar(new EntradaClassificacao("Oi, tudo bem?",2,List.of())).categoria());}
    @Test void palavrasChaveNaoIdentificamPessoa(){assertEquals(Categoria.ASSEDIO,classificador.classificar(new EntradaClassificacao("Sofri assédio",10,List.of())).categoria());}
    @Test void comandosIgnoramAcentosECaixa(){assertEquals("sair",ProcessadorEntradaWhatsApp.normalizarComando("SAÍR!"));assertEquals("parar",ProcessadorEntradaWhatsApp.normalizarComando("Parár"));}
}
