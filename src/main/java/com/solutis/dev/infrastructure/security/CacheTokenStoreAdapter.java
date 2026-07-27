package com.solutis.dev.infrastructure.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import com.solutis.dev.application.port.out.TokenStorePort;
import com.solutis.dev.infrastructure.config.CacheConfig;

@Component
public class CacheTokenStoreAdapter implements TokenStorePort {

    private final CacheManager cacheManager;

    public CacheTokenStoreAdapter(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public String issue(Long userId) {
        String token = UUID.randomUUID().toString();
        cache().put(token, userId);
        return token;
    }

    @Override
    public Optional<Long> resolve(String token) {
        Cache.ValueWrapper wrapper = cache().get(token);
        return Optional.ofNullable(wrapper).map(w -> (Long) w.get());
    }

    @Override
    public void invalidate(String token) {
        cache().evict(token);
    }

    private Cache cache() {
        Cache cache = cacheManager.getCache(CacheConfig.AUTH_TOKENS_CACHE);
        if (cache == null) {
            throw new IllegalStateException("Cache '" + CacheConfig.AUTH_TOKENS_CACHE + "' não configurado");
        }
        return cache;
    }
}
