package com.solutis.dev.application.dto.auth;

public record TokenStatusResponse(boolean valid, Long userId) {
}
