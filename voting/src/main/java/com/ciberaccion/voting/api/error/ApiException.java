package com.ciberaccion.voting.api.error;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}