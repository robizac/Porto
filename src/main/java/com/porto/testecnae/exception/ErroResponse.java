package com.porto.testecnae.exception;

import java.time.OffsetDateTime;

/**
 * Corpo padronizado de resposta de erro da API.
 */
public record ErroResponse(
        OffsetDateTime timestamp,
        int status,
        String mensagem,
        String path
) {
}
