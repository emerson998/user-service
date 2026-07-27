package com.solutis.dev.application.port.in;

import com.solutis.dev.application.dto.user.UserRequest;
import com.solutis.dev.application.dto.user.UserResponse;
import com.solutis.dev.application.dto.user.UserUpdateRequest;
import com.solutis.dev.application.dto.user.UserUpsertRequest;
import com.solutis.dev.domain.repository.PageQuery;
import com.solutis.dev.domain.repository.PageResult;
import com.solutis.dev.domain.repository.UserFilter;

public interface UserUseCase {

    UserResponse create(UserRequest request);

    UserResponse update(Long id, UserUpdateRequest request);

    UserResponse upsert(UserUpsertRequest request);

    UserResponse getById(Long id);

    PageResult<UserResponse> listAll(PageQuery pageQuery, UserFilter filter);

    void delete(Long id);
}
