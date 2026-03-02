package com.ciberaccion.voting.api.error;

public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(message);
    }
}