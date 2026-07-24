package com.emerson.dev.usuarios.domain.repository;

import java.util.List;
import java.util.Optional;

import com.emerson.dev.usuarios.domain.model.User;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);
}
