package com.solutis.dev.web.security;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.solutis.dev.application.port.in.AuthUseCase;
import com.solutis.dev.domain.exception.AccessDeniedException;
import com.solutis.dev.domain.exception.InvalidTokenException;
import com.solutis.dev.domain.exception.ResourceNotFoundException;
import com.solutis.dev.domain.model.User;
import com.solutis.dev.domain.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RoleAuthorizationInterceptor implements HandlerInterceptor {

    private final AuthUseCase authUseCase;
    private final UserRepository userRepository;

    public RoleAuthorizationInterceptor(AuthUseCase authUseCase, UserRepository userRepository) {
        this.authUseCase = authUseCase;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            return true;
        }

        Long userId = authUseCase.requireValidToken(extractBearerToken(request));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));
        if (user.getRole() != requireRole.value()) {
            throw new AccessDeniedException(
                    "Acesso negado: papel exigido " + requireRole.value() + ", papel atual " + user.getRole());
        }
        return true;
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new InvalidTokenException("Header Authorization deve estar no formato 'Bearer <token>'");
        }
        return header.substring("Bearer ".length());
    }
}
