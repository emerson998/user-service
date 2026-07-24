package com.solutis.dev.infrastructure.persistence.mapper;

import com.solutis.dev.domain.model.User;
import com.solutis.dev.infrastructure.persistence.entity.UserJpaEntity;

public final class UserEntityMapper {

    private UserEntityMapper() {
    }

    public static UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.getId(), user.getName(), user.getEmail(), user.getCpf(), user.getPasswordHash(),
                user.getPhone(), user.getBio(), user.isEnabled());
    }

    public static User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(), entity.getName(), entity.getEmail(), entity.getCpf(), entity.getPasswordHash(),
                entity.getPhone(), entity.getBio(), entity.isEnabled());
    }
}
