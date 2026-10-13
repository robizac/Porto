package com.porto.testecnae.service.impl;

import com.porto.testecnae.domain.AtividadeEconomicaCnae;
import com.porto.testecnae.dto.AtividadeEconomicaCnaeResponse;
import com.porto.testecnae.exception.RecursoNaoEncontradoException;
import com.porto.testecnae.repository.AtividadeEconomicaCnaeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtividadeEconomicaCnaeServiceImplTest {

    @Mock
    private AtividadeEconomicaCnaeRepository repository;

    @InjectMocks
    private AtividadeEconomicaCnaeServiceImpl service;

    private AtividadeEconomicaCnae cnae(Long id, String codigo, String descricao) {
        return AtividadeEconomicaCnae.builder().id(id).codigo(codigo).descricao(descricao).secao("Tecnologia").build();
    }

    // C2 - fix check: codigo inexistente lanca 404 em vez de retornar CNAE arbitrario
    @Test
    void buscarPorCodigo_inexistente_lancaRecursoNaoEncontrado() {
        when(repository.findByCodigo("9999-9/99")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorCodigo("9999-9/99"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // 3.2 - preservation: codigo existente retorna o CNAE correto
    @Test
    void buscarPorCodigo_existente_retornaCnae() {
        when(repository.findByCodigo("6201-5/01"))
                .thenReturn(Optional.of(cnae(5L, "6201-5/01", "Desenvolvimento de programas")));
        AtividadeEconomicaCnaeResponse r = service.buscarPorCodigo("6201-5/01");
        assertThat(r.codigo()).isEqualTo("6201-5/01");
    }

    // guard - termo nulo/vazio retorna lista vazia sem acionar a query
    @Test
    void buscarPorDescricao_termoVazio_retornaVaziaSemQuery() {
        assertThat(service.buscarPorDescricao("")).isEmpty();
        assertThat(service.buscarPorDescricao("   ")).isEmpty();
        assertThat(service.buscarPorDescricao(null)).isEmpty();
        verifyNoInteractions(repository);
    }

    // 3.1 - preservation: listagem delega ao repositorio
    @Test
    void listarTodas_mapeiaResultado() {
        when(repository.findAll()).thenReturn(List.of(cnae(1L, "0111-3/01", "Cultivo de arroz")));
        List<AtividadeEconomicaCnaeResponse> r = service.listarTodas();
        assertThat(r).hasSize(1);
        assertThat(r.get(0).codigo()).isEqualTo("0111-3/01");
    }
}
