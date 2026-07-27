package com.solutis.dev.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solutis.dev.application.dto.auth.LoginRequest;
import com.solutis.dev.application.dto.auth.LoginResponse;
import com.solutis.dev.application.dto.auth.TokenStatusResponse;
import com.solutis.dev.application.port.in.AuthUseCase;
import com.solutis.dev.web.ApiRoutes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiRoutes.V1 + "/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/login")
    @Operation(summary = "Login básico por id de usuário, devolve um token UUID")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authUseCase.login(request));
    }

    @GetMapping("/tokens/{token}")
    @Operation(summary = "Consulta o status de um token UUID")
    public ResponseEntity<TokenStatusResponse> lookup(@PathVariable String token) {
        return ResponseEntity.ok(authUseCase.lookup(token));
    }
}
