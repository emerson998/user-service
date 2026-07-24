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
        // CHAOS:FIX — nome pode vir nulo se o dado foi inserido contornando a
        // validação de @NotBlank; nesse caso devolvemos string vazia em vez de
        // quebrar com NullPointerException.
        return new UserResponse(
                user.getId(),
                user.getName() == null ? "" : user.getName().trim(),
                user.getEmail(),
                user.getPhone(),
                user.getBio(),
                user.isEnabled(),
                user.getSaldo());
        // CHAOS:TOGGLE:END
    }
}
