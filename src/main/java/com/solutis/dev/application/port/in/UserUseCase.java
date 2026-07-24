package com.solutis.dev.application.port.in;

import java.util.List;

import com.solutis.dev.application.dto.user.UserRequest;
import com.solutis.dev.application.dto.user.UserResponse;
import com.solutis.dev.application.dto.user.UserUpdateRequest;

public interface UserUseCase {

    UserResponse create(UserRequest request);

    UserResponse update(Long id, UserUpdateRequest request);

    UserResponse getById(Long id);

    List<UserResponse> listAll();

    void delete(Long id);
}
