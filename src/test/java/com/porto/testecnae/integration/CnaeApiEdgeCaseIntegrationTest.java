package com.porto.testecnae.integration;

import com.porto.testecnae.repository.AtividadeEconomicaCnaeRepository;
import com.porto.testecnae.repository.CadastroSecundarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Casos de borda de robustez: base vazia (clausula 1.6), payload acima do
 * limite das colunas, corpo malformado e parametro obrigatorio ausente.
 */
@SpringBootTest
@Transactional
class CnaeApiEdgeCaseIntegrationTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private AtividadeEconomicaCnaeRepository cnaeRepository;
    @Autowired
    private CadastroSecundarioRepository cadastroRepository;

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // 1.6 -> 2.3/2.6: base vazia nao deve gerar 500 (getFirst()), e sim 404 com corpo padronizado
    @Test
    void baseVazia_buscarPorCodigo_404() throws Exception {
        cnaeRepository.deleteAll();
        mvc.perform(get("/api/cnaes/codigo").param("codigo", "9999-9/99"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.status").value(404))
           .andExpect(jsonPath("$.path").value("/api/cnaes/codigo"));
    }

    @Test
    void baseVazia_validarCnae_404() throws Exception {
        cnaeRepository.deleteAll();
        mvc.perform(get("/api/cadastros-secundarios/validar-cnae").param("codigoCnae", "9999-9/99"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void baseVazia_cadastrarCnaeInexistente_404_semPersistir() throws Exception {
        cnaeRepository.deleteAll();
        String body = "{\"nomeFantasia\":\"Tech Porto\",\"documento\":\"12345678000199\",\"codigoCnae\":\"9999-9/99\"}";
        mvc.perform(post("/api/cadastros-secundarios").contentType(MediaType.APPLICATION_JSON).content(body))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.status").value(404));
        assertThat(cadastroRepository.count()).isZero();
    }

    // documento excede varchar(14): deve ser 400 (validacao), nao 500 (erro de banco)
    @Test
    void documentoAcimaDoLimite_400() throws Exception {
        String body = "{\"nomeFantasia\":\"Tech Porto\",\"documento\":\"1234567800019912345\",\"codigoCnae\":\"6201-5/01\"}";
        mvc.perform(post("/api/cadastros-secundarios").contentType(MediaType.APPLICATION_JSON).content(body))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.status").value(400))
           .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("documento")));
    }

    // nomeFantasia excede varchar(120): deve ser 400 (validacao), nao 500 (erro de banco)
    @Test
    void nomeFantasiaAcimaDoLimite_400() throws Exception {
        String nome = "x".repeat(121);
        String body = "{\"nomeFantasia\":\"" + nome + "\",\"documento\":\"12345678000199\",\"codigoCnae\":\"6201-5/01\"}";
        mvc.perform(post("/api/cadastros-secundarios").contentType(MediaType.APPLICATION_JSON).content(body))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.status").value(400))
           .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("nomeFantasia")));
    }

    @Test
    void corpoMalformado_400_corpoPadronizado() throws Exception {
        mvc.perform(post("/api/cadastros-secundarios").contentType(MediaType.APPLICATION_JSON).content("{"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void parametroObrigatorioAusente_400_corpoPadronizado() throws Exception {
        mvc.perform(get("/api/cnaes/codigo"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.status").value(400))
           .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("codigo")));
    }
}
