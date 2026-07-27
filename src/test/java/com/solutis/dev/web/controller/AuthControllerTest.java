package com.solutis.dev.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.solutis.dev.application.dto.auth.LoginResponse;
import com.solutis.dev.application.port.in.AuthUseCase;
import com.solutis.dev.domain.exception.ResourceNotFoundException;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthUseCase authUseCase;

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
}
