package com.solutis.dev.application.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpsertRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        String password,
        String phone,
        @Size(max = 500) String bio) {
}
