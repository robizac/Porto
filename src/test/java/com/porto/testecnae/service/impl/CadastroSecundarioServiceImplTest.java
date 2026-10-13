package com.porto.testecnae.service.impl;

import com.porto.testecnae.domain.AtividadeEconomicaCnae;
import com.porto.testecnae.domain.CadastroSecundario;
import com.porto.testecnae.dto.CadastroSecundarioRequest;
import com.porto.testecnae.dto.CadastroSecundarioResponse;
import com.porto.testecnae.exception.RecursoNaoEncontradoException;
import com.porto.testecnae.repository.AtividadeEconomicaCnaeRepository;
import com.porto.testecnae.repository.CadastroSecundarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CadastroSecundarioServiceImplTest {

    @Mock
    private CadastroSecundarioRepository repository;
    @Mock
    private AtividadeEconomicaCnaeRepository cnaeRepository;
    @InjectMocks
    private CadastroSecundarioServiceImpl service;

    private AtividadeEconomicaCnae cnae() {
        return AtividadeEconomicaCnae.builder().id(5L).codigo("6201-5/01").descricao("Desenvolvimento de programas").secao("Tecnologia").build();
    }

    // C3 - fix check: POST com CNAE inexistente lanca 404 e NUNCA persiste
    @Test
    void cadastrar_cnaeInexistente_lancaENaoPersiste() {
        var req = new CadastroSecundarioRequest("Tech Porto", "12345678000199", "9999-9/99");
        when(cnaeRepository.findByCodigo("9999-9/99")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.cadastrar(req)).isInstanceOf(RecursoNaoEncontradoException.class);
        verify(repository, never()).save(any());
    }

    // 3.6 - preservation: POST valido com CNAE existente persiste e vincula corretamente
    @Test
    void cadastrar_cnaeExistente_persisteVinculoCorreto() {
        var req = new CadastroSecundarioRequest("Tech Porto", "12345678000199", "6201-5/01");
        when(cnaeRepository.findByCodigo("6201-5/01")).thenReturn(Optional.of(cnae()));
        when(repository.save(any(CadastroSecundario.class))).thenAnswer(i -> {
            CadastroSecundario c = i.getArgument(0); c.setId(1L); return c;
        });
        CadastroSecundarioResponse r = service.cadastrar(req);
        assertThat(r.cnae().codigo()).isEqualTo("6201-5/01");
        verify(repository).save(any(CadastroSecundario.class));
    }

    // C4 - fix check: validar-cnae inexistente lanca 404
    @Test
    void validarCnae_inexistente_lanca() {
        when(cnaeRepository.findByCodigo("9999-9/99")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.validarCnae("9999-9/99")).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // 3.5 - preservation: validar-cnae existente retorna o CNAE
    @Test
    void validarCnae_existente_retornaCnae() {
        when(cnaeRepository.findByCodigo("6201-5/01")).thenReturn(Optional.of(cnae()));
        assertThat(service.validarCnae("6201-5/01").codigo()).isEqualTo("6201-5/01");
    }
}
