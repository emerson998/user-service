package com.solutis.dev.web.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.solutis.dev.application.dto.user.UserRequest;
import com.solutis.dev.application.dto.user.UserResponse;
import com.solutis.dev.application.dto.user.UserUpdateRequest;
import com.solutis.dev.application.dto.user.UserUpsertRequest;
import com.solutis.dev.application.port.in.UserUseCase;
import com.solutis.dev.domain.repository.PageQuery;
import com.solutis.dev.domain.repository.PageResult;
import com.solutis.dev.domain.repository.UserFilter;
import com.solutis.dev.web.ApiRoutes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiRoutes.V1 + "/users")
@Tag(name = "Users")
public class UserController {

    private final UserUseCase userUseCase;

    public UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @PostMapping
    @Operation(summary = "Cria um novo usuário")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        UserResponse created = userUseCase.create(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + created.id())).body(created);
    }

    @GetMapping
    @Operation(summary = "Lista usuários paginados, com filtros opcionais de nome/e-mail/status")
    public ResponseEntity<PageResult<UserResponse>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean enabled) {
        PageQuery pageQuery = new PageQuery(page, size);
        UserFilter filter = new UserFilter(name, email, enabled);
        return ResponseEntity.ok(userUseCase.listAll(pageQuery, filter));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um usuário por id")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userUseCase.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza o perfil de um usuário")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userUseCase.update(id, request));
    }

    @PutMapping("/upsert")
    @Operation(summary = "Cria ou atualiza um usuário (upsert por e-mail)")
    public ResponseEntity<UserResponse> upsert(@Valid @RequestBody UserUpsertRequest request) {
        return ResponseEntity.ok(userUseCase.upsert(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um usuário")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
