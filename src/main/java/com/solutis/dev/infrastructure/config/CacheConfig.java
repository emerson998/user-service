package com.solutis.dev.infrastructure.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String AUTH_TOKENS_CACHE = "authTokens";
    public static final String USERS_CACHE = "users";

    @Bean
    public CacheManager cacheManager(
            @Value("${app.auth.token-ttl-seconds}") long tokenTtlSeconds,
            @Value("${app.cache.users-ttl-seconds}") long usersTtlSeconds) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.registerCustomCache(AUTH_TOKENS_CACHE,
                Caffeine.newBuilder().expireAfterWrite(tokenTtlSeconds, TimeUnit.SECONDS).build());
        cacheManager.registerCustomCache(USERS_CACHE,
                Caffeine.newBuilder().expireAfterWrite(usersTtlSeconds, TimeUnit.SECONDS).build());
        return cacheManager;
    }
}
