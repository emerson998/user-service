package com.solutis.dev.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.solutis.dev.application.port.in.AuthUseCase;
import com.solutis.dev.domain.repository.UserRepository;
import com.solutis.dev.web.security.RoleAuthorizationInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthUseCase authUseCase;
    private final UserRepository userRepository;

    public WebMvcConfig(AuthUseCase authUseCase, UserRepository userRepository) {
        this.authUseCase = authUseCase;
        this.userRepository = userRepository;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RoleAuthorizationInterceptor(authUseCase, userRepository));
    }
}
