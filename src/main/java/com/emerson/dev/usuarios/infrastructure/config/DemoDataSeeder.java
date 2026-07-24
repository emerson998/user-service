package com.emerson.dev.usuarios.infrastructure.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.emerson.dev.usuarios.application.dto.user.UserRequest;
import com.emerson.dev.usuarios.application.port.in.UserUseCase;
import com.emerson.dev.usuarios.domain.repository.UserRepository;

@Component
public class DemoDataSeeder implements CommandLineRunner {

    private final UserUseCase userUseCase;
    private final UserRepository userRepository;

    public DemoDataSeeder(UserUseCase userUseCase, UserRepository userRepository) {
        this.userUseCase = userUseCase;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findAll().isEmpty()) {
            userUseCase.create(new UserRequest("Ana Teste", "ana.teste@example.com", "senha123",
                    "11999990001", "Usuario de teste PIX"));
            userUseCase.create(new UserRequest("Bruno Teste", "bruno.teste@example.com", "senha123",
                    "11999990002", "Usuario de teste PIX"));
        }
    }
}
