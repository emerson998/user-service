package com.solutis.dev.domain.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Object id) {
        super("%s com id %s não encontrado".formatted(resource, id));
    }
}
