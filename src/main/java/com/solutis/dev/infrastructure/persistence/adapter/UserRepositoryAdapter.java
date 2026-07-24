package com.solutis.dev.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.solutis.dev.domain.exception.ResourceNotFoundException;
import com.solutis.dev.domain.model.User;
import com.solutis.dev.domain.repository.UserRepository;
import com.solutis.dev.infrastructure.persistence.entity.UserJpaEntity;
import com.solutis.dev.infrastructure.persistence.mapper.UserEntityMapper;
import com.solutis.dev.infrastructure.persistence.repository.UserJpaRepository;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = user.getId() == null
                ? UserEntityMapper.toEntity(user)
                : loadManagedEntityWithCurrentVersion(user);
        return UserEntityMapper.toDomain(jpaRepository.save(entity));
    }

    private UserJpaEntity loadManagedEntityWithCurrentVersion(User user) {
        UserJpaEntity managedEntity = jpaRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", user.getId()));
        UserEntityMapper.copyMutableFieldsTo(user, managedEntity);
        return managedEntity;
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findByIdAndDeletedAtIsNull(id).map(UserEntityMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmailAndDeletedAtIsNull(email).map(UserEntityMapper::toDomain);
    }

    @Override
    public Optional<User> findByCpf(String cpf) {
        return jpaRepository.findByCpfAndDeletedAtIsNull(cpf).map(UserEntityMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAllByDeletedAtIsNull().stream().map(UserEntityMapper::toDomain).toList();
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsByIdAndDeletedAtIsNull(id);
    }
}
