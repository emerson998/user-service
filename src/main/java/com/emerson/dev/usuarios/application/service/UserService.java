package com.emerson.dev.usuarios.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emerson.dev.usuarios.application.dto.user.UserRequest;
import com.emerson.dev.usuarios.application.dto.user.UserResponse;
import com.emerson.dev.usuarios.application.dto.user.UserUpdateRequest;
import com.emerson.dev.usuarios.application.mapper.UserMapper;
import com.emerson.dev.usuarios.application.port.in.UserUseCase;
import com.emerson.dev.usuarios.application.port.out.PasswordEncoderPort;
import com.emerson.dev.usuarios.domain.exception.DuplicateResourceException;
import com.emerson.dev.usuarios.domain.exception.ResourceNotFoundException;
import com.emerson.dev.usuarios.domain.model.User;
import com.emerson.dev.usuarios.domain.repository.UserRepository;

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
