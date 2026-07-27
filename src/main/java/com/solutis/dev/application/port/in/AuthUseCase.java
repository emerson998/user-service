package com.solutis.dev.application.port.in;

import com.solutis.dev.application.dto.auth.LoginRequest;
import com.solutis.dev.application.dto.auth.LoginResponse;

public interface AuthUseCase {

    LoginResponse login(LoginRequest request);
}
