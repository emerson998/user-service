package com.solutis.dev.infrastructure.security;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.solutis.dev.domain.exception.RateLimitExceededException;

@Component
public class LoginRateLimiter {

    private final Cache<String, AtomicInteger> attemptsByIp;
    private final int maxAttempts;

    public LoginRateLimiter(
            @Value("${app.auth.login-max-attempts}") int maxAttempts,
            @Value("${app.auth.login-rate-limit-window-seconds}") long windowSeconds) {
        this.maxAttempts = maxAttempts;
        this.attemptsByIp = Caffeine.newBuilder()
                .expireAfterWrite(windowSeconds, TimeUnit.SECONDS)
                .build();
    }

    public void checkAndIncrement(String ip) {
        int attempts = attemptsByIp.asMap()
                .computeIfAbsent(ip, key -> new AtomicInteger())
                .incrementAndGet();
        if (attempts > maxAttempts) {
            throw new RateLimitExceededException(
                    "Limite de tentativas de login excedido para o IP " + ip);
        }
    }
}
