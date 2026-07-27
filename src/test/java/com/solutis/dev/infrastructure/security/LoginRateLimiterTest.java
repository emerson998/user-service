package com.solutis.dev.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.solutis.dev.domain.exception.RateLimitExceededException;

class LoginRateLimiterTest {

    @Test
    void checkAndIncrement_shouldAllowUpToMaxAttempts() {
        LoginRateLimiter rateLimiter = new LoginRateLimiter(3, 60);

        assertThatCode(() -> {
            rateLimiter.checkAndIncrement("127.0.0.1");
            rateLimiter.checkAndIncrement("127.0.0.1");
            rateLimiter.checkAndIncrement("127.0.0.1");
        }).doesNotThrowAnyException();
    }

    @Test
    void checkAndIncrement_shouldThrowRateLimitExceededException_afterMaxAttemptsExceeded() {
        LoginRateLimiter rateLimiter = new LoginRateLimiter(3, 60);
        rateLimiter.checkAndIncrement("127.0.0.1");
        rateLimiter.checkAndIncrement("127.0.0.1");
        rateLimiter.checkAndIncrement("127.0.0.1");

        assertThatThrownBy(() -> rateLimiter.checkAndIncrement("127.0.0.1"))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void checkAndIncrement_shouldTrackAttemptsPerIpIndependently() {
        LoginRateLimiter rateLimiter = new LoginRateLimiter(1, 60);
        rateLimiter.checkAndIncrement("127.0.0.1");

        assertThatCode(() -> rateLimiter.checkAndIncrement("10.0.0.1")).doesNotThrowAnyException();
        assertThatThrownBy(() -> rateLimiter.checkAndIncrement("127.0.0.1"))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void checkAndIncrement_shouldAllowAttemptsAgain_afterWindowElapses() throws InterruptedException {
        LoginRateLimiter rateLimiter = new LoginRateLimiter(1, 0);
        rateLimiter.checkAndIncrement("127.0.0.1");

        Thread.sleep(50);

        assertThatCode(() -> rateLimiter.checkAndIncrement("127.0.0.1")).doesNotThrowAnyException();
    }
}
