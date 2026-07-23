package com.emerson.dev.usuarios.application.port.in;

import java.util.List;

import com.emerson.dev.usuarios.application.dto.user.UserRequest;
import com.emerson.dev.usuarios.application.dto.user.UserResponse;
import com.emerson.dev.usuarios.application.dto.user.UserUpdateRequest;

public interface UserUseCase {

    UserResponse create(UserRequest request);

    UserResponse update(Long id, UserUpdateRequest request);

    UserResponse getById(Long id);

    List<UserResponse> listAll();

    void delete(Long id);
}
