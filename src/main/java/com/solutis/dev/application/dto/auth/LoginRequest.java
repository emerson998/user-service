package com.solutis.dev.application.dto.auth;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(@NotNull Long userId) {
}
