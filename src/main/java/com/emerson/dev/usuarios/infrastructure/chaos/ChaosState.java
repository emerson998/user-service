package com.emerson.dev.usuarios.infrastructure.chaos;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

@Component
public class ChaosState {

    private final AtomicReference<Long> lastCorruptedUserId = new AtomicReference<>();
    private final AtomicReference<Instant> lastTriggeredAt = new AtomicReference<>();
    private final AtomicInteger triggerCount = new AtomicInteger(0);
    private final AtomicReference<Instant> lastResolvedAt = new AtomicReference<>();
    private final AtomicInteger resolvedCount = new AtomicInteger(0);

    public void recordTrigger(Long corruptedUserId) {
        lastCorruptedUserId.set(corruptedUserId);
        lastTriggeredAt.set(Instant.now());
        triggerCount.incrementAndGet();
    }

    public void recordResolution() {
        lastResolvedAt.set(Instant.now());
        resolvedCount.incrementAndGet();
    }

    public Instant lastResolvedAt() {
        return lastResolvedAt.get();
    }

    public int resolvedCount() {
        return resolvedCount.get();
    }

    public void reset() {
        lastCorruptedUserId.set(null);
        lastTriggeredAt.set(null);
        triggerCount.set(0);
        lastResolvedAt.set(null);
        resolvedCount.set(0);
    }

    public Long lastCorruptedUserId() {
        return lastCorruptedUserId.get();
    }

    public Instant lastTriggeredAt() {
        return lastTriggeredAt.get();
    }

    public int triggerCount() {
        return triggerCount.get();
    }
}
