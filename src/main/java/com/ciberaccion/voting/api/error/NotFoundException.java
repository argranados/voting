package com.ciberaccion.voting.api.error;

public class NotFoundException extends ApiException {
    public NotFoundException(String message) {
        super(message);
    }
}