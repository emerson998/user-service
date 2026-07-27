package com.solutis.dev.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solutis.dev.application.dto.auth.LoginRequest;
import com.solutis.dev.application.dto.auth.LoginResponse;
import com.solutis.dev.application.dto.auth.TokenStatusResponse;
import com.solutis.dev.application.port.in.AuthUseCase;
import com.solutis.dev.domain.exception.InvalidTokenException;
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
    @Operation(summary = "Login básico por id de usuário, devolve um token UUID",
            description = "Login simplificado: recebe apenas o id do usuário, sem senha. "
                    + "Não valida password/passwordHash — não é autenticação real por credenciais.")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authUseCase.login(request));
    }

    @GetMapping("/tokens/{token}")
    @Operation(summary = "Consulta o status de um token UUID")
    public ResponseEntity<TokenStatusResponse> lookup(@PathVariable String token) {
        return ResponseEntity.ok(authUseCase.lookup(token));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoga um token ativo",
            description = "Recebe o token via header 'Authorization: Bearer <token>'.")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        authUseCase.logout(extractBearerToken(authorizationHeader));
        return ResponseEntity.noContent().build();
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Header Authorization deve estar no formato 'Bearer <token>'");
        }
        return authorizationHeader.substring("Bearer ".length());
    }
}
