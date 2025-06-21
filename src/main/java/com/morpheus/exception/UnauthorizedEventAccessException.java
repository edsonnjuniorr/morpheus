package com.morpheus.exception;

public class UnauthorizedEventAccessException extends RuntimeException {
    public UnauthorizedEventAccessException() {
        super("Não autorizado a acessar este evento");
    }

    public UnauthorizedEventAccessException(String message) {
        super(message);
    }
}

