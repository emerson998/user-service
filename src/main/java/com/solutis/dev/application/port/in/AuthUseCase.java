package com.solutis.dev.application.port.in;

import com.solutis.dev.application.dto.auth.LoginRequest;
import com.solutis.dev.application.dto.auth.LoginResponse;
import com.solutis.dev.application.dto.auth.TokenStatusResponse;

public interface AuthUseCase {

    LoginResponse login(LoginRequest request);

    TokenStatusResponse lookup(String token);

    Long requireValidToken(String token);

    void logout(String token);
}
