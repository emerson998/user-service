package com.solutis.dev.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.solutis.dev.infrastructure.config.CacheConfig;

class TokenCleanupJobTest {

    @Test
    void cleanupExpiredTokens_shouldRemoveExpiredEntriesFromNativeCache() throws InterruptedException {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CacheConfig.AUTH_TOKENS_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(100, TimeUnit.MILLISECONDS));
        Cache cache = cacheManager.getCache(CacheConfig.AUTH_TOKENS_CACHE);
        cache.put("expired-token", 1L);
        CaffeineCache caffeineCache = (CaffeineCache) cache;
        TokenCleanupJob job = new TokenCleanupJob(cacheManager);

        Thread.sleep(300);
        job.cleanupExpiredTokens();

        assertThat(caffeineCache.getNativeCache().asMap()).isEmpty();
    }

    @Test
    void cleanupExpiredTokens_shouldDoNothing_whenAuthTokensCacheIsNotConfigured() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheNames(List.of("someOtherCache"));
        TokenCleanupJob job = new TokenCleanupJob(cacheManager);

        assertThatCode(job::cleanupExpiredTokens).doesNotThrowAnyException();
    }

    @Test
    void cleanupExpiredTokens_shouldNotPropagateException_whenCacheManagerFails() {
        CacheManager cacheManager = mock(CacheManager.class);
        when(cacheManager.getCache(CacheConfig.AUTH_TOKENS_CACHE)).thenThrow(new RuntimeException("boom"));
        TokenCleanupJob job = new TokenCleanupJob(cacheManager);

        assertThatCode(job::cleanupExpiredTokens).doesNotThrowAnyException();
    }
}
