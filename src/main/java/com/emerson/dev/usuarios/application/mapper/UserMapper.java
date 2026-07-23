package com.emerson.dev.usuarios.application.mapper;

import com.emerson.dev.usuarios.application.dto.user.UserRequest;
import com.emerson.dev.usuarios.application.dto.user.UserResponse;
import com.emerson.dev.usuarios.domain.model.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toDomain(UserRequest request, String passwordHash) {
        return User.createNew(request.name(), request.email(), passwordHash);
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(), user.getName(), user.getEmail(),
                user.getPhone(), user.getBio(), user.isEnabled());
    }
}
