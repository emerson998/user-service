package com.solutis.dev.application.dto.user;

public record UserResponse(
        Long id, String name, String email, String cpf, String phone, String bio, boolean enabled) {
}
