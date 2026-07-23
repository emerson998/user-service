package com.emerson.dev.usuarios.domain.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resource, Object id) {
        super("%s com id %s já existe".formatted(resource, id));
    }
}
