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
import com.solutis.dev.application.port.in.AuthUseCase;
import com.solutis.dev.application.port.in.UserUseCase;
import com.solutis.dev.domain.exception.DuplicateResourceException;
import com.solutis.dev.domain.exception.ResourceNotFoundException;
import com.solutis.dev.domain.model.Role;
import com.solutis.dev.domain.repository.PageQuery;
import com.solutis.dev.domain.repository.PageResult;
import com.solutis.dev.domain.repository.UserFilter;
import com.solutis.dev.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserUseCase userUseCase;

    @MockitoBean
    private AuthUseCase authUseCase;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void create_shouldReturn201_whenRequestIsValid() throws Exception {
        UserRequest request = new UserRequest("Alice", "alice@example.com", "12345678909", "password123", null, null);
        UserResponse response = new UserResponse(1L, "Alice", "alice@example.com", "12345678909", null, null, true, false, Role.USER);
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
        UserResponse response = new UserResponse(1L, "Alice", "alice@example.com", "12345678909", null, null, true, false, Role.USER);
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
    void listAll_shouldReturn200WithPagedUserList_whenNoParamsGiven() throws Exception {
        UserResponse response = new UserResponse(1L, "Alice", "alice@example.com", "12345678909", null, null, true, false, Role.USER);
        when(userUseCase.listAll(new PageQuery(0, 20), new UserFilter(null, null, null)))
                .thenReturn(new PageResult<>(List.of(response), 1, 1));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void listAll_shouldPassPageAndSizeParams_whenProvided() throws Exception {
        when(userUseCase.listAll(new PageQuery(1, 5), new UserFilter(null, null, null)))
                .thenReturn(new PageResult<>(List.of(), 0, 0));

        mockMvc.perform(get("/api/v1/users").param("page", "1").param("size", "5"))
                .andExpect(status().isOk());

        verify(userUseCase).listAll(new PageQuery(1, 5), new UserFilter(null, null, null));
    }

    @Test
    void listAll_shouldPassNameFilter_whenNameParamProvided() throws Exception {
        when(userUseCase.listAll(new PageQuery(0, 20), new UserFilter("jo", null, null)))
                .thenReturn(new PageResult<>(List.of(), 0, 0));

        mockMvc.perform(get("/api/v1/users").param("name", "jo"))
                .andExpect(status().isOk());

        verify(userUseCase).listAll(new PageQuery(0, 20), new UserFilter("jo", null, null));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userUseCase).delete(eq(1L));
    }
}
