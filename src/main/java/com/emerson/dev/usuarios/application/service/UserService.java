package com.emerson.dev.usuarios.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emerson.dev.usuarios.application.dto.user.UserRequest;
import com.emerson.dev.usuarios.application.dto.user.UserResponse;
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
    private final PasswordEncoderPort passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse create(UserRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(u -> {
            throw new DuplicateResourceException("User already exists with email " + request.email());
        });
        User user = UserMapper.toDomain(request, passwordEncoder);
        user.updateProfile(request.name(), request.phone(), request.bio());
        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse update(Long id, UserRequest request) {
        User user = findUserOrThrow(id);
        userRepository.findByEmail(request.email())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(u -> {
                    throw new DuplicateResourceException("User already exists with email " + request.email());
                });
        user.updateProfile(request.name(), request.phone(), request.bio());
        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return UserMapper.toResponse(findUserOrThrow(id));
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
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
