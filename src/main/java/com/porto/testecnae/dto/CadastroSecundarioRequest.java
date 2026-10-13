package com.porto.testecnae.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroSecundarioRequest(
        @NotBlank(message = "nomeFantasia e obrigatorio")
        @Size(max = 120, message = "nomeFantasia deve ter no maximo 120 caracteres")
        String nomeFantasia,

        @NotBlank(message = "documento e obrigatorio")
        @Size(max = 14, message = "documento deve ter no maximo 14 caracteres")
        String documento,

        @NotBlank(message = "codigoCnae e obrigatorio")
        String codigoCnae
) {
}
