package com.solutis.dev.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solutis.dev.application.dto.user.BulkActionResult;
import com.solutis.dev.application.dto.user.BulkNotificationActivationRequest;
import com.solutis.dev.application.port.in.UserBulkNotificationUseCase;
import com.solutis.dev.domain.model.Role;
import com.solutis.dev.web.ApiRoutes;
import com.solutis.dev.web.security.RequireRole;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiRoutes.V1 + "/users/notifications")
@Tag(name = "Users Notifications")
public class UserBulkNotificationController {

    private final UserBulkNotificationUseCase userBulkNotificationUseCase;

    public UserBulkNotificationController(UserBulkNotificationUseCase userBulkNotificationUseCase) {
        this.userBulkNotificationUseCase = userBulkNotificationUseCase;
    }

    @PostMapping("/activate")
    @Operation(summary = "Ativa a flag de notificações em lote",
            description = "Requer token de usuário com papel ADMIN (header 'Authorization: Bearer <token>').")
    @RequireRole(Role.ADMIN)
    public ResponseEntity<BulkActionResult> activate(@Valid @RequestBody BulkNotificationActivationRequest request) {
        return ResponseEntity.ok(userBulkNotificationUseCase.activate(request));
    }
}
