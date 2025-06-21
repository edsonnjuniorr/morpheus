package com.morpheus.exception;

/**
 * Exceção para erros recuperáveis durante o processamento de eventos.
 */
public class RecoverableEventException extends RuntimeException {
    public RecoverableEventException(String message) {
        super(message);
    }

    public RecoverableEventException(String message, Throwable cause) {
        super(message, cause);
    }
}

