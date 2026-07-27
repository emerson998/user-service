package com.solutis.dev.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.solutis.dev.domain.model.User;
import com.solutis.dev.domain.repository.UserRepository;
import com.solutis.dev.infrastructure.config.CacheConfig;

@SpringBootTest
class UserRepositoryAdapterCacheTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    private Cache usersCache() {
        return cacheManager.getCache(CacheConfig.USERS_CACHE);
    }

    @Test
    void findById_shouldPopulateUsersCache_afterFirstCall() {
        User saved = userRepository.save(
                User.createNew("Alice", "alice-cache@example.com", "11111111111", "hashed"));
        usersCache().evict(saved.getId());
        assertThat(usersCache().get(saved.getId())).isNull();

        userRepository.findById(saved.getId());

        assertThat(usersCache().get(saved.getId())).isNotNull();
    }

    @Test
    void save_shouldUpdateUsersCache_viaCachePut() {
        User saved = userRepository.save(
                User.createNew("Bob", "bob-cache@example.com", "22222222222", "hashed"));

        Cache.ValueWrapper cached = usersCache().get(saved.getId());
        assertThat(cached).isNotNull();
        assertThat(((User) cached.get()).getName()).isEqualTo("Bob");

        saved.updateProfile("Bob Updated", null, null);
        userRepository.save(saved);

        Cache.ValueWrapper updatedCached = usersCache().get(saved.getId());
        assertThat(((User) updatedCached.get()).getName()).isEqualTo("Bob Updated");
    }
}
