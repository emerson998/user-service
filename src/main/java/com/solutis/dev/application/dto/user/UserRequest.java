package com.solutis.dev.application.dto.user;

import org.hibernate.validator.constraints.br.CPF;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @CPF String cpf,
        @NotBlank @Size(min = 8) String password,
        String phone,
        @Size(max = 500) String bio) {
}
