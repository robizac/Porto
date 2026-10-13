package com.porto.testecnae.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CnaeApiIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void listarCnaes_retornaTodos() throws Exception {
        mvc.perform(get("/api/cnaes"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.length()").value(8));
    }

    // C1 - "programas" (substring no meio)
    @Test
    void buscar_programas_retornaDois() throws Exception {
        mvc.perform(get("/api/cnaes/buscar").param("termo", "programas"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.length()").value(2))
           .andExpect(jsonPath("$[0].codigo").value("6201-5/01"))
           .andExpect(jsonPath("$[1].codigo").value("6202-3/00"));
    }

    @Test
    void buscarPorCodigo_existente_200() throws Exception {
        mvc.perform(get("/api/cnaes/codigo").param("codigo", "6201-5/01"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.codigo").value("6201-5/01"));
    }

    // C2 - codigo inexistente -> 404
    @Test
    void buscarPorCodigo_inexistente_404() throws Exception {
        mvc.perform(get("/api/cnaes/codigo").param("codigo", "9999-9/99"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void validarCnae_existente_200() throws Exception {
        mvc.perform(get("/api/cadastros-secundarios/validar-cnae").param("codigoCnae", "6201-5/01"))
           .andExpect(status().isOk());
    }

    // C4 - validar-cnae inexistente -> 404
    @Test
    void validarCnae_inexistente_404() throws Exception {
        mvc.perform(get("/api/cadastros-secundarios/validar-cnae").param("codigoCnae", "9999-9/99"))
           .andExpect(status().isNotFound());
    }

    // 3.6 - POST valido -> 201
    @Test
    void criarCadastro_valido_201() throws Exception {
        String body = "{\"nomeFantasia\":\"Tech Porto\",\"documento\":\"12345678000199\",\"codigoCnae\":\"6201-5/01\"}";
        mvc.perform(post("/api/cadastros-secundarios").contentType(MediaType.APPLICATION_JSON).content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.cnae.codigo").value("6201-5/01"));
    }

    // C3 - POST com CNAE inexistente -> 404
    @Test
    void criarCadastro_cnaeInexistente_404() throws Exception {
        String body = "{\"nomeFantasia\":\"X\",\"documento\":\"123\",\"codigoCnae\":\"9999-9/99\"}";
        mvc.perform(post("/api/cadastros-secundarios").contentType(MediaType.APPLICATION_JSON).content(body))
           .andExpect(status().isNotFound());
    }

    // 2.7 - payload invalido -> 400
    @Test
    void criarCadastro_payloadInvalido_400() throws Exception {
        String body = "{\"nomeFantasia\":\"\",\"documento\":\"\",\"codigoCnae\":\"\"}";
        mvc.perform(post("/api/cadastros-secundarios").contentType(MediaType.APPLICATION_JSON).content(body))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.status").value(400));
    }
}
