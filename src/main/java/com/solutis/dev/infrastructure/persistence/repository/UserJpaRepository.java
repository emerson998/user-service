package com.solutis.dev.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.solutis.dev.infrastructure.persistence.entity.UserJpaEntity;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long>,
        JpaSpecificationExecutor<UserJpaEntity> {

    Optional<UserJpaEntity> findByIdAndDeletedAtIsNull(Long id);

    Optional<UserJpaEntity> findByEmailAndDeletedAtIsNull(String email);

    Optional<UserJpaEntity> findByCpfAndDeletedAtIsNull(String cpf);

    List<UserJpaEntity> findAllByDeletedAtIsNull();

    boolean existsByIdAndDeletedAtIsNull(Long id);
}
