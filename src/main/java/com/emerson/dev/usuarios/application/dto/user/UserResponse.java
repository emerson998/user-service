package com.emerson.dev.usuarios.application.dto.user;

import java.math.BigDecimal;

public record UserResponse(Long id, String name, String email, String phone, String bio, boolean enabled,
        BigDecimal saldo) {
}
