package com.porto.testecnae.dto;

import jakarta.validation.constraints.NotBlank;

public record CadastroSecundarioRequest(
        @NotBlank(message = "nomeFantasia e obrigatorio")
        String nomeFantasia,

        @NotBlank(message = "documento e obrigatorio")
        String documento,

        @NotBlank(message = "codigoCnae e obrigatorio")
        String codigoCnae
) {
}
