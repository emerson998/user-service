package com.solutis.dev.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.solutis.dev.application.dto.user.BulkActionResult;
import com.solutis.dev.application.port.in.UserBulkNotificationUseCase;
import com.solutis.dev.domain.exception.ResourceNotFoundException;

@WebMvcTest(UserBulkNotificationController.class)
class UserBulkNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserBulkNotificationUseCase userBulkNotificationUseCase;

    @Test
    void activate_shouldReturn200WithActivatedCount_whenPayloadIsValid() throws Exception {
        when(userBulkNotificationUseCase.activate(any()))
                .thenReturn(new BulkActionResult(2, List.of(), false));

        mockMvc.perform(post("/api/v1/users/notifications/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userIds\":[1,2],\"dryRun\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activatedCount").value(2))
                .andExpect(jsonPath("$.dryRun").value(false));
    }

    @Test
    void activate_shouldReturn400_whenUserIdsIsEmpty() throws Exception {
        mockMvc.perform(post("/api/v1/users/notifications/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userIds\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void activate_shouldReturn400_whenUserIdsIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/users/notifications/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void activate_shouldReturn400_whenServiceThrowsIllegalArgumentException_forDuplicates() throws Exception {
        when(userBulkNotificationUseCase.activate(any()))
                .thenThrow(new IllegalArgumentException("userIds contém ids duplicados"));

        mockMvc.perform(post("/api/v1/users/notifications/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userIds\":[1,1]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void activate_shouldReturn404_whenServiceThrowsResourceNotFoundException() throws Exception {
        when(userBulkNotificationUseCase.activate(any()))
                .thenThrow(new ResourceNotFoundException("Usuários com ids [99] não encontrados"));

        mockMvc.perform(post("/api/v1/users/notifications/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userIds\":[99]}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void activate_shouldReturn200WithDryRunTrue_whenDryRunRequested() throws Exception {
        when(userBulkNotificationUseCase.activate(any()))
                .thenReturn(new BulkActionResult(1, List.of(), true));

        mockMvc.perform(post("/api/v1/users/notifications/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userIds\":[1],\"dryRun\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activatedCount").value(1))
                .andExpect(jsonPath("$.dryRun").value(true));
    }
}
