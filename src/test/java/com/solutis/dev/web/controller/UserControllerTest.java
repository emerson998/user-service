package com.solutis.dev.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.solutis.dev.application.dto.user.UserRequest;
import com.solutis.dev.application.dto.user.UserResponse;
import com.solutis.dev.application.dto.user.UserUpsertRequest;
import com.solutis.dev.application.port.in.UserUseCase;
import com.solutis.dev.domain.exception.DuplicateResourceException;
import com.solutis.dev.domain.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserUseCase userUseCase;

    @Test
    void create_shouldReturn201_whenRequestIsValid() throws Exception {
        UserRequest request = new UserRequest("Alice", "alice@example.com", "12345678909", "password123", null, null);
        UserResponse response = new UserResponse(1L, "Alice", "alice@example.com", "12345678909", null, null, true, false);
        when(userUseCase.create(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/users/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.cpf").value("12345678909"))
                .andExpect(jsonPath("$.notificationsEnabled").value(false));
    }

    @Test
    void create_shouldReturn400_whenNameIsBlank() throws Exception {
        UserRequest request = new UserRequest("", "alice@example.com", "12345678909", "password123", null, null);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenCpfIsMalformed() throws Exception {
        UserRequest request = new UserRequest("Alice", "alice@example.com", "123", "password123", null, null);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn409_whenEmailAlreadyExists() throws Exception {
        UserRequest request = new UserRequest("Alice", "alice@example.com", "12345678909", "password123", null, null);
        when(userUseCase.create(any(UserRequest.class)))
                .thenThrow(new DuplicateResourceException("Usuário com e-mail alice@example.com já existe"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void create_shouldReturn409_whenCpfAlreadyExists() throws Exception {
        UserRequest request = new UserRequest("Alice", "alice@example.com", "12345678909", "password123", null, null);
        when(userUseCase.create(any(UserRequest.class)))
                .thenThrow(new DuplicateResourceException("Usuário com CPF 12345678909 já existe"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void upsert_shouldReturn200_whenRequestIsValid() throws Exception {
        UserUpsertRequest request = new UserUpsertRequest(
                "Alice", "alice@example.com", "12345678909", "password123", null, null);
        UserResponse response = new UserResponse(1L, "Alice", "alice@example.com", "12345678909", null, null, true, false);
        when(userUseCase.upsert(any(UserUpsertRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/users/upsert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.cpf").value("12345678909"));
    }

    @Test
    void upsert_shouldReturn400_whenNameIsBlank() throws Exception {
        UserUpsertRequest request = new UserUpsertRequest("", "alice@example.com", "12345678909", "password123", null, null);

        mockMvc.perform(put("/api/v1/users/upsert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upsert_shouldReturn400_whenEmailIsMalformed() throws Exception {
        UserUpsertRequest request = new UserUpsertRequest("Alice", "not-an-email", "12345678909", "password123", null, null);

        mockMvc.perform(put("/api/v1/users/upsert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upsert_shouldReturn400_whenCpfIsMalformed() throws Exception {
        UserUpsertRequest request = new UserUpsertRequest("Alice", "alice@example.com", "123", "password123", null, null);

        mockMvc.perform(put("/api/v1/users/upsert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upsert_shouldReturn400_whenPasswordMissingOnCreate() throws Exception {
        UserUpsertRequest request = new UserUpsertRequest("Alice", "alice@example.com", "12345678909", null, null, null);
        when(userUseCase.upsert(any(UserUpsertRequest.class)))
                .thenThrow(new IllegalArgumentException("password é obrigatório para criar um novo usuário"));

        mockMvc.perform(put("/api/v1/users/upsert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upsert_shouldReturn409_whenCreatingWithCpfAlreadyTaken() throws Exception {
        UserUpsertRequest request = new UserUpsertRequest(
                "Alice", "alice@example.com", "12345678909", "password123", null, null);
        when(userUseCase.upsert(any(UserUpsertRequest.class)))
                .thenThrow(new DuplicateResourceException("Usuário com CPF 12345678909 já existe"));

        mockMvc.perform(put("/api/v1/users/upsert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getById_shouldReturn404_whenUserDoesNotExist() throws Exception {
        when(userUseCase.getById(99L)).thenThrow(new ResourceNotFoundException("Usuário", 99L));

        mockMvc.perform(get("/api/v1/users/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void listAll_shouldReturn200WithUserList() throws Exception {
        UserResponse response = new UserResponse(1L, "Alice", "alice@example.com", "12345678909", null, null, true, false);
        when(userUseCase.listAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userUseCase).delete(eq(1L));
    }
}
