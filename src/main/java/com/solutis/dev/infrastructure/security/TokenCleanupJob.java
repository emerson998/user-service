package com.solutis.dev.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.solutis.dev.infrastructure.config.CacheConfig;

@Component
public class TokenCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupJob.class);

    private final CacheManager cacheManager;

    public TokenCleanupJob(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Scheduled(fixedRateString = "${app.auth.token-cleanup-interval-ms:60000}")
    public void cleanupExpiredTokens() {
        try {
            Cache cache = cacheManager.getCache(CacheConfig.AUTH_TOKENS_CACHE);
            if (cache instanceof CaffeineCache caffeineCache) {
                caffeineCache.getNativeCache().cleanUp();
            }
        } catch (RuntimeException e) {
            log.warn("Falha ao limpar tokens expirados do cache '{}'", CacheConfig.AUTH_TOKENS_CACHE, e);
        }
    }
}
