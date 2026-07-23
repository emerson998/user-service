package com.emerson.dev.usuarios.application.mapper;

import com.emerson.dev.usuarios.application.dto.user.UserRequest;
import com.emerson.dev.usuarios.application.dto.user.UserResponse;
import com.emerson.dev.usuarios.application.port.out.PasswordEncoderPort;
import com.emerson.dev.usuarios.domain.model.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toDomain(UserRequest request, PasswordEncoderPort passwordEncoder) {
        return User.createNew(request.name(), request.email(), passwordEncoder.encode(request.password()));
    }

    public static UserResponse toResponse(User user) {
        // CHAOS:TOGGLE:BEGIN
        // CHAOS:BUG — assume que name nunca é nulo; essa garantia sempre veio da
        // validação de @NotBlank em UserRequest. O chaos insere um User direto pelo
        // repositório, contornando essa validação, e expõe esta falta de defesa.
        return new UserResponse(
                user.getId(),
                user.getName().trim(),
                user.getEmail(),
                user.getPhone(),
                user.getBio(),
                user.isEnabled());
        // CHAOS:TOGGLE:END
    }
}
