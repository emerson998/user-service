package com.solutis.dev.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.solutis.dev.domain.exception.ResourceNotFoundException;
import com.solutis.dev.domain.model.User;
import com.solutis.dev.domain.repository.PageQuery;
import com.solutis.dev.domain.repository.PageResult;
import com.solutis.dev.domain.repository.UserFilter;
import com.solutis.dev.domain.repository.UserRepository;
import com.solutis.dev.infrastructure.config.CacheConfig;
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
    @CachePut(cacheNames = CacheConfig.USERS_CACHE, key = "#result.id")
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
    @Cacheable(cacheNames = CacheConfig.USERS_CACHE, key = "#id")
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
    public PageResult<User> findAll(PageQuery pageQuery, UserFilter filter) {
        Pageable pageable = PageRequest.of(pageQuery.page(), pageQuery.size());
        Page<UserJpaEntity> page = jpaRepository.findAll(buildSpecification(filter), pageable);
        List<User> content = page.getContent().stream().map(UserEntityMapper::toDomain).toList();
        return new PageResult<>(content, page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsByIdAndDeletedAtIsNull(id);
    }

    private Specification<UserJpaEntity> buildSpecification(UserFilter filter) {
        Specification<UserJpaEntity> spec = (root, query, cb) -> cb.isNull(root.get("deletedAt"));
        if (filter.name() != null && !filter.name().isBlank()) {
            String pattern = "%" + filter.name().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern));
        }
        if (filter.email() != null && !filter.email().isBlank()) {
            String pattern = "%" + filter.email().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("email")), pattern));
        }
        if (filter.enabled() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("enabled"), filter.enabled()));
        }
        return spec;
    }
}
