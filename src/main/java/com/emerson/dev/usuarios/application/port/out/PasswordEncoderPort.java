package com.emerson.dev.usuarios.application.port.out;

public interface PasswordEncoderPort {

    String encode(String rawPassword);
}
