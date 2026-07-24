package com.emerson.dev.usuarios.infrastructure.persistence.mapper;

import com.emerson.dev.usuarios.domain.model.User;
import com.emerson.dev.usuarios.infrastructure.persistence.entity.UserJpaEntity;

public final class UserEntityMapper {

    private UserEntityMapper() {
    }

    public static UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(user.getId(), user.getName(), user.getEmail(), user.getPasswordHash(),
                user.getPhone(), user.getBio(), user.isEnabled(), user.getSaldo());
    }

    public static User toDomain(UserJpaEntity entity) {
        return new User(entity.getId(), entity.getName(), entity.getEmail(), entity.getPasswordHash(),
                entity.getPhone(), entity.getBio(), entity.isEnabled(), entity.getSaldo());
    }
}
