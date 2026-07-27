package com.solutis.dev.application.dto.user;

import com.solutis.dev.domain.model.Role;

public record UserResponse(
        Long id, String name, String email, String cpf, String phone, String bio, boolean enabled,
        boolean notificationsEnabled, Role role) {
}
