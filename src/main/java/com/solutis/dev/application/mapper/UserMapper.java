package com.solutis.dev.application.mapper;

import com.solutis.dev.application.dto.user.UserRequest;
import com.solutis.dev.application.dto.user.UserResponse;
import com.solutis.dev.application.dto.user.UserUpsertRequest;
import com.solutis.dev.domain.model.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toDomain(UserRequest request, String passwordHash) {
        return User.createNew(request.name(), request.email(), passwordHash);
    }

    public static User toDomain(UserUpsertRequest request, String passwordHash) {
        return User.createNew(request.name(), request.email(), passwordHash);
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(), user.getName(), user.getEmail(),
                user.getPhone(), user.getBio(), user.isEnabled());
    }
}
