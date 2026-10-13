package com.porto.testecnae.repository;

import com.porto.testecnae.domain.AtividadeEconomicaCnae;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AtividadeEconomicaCnaeRepositoryTest {

    @Autowired
    private AtividadeEconomicaCnaeRepository repository;

    // C1 - substring no meio: "programas" deve casar 6201-5/01 e 6202-3/00
    @Test
    void buscarPorDescricao_substringNoMeio() {
        List<AtividadeEconomicaCnae> r = repository.buscarPorDescricao("programas");
        assertThat(r).extracting(AtividadeEconomicaCnae::getCodigo)
                .containsExactly("6201-5/01", "6202-3/00");
    }

    // 2.1 - case-insensitive
    @Test
    void buscarPorDescricao_caseInsensitive() {
        assertThat(repository.buscarPorDescricao("PROGRAMAS"))
                .extracting(AtividadeEconomicaCnae::getCodigo)
                .containsExactly("6201-5/01", "6202-3/00");
    }

    // 3.7 - prefixo preservado + ordenacao por codigo
    @Test
    void buscarPorDescricao_prefixoOrdenado() {
        assertThat(repository.buscarPorDescricao("Cultivo"))
                .extracting(AtividadeEconomicaCnae::getCodigo)
                .containsExactly("0111-3/01", "0112-1/01");
    }

    // 3.3 - termo sem ocorrencia retorna vazio
    @Test
    void buscarPorDescricao_semOcorrencia_vazio() {
        assertThat(repository.buscarPorDescricao("xyzinexistente")).isEmpty();
    }

    // findByCodigo basico
    @Test
    void findByCodigo_existenteEInexistente() {
        assertThat(repository.findByCodigo("6201-5/01")).isPresent();
        assertThat(repository.findByCodigo("9999-9/99")).isEmpty();
    }
}
