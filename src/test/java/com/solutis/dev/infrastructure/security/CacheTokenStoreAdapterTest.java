package com.solutis.dev.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.solutis.dev.infrastructure.config.CacheConfig;

class CacheTokenStoreAdapterTest {

    private CacheTokenStoreAdapter tokenStoreAdapter;

    @BeforeEach
    void setUp() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CacheConfig.AUTH_TOKENS_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS));
        tokenStoreAdapter = new CacheTokenStoreAdapter(cacheManager);
    }

    @Test
    void issue_shouldReturnUniqueTokenResolvableToTheGivenUserId() {
        String token = tokenStoreAdapter.issue(1L);

        assertThat(token).isNotBlank();
        assertThat(tokenStoreAdapter.resolve(token)).contains(1L);
    }

    @Test
    void issue_shouldReturnDifferentTokens_forDifferentCalls() {
        String tokenA = tokenStoreAdapter.issue(1L);
        String tokenB = tokenStoreAdapter.issue(1L);

        assertThat(tokenA).isNotEqualTo(tokenB);
    }

    @Test
    void resolve_shouldReturnEmpty_whenTokenDoesNotExist() {
        assertThat(tokenStoreAdapter.resolve("unknown-token")).isEmpty();
    }

    @Test
    void invalidate_shouldMakeTokenUnresolvable() {
        String token = tokenStoreAdapter.issue(1L);

        tokenStoreAdapter.invalidate(token);

        assertThat(tokenStoreAdapter.resolve(token)).isEmpty();
    }

    @Test
    void resolve_shouldThrowIllegalStateException_whenAuthTokensCacheIsNotConfigured() {
        CaffeineCacheManager restrictedCacheManager = new CaffeineCacheManager();
        restrictedCacheManager.setCacheNames(List.of("someOtherCache"));
        CacheTokenStoreAdapter adapterWithoutCache = new CacheTokenStoreAdapter(restrictedCacheManager);

        assertThatThrownBy(() -> adapterWithoutCache.resolve("any-token"))
                .isInstanceOf(IllegalStateException.class);
    }
}
