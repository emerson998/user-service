package com.solutis.dev.application.port.out;

import java.util.Optional;

public interface TokenStorePort {

    String issue(Long userId);

    Optional<Long> resolve(String token);

    void invalidate(String token);
}
