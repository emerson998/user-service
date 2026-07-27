package com.solutis.dev.application.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solutis.dev.application.dto.auth.LoginRequest;
import com.solutis.dev.application.dto.auth.LoginResponse;
import com.solutis.dev.application.dto.auth.TokenStatusResponse;
import com.solutis.dev.application.port.in.AuthUseCase;
import com.solutis.dev.application.port.out.TokenStorePort;
import com.solutis.dev.domain.exception.InvalidTokenException;
import com.solutis.dev.domain.exception.ResourceNotFoundException;
import com.solutis.dev.domain.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class AuthService implements AuthUseCase {

    private final UserRepository userRepository;
    private final TokenStorePort tokenStorePort;
    private final long tokenTtlSeconds;

    public AuthService(UserRepository userRepository, TokenStorePort tokenStorePort,
            @Value("${app.auth.token-ttl-seconds}") long tokenTtlSeconds) {
        this.userRepository = userRepository;
        this.tokenStorePort = tokenStorePort;
        this.tokenTtlSeconds = tokenTtlSeconds;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", request.userId()));
        String token = tokenStorePort.issue(request.userId());
        return new LoginResponse(token, Instant.now().plusSeconds(tokenTtlSeconds));
    }

    @Override
    public TokenStatusResponse lookup(String token) {
        return tokenStorePort.resolve(token)
                .map(userId -> new TokenStatusResponse(true, userId))
                .orElseGet(() -> new TokenStatusResponse(false, null));
    }

    @Override
    public Long requireValidToken(String token) {
        return tokenStorePort.resolve(token)
                .orElseThrow(() -> new InvalidTokenException("Token ausente, expirado ou inválido"));
    }

    @Override
    public void logout(String token) {
        requireValidToken(token);
        tokenStorePort.invalidate(token);
    }
}
