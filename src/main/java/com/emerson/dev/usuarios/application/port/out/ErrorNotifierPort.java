package com.emerson.dev.usuarios.application.port.out;

public interface ErrorNotifierPort {

    void notifyError(String path, String exceptionType, String message);
}
