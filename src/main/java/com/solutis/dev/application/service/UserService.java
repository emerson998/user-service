package com.solutis.dev.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solutis.dev.application.dto.user.UserRequest;
import com.solutis.dev.application.dto.user.UserResponse;
import com.solutis.dev.application.dto.user.UserUpdateRequest;
import com.solutis.dev.application.dto.user.UserUpsertRequest;
import com.solutis.dev.application.mapper.UserMapper;
import com.solutis.dev.application.port.in.UserUseCase;
import com.solutis.dev.application.port.out.PasswordEncoderPort;
import com.solutis.dev.domain.exception.DuplicateResourceException;
import com.solutis.dev.domain.exception.ResourceNotFoundException;
import com.solutis.dev.domain.model.User;
import com.solutis.dev.domain.repository.UserRepository;

@Service
@Transactional
public class UserService implements UserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    public UserService(UserRepository userRepository, PasswordEncoderPort passwordEncoderPort) {
        this.userRepository = userRepository;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    public UserResponse create(UserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new DuplicateResourceException("Usuário com e-mail " + request.email() + " já existe");
        }
        if (userRepository.findByCpf(request.cpf()).isPresent()) {
            throw new DuplicateResourceException("Usuário com CPF " + request.cpf() + " já existe");
        }
        String passwordHash = passwordEncoderPort.encode(request.password());
        User saved = userRepository.save(UserMapper.toDomain(request, passwordHash));
        return UserMapper.toResponse(saved);
    }

    @Override
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
        user.updateProfile(request.name(), request.phone(), request.bio());
        User saved = userRepository.save(user);
        return UserMapper.toResponse(saved);
    }

    @Override
    public UserResponse upsert(UserUpsertRequest request) {
        return userRepository.findByEmail(request.email())
                .map(existing -> updateExisting(existing, request))
                .orElseGet(() -> createFromUpsert(request));
    }

    private UserResponse updateExisting(User user, UserUpsertRequest request) {
        user.updateProfile(request.name(), request.phone(), request.bio());
        if (request.password() != null && !request.password().isBlank()) {
            user.changePassword(passwordEncoderPort.encode(request.password()));
        }
        return UserMapper.toResponse(userRepository.save(user));
    }

    private UserResponse createFromUpsert(UserUpsertRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("password é obrigatório para criar um novo usuário");
        }
        if (userRepository.findByCpf(request.cpf()).isPresent()) {
            throw new DuplicateResourceException("Usuário com CPF " + request.cpf() + " já existe");
        }
        String passwordHash = passwordEncoderPort.encode(request.password());
        User saved = userRepository.save(UserMapper.toDomain(request, passwordHash));
        return UserMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> listAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário", id);
        }
        userRepository.deleteById(id);
    }
}
