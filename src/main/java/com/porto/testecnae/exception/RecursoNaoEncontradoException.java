package com.porto.testecnae.exception;

/**
 * Exceção de domínio lançada quando um recurso solicitado não existe.
 * Mapeada para HTTP 404 pelo {@link GlobalExceptionHandler}.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
