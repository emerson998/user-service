package com.solutis.dev.domain.repository;

import java.util.List;
import java.util.Optional;

import com.solutis.dev.domain.model.User;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByCpf(String cpf);

    List<User> findAll();

    boolean existsById(Long id);
}
