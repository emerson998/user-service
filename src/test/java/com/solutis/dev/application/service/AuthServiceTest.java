package com.solutis.dev.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.solutis.dev.application.dto.auth.LoginRequest;
import com.solutis.dev.application.dto.auth.LoginResponse;
import com.solutis.dev.application.port.out.TokenStorePort;
import com.solutis.dev.domain.exception.ResourceNotFoundException;
import com.solutis.dev.domain.model.User;
import com.solutis.dev.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final long TOKEN_TTL_SECONDS = 1800L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenStorePort tokenStorePort;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, tokenStorePort, TOKEN_TTL_SECONDS);
    }

    @Test
    void login_shouldReturnTokenAndExpiresAt_whenUserExists() {
        User user = new User(1L, "Alice", "alice@example.com", "12345678909", "hashed", null, null, true, null, false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tokenStorePort.issue(1L)).thenReturn("11111111-1111-1111-1111-111111111111");

        Instant before = Instant.now();
        LoginResponse response = authService.login(new LoginRequest(1L));
        Instant after = Instant.now().plusSeconds(TOKEN_TTL_SECONDS);

        assertThat(response.token()).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(response.expiresAt()).isAfterOrEqualTo(before.plusSeconds(TOKEN_TTL_SECONDS).minusSeconds(5));
        assertThat(response.expiresAt()).isBeforeOrEqualTo(after);
        verify(tokenStorePort).issue(1L);
    }

    @Test
    void login_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest(99L)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(tokenStorePort, never()).issue(anyLong());
    }
}
