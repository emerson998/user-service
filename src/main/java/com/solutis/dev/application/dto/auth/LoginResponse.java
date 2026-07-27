package com.solutis.dev.application.dto.auth;

import java.time.Instant;

public record LoginResponse(String token, Instant expiresAt) {
}
