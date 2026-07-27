package com.solutis.dev.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.solutis.dev.application.dto.auth.LoginRequest;
import com.solutis.dev.application.dto.auth.LoginResponse;
import com.solutis.dev.application.dto.auth.TokenStatusResponse;
import com.solutis.dev.application.port.out.TokenStorePort;
import com.solutis.dev.domain.exception.InvalidTokenException;
import com.solutis.dev.domain.exception.ResourceNotFoundException;
import com.solutis.dev.domain.model.Role;
import com.solutis.dev.domain.model.User;
import com.solutis.dev.domain.repository.UserRepository;
import com.solutis.dev.infrastructure.config.CacheConfig;
import com.solutis.dev.infrastructure.security.CacheTokenStoreAdapter;

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
        User user = new User(1L, "Alice", "alice@example.com", "12345678909", "hashed", null, null, true, null, false, Role.USER);
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

    @Test
    void lookup_shouldReturnValidTrueWithUserId_whenTokenExists() {
        when(tokenStorePort.resolve("valid-token")).thenReturn(Optional.of(1L));

        TokenStatusResponse response = authService.lookup("valid-token");

        assertThat(response.valid()).isTrue();
        assertThat(response.userId()).isEqualTo(1L);
    }

    @Test
    void lookup_shouldReturnValidFalseWithNullUserId_whenTokenDoesNotExist() {
        when(tokenStorePort.resolve("unknown-token")).thenReturn(Optional.empty());

        TokenStatusResponse response = authService.lookup("unknown-token");

        assertThat(response.valid()).isFalse();
        assertThat(response.userId()).isNull();
    }

    @Test
    void requireValidToken_shouldReturnUserId_whenTokenIsValid() {
        when(tokenStorePort.resolve("valid-token")).thenReturn(Optional.of(1L));

        Long userId = authService.requireValidToken("valid-token");

        assertThat(userId).isEqualTo(1L);
    }

    @Test
    void requireValidToken_shouldThrowInvalidTokenException_whenTokenIsMissingOrExpired() {
        when(tokenStorePort.resolve("unknown-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.requireValidToken("unknown-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void requireValidToken_shouldThrowInvalidTokenException_afterTokenTtlElapses() throws InterruptedException {
        long shortTtlSeconds = 0L;
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CacheConfig.AUTH_TOKENS_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(200, TimeUnit.MILLISECONDS));
        CacheTokenStoreAdapter realTokenStore = new CacheTokenStoreAdapter(cacheManager);
        AuthService authServiceWithRealCache = new AuthService(userRepository, realTokenStore, shortTtlSeconds);
        User user = new User(1L, "Alice", "alice@example.com", "12345678909", "hashed", null, null, true, null, false, Role.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        LoginResponse loginResponse = authServiceWithRealCache.login(new LoginRequest(1L));
        assertThat(authServiceWithRealCache.requireValidToken(loginResponse.token())).isEqualTo(1L);

        Thread.sleep(500);

        assertThatThrownBy(() -> authServiceWithRealCache.requireValidToken(loginResponse.token()))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void logout_shouldInvalidateToken_whenTokenIsValid() {
        when(tokenStorePort.resolve("valid-token")).thenReturn(Optional.of(1L));

        authService.logout("valid-token");

        verify(tokenStorePort, times(1)).resolve("valid-token");
        verify(tokenStorePort).invalidate("valid-token");
    }

    @Test
    void logout_shouldThrowInvalidTokenException_whenTokenIsMissingOrExpired() {
        when(tokenStorePort.resolve("unknown-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.logout("unknown-token"))
                .isInstanceOf(InvalidTokenException.class);

        verify(tokenStorePort, never()).invalidate(anyString());
    }
}
