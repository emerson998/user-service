package com.solutis.dev.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.solutis.dev.application.dto.auth.LoginResponse;
import com.solutis.dev.application.dto.auth.TokenStatusResponse;
import com.solutis.dev.application.port.in.AuthUseCase;
import com.solutis.dev.domain.exception.InvalidTokenException;
import com.solutis.dev.domain.exception.RateLimitExceededException;
import com.solutis.dev.domain.exception.ResourceNotFoundException;
import com.solutis.dev.domain.repository.UserRepository;
import com.solutis.dev.infrastructure.security.LoginRateLimiter;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthUseCase authUseCase;

    @MockitoBean
    private LoginRateLimiter loginRateLimiter;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void login_shouldReturn200WithToken_whenUserExists() throws Exception {
        when(authUseCase.login(any()))
                .thenReturn(new LoginResponse("11111111-1111-1111-1111-111111111111", Instant.now().plusSeconds(1800)));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("11111111-1111-1111-1111-111111111111"));
    }

    @Test
    void login_shouldReturn404_whenUserDoesNotExist() throws Exception {
        when(authUseCase.login(any()))
                .thenThrow(new ResourceNotFoundException("Usuário", 99L));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":99}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void login_shouldReturn400_whenUserIdIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn429_whenRateLimitIsExceeded() throws Exception {
        doThrow(new RateLimitExceededException("Limite de tentativas de login excedido para o IP 127.0.0.1"))
                .when(loginRateLimiter).checkAndIncrement(any());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void lookup_shouldReturn200WithValidTrue_whenTokenExists() throws Exception {
        when(authUseCase.lookup("valid-token")).thenReturn(new TokenStatusResponse(true, 1L));

        mockMvc.perform(get("/api/v1/auth/tokens/valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void lookup_shouldReturn200WithValidFalse_whenTokenDoesNotExist() throws Exception {
        when(authUseCase.lookup("unknown-token")).thenReturn(new TokenStatusResponse(false, null));

        mockMvc.perform(get("/api/v1/auth/tokens/unknown-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.userId").doesNotExist());
    }

    @Test
    void logout_shouldReturn204_whenTokenIsValid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
                .andExpect(status().isNoContent());

        verify(authUseCase).logout("valid-token");
    }

    @Test
    void logout_shouldReturn401_whenTokenIsInvalidOrExpired() throws Exception {
        doThrow(new InvalidTokenException("Token expirado")).when(authUseCase).logout("expired-token");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer expired-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldReturn401_whenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized());

        verify(authUseCase, never()).logout(any());
    }

    @Test
    void logout_shouldReturn401_whenAuthorizationHeaderIsNotBearer() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Basic abc123"))
                .andExpect(status().isUnauthorized());

        verify(authUseCase, never()).logout(any());
    }
}
