package com.porto.testecnae.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

/**
 * Tratamento global de erros da API. Converte excecoes de dominio em
 * respostas HTTP padronizadas (corpo {@link ErroResponse}).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarRecursoNaoEncontrado(
            RecursoNaoEncontradoException ex, HttpServletRequest request) {
        return erro(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return erro(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarCorpoIlegivel(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return erro(HttpStatus.BAD_REQUEST, "Corpo da requisicao invalido ou malformado", request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponse> tratarParametroAusente(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        return erro(HttpStatus.BAD_REQUEST,
                "Parametro obrigatorio ausente: " + ex.getParameterName(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> tratarIntegridade(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        return erro(HttpStatus.BAD_REQUEST, "Dados invalidos para persistencia", request);
    }

    private ResponseEntity<ErroResponse> erro(HttpStatus status, String mensagem, HttpServletRequest request) {
        ErroResponse corpo = new ErroResponse(
                OffsetDateTime.now(),
                status.value(),
                mensagem,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(corpo);
    }
}
