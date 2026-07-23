package com.emerson.dev.usuarios.application.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank String name,
        String phone,
        @Size(max = 500) String bio) {
}
