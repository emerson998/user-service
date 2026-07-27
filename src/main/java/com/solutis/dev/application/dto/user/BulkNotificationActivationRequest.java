package com.solutis.dev.application.dto.user;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record BulkNotificationActivationRequest(
        @NotEmpty(message = "userIds não pode ser vazio") List<Long> userIds,
        Boolean dryRun) {

    public boolean isDryRun() {
        return Boolean.TRUE.equals(dryRun);
    }
}
