package com.solutis.dev.application.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solutis.dev.application.dto.user.BulkActionResult;
import com.solutis.dev.application.dto.user.BulkNotificationActivationRequest;
import com.solutis.dev.application.port.in.UserBulkNotificationUseCase;
import com.solutis.dev.domain.exception.ResourceNotFoundException;
import com.solutis.dev.domain.model.User;
import com.solutis.dev.domain.repository.UserRepository;

@Service
@Transactional
public class UserBulkNotificationService implements UserBulkNotificationUseCase {

    private final UserRepository userRepository;

    public UserBulkNotificationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public BulkActionResult activate(BulkNotificationActivationRequest request) {
        List<Long> userIds = request.userIds();

        if (new HashSet<>(userIds).size() != userIds.size()) {
            throw new IllegalArgumentException("userIds contém ids duplicados");
        }

        List<Long> missingIds = userIds.stream()
                .filter(id -> !userRepository.existsById(id))
                .toList();
        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException("Usuários com ids " + missingIds + " não encontrados");
        }

        boolean dryRun = request.isDryRun();
        if (!dryRun) {
            for (Long id : userIds) {
                User user = userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
                user.enableNotifications();
                userRepository.save(user);
            }
        }

        return new BulkActionResult(userIds.size(), List.of(), dryRun);
    }
}
