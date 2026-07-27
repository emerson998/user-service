package com.solutis.dev.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.solutis.dev.application.dto.user.BulkActionResult;
import com.solutis.dev.application.port.in.AuthUseCase;
import com.solutis.dev.application.port.in.UserBulkNotificationUseCase;
import com.solutis.dev.domain.exception.ResourceNotFoundException;
import com.solutis.dev.domain.model.Role;
import com.solutis.dev.domain.model.User;
import com.solutis.dev.domain.repository.UserRepository;

@WebMvcTest(UserBulkNotificationController.class)
class UserBulkNotificationControllerTest {

    private static final String ADMIN_TOKEN = "admin-token";
    private static final String USER_TOKEN = "user-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserBulkNotificationUseCase userBulkNotificationUseCase;

    @MockitoBean
    private AuthUseCase authUseCase;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUpAdminToken() {
        User admin = new User(1L, "Admin", "admin@example.com", "12345678909", "hashed",
                null, null, true, null, false, Role.ADMIN);
        when(authUseCase.requireValidToken(ADMIN_TOKEN)).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
    }

    private MockHttpServletRequestBuilder activateRequest(String body) {
        return post("/api/v1/users/notifications/activate")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }

    @Test
    void activate_shouldReturn200WithActivatedCount_whenPayloadIsValid() throws Exception {
        when(userBulkNotificationUseCase.activate(any()))
                .thenReturn(new BulkActionResult(2, List.of(), false));

        mockMvc.perform(activateRequest("{\"userIds\":[1,2],\"dryRun\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activatedCount").value(2))
                .andExpect(jsonPath("$.dryRun").value(false));
    }

    @Test
    void activate_shouldReturn400_whenUserIdsIsEmpty() throws Exception {
        mockMvc.perform(activateRequest("{\"userIds\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void activate_shouldReturn400_whenUserIdsIsMissing() throws Exception {
        mockMvc.perform(activateRequest("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void activate_shouldReturn400_whenServiceThrowsIllegalArgumentException_forDuplicates() throws Exception {
        when(userBulkNotificationUseCase.activate(any()))
                .thenThrow(new IllegalArgumentException("userIds contém ids duplicados"));

        mockMvc.perform(activateRequest("{\"userIds\":[1,1]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void activate_shouldReturn404_whenServiceThrowsResourceNotFoundException() throws Exception {
        when(userBulkNotificationUseCase.activate(any()))
                .thenThrow(new ResourceNotFoundException("Usuários com ids [99] não encontrados"));

        mockMvc.perform(activateRequest("{\"userIds\":[99]}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void activate_shouldReturn200WithDryRunTrue_whenDryRunRequested() throws Exception {
        when(userBulkNotificationUseCase.activate(any()))
                .thenReturn(new BulkActionResult(1, List.of(), true));

        mockMvc.perform(activateRequest("{\"userIds\":[1],\"dryRun\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activatedCount").value(1))
                .andExpect(jsonPath("$.dryRun").value(true));
    }

    @Test
    void activate_shouldReturn403_whenTokenBelongsToUserRoleNotAdmin() throws Exception {
        User regularUser = new User(2L, "Bob", "bob@example.com", "98765432100", "hashed",
                null, null, true, null, false, Role.USER);
        when(authUseCase.requireValidToken(USER_TOKEN)).thenReturn(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));

        mockMvc.perform(post("/api/v1/users/notifications/activate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userIds\":[1,2]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void activate_shouldReturn401_whenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/users/notifications/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userIds\":[1,2]}"))
                .andExpect(status().isUnauthorized());
    }
}
