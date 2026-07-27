package com.solutis.dev.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.solutis.dev.application.dto.user.BulkActionResult;
import com.solutis.dev.application.dto.user.BulkNotificationActivationRequest;
import com.solutis.dev.domain.exception.ResourceNotFoundException;
import com.solutis.dev.domain.model.Role;
import com.solutis.dev.domain.model.User;
import com.solutis.dev.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserBulkNotificationServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserBulkNotificationService userBulkNotificationService;

    private User newUser(Long id) {
        return new User(id, "User " + id, "user" + id + "@example.com", "1234567890" + id,
                "hashed", null, null, true, null, false, Role.USER);
    }

    @Test
    void activate_shouldEnableNotificationsForAllUsers_whenIdsAreValid() {
        userBulkNotificationService = new UserBulkNotificationService(userRepository);
        User user1 = newUser(1L);
        User user2 = newUser(2L);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(2L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BulkActionResult result = userBulkNotificationService.activate(
                new BulkNotificationActivationRequest(List.of(1L, 2L), false));

        assertThat(result.activatedCount()).isEqualTo(2);
        assertThat(result.dryRun()).isFalse();
        assertThat(user1.isNotificationsEnabled()).isTrue();
        assertThat(user2.isNotificationsEnabled()).isTrue();
        verify(userRepository, times(2)).save(any());
    }

    @Test
    void activate_shouldThrowIllegalArgumentException_whenUserIdsHaveDuplicates() {
        userBulkNotificationService = new UserBulkNotificationService(userRepository);

        assertThatThrownBy(() -> userBulkNotificationService.activate(
                new BulkNotificationActivationRequest(List.of(1L, 1L), false)))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void activate_shouldThrowResourceNotFoundException_whenSomeUserIdDoesNotExist() {
        userBulkNotificationService = new UserBulkNotificationService(userRepository);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(2L)).thenReturn(false);

        assertThatThrownBy(() -> userBulkNotificationService.activate(
                new BulkNotificationActivationRequest(List.of(1L, 2L), false)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("2");

        verify(userRepository, never()).save(any());
    }

    @Test
    void activate_shouldNotPersistAnyChange_whenDryRunIsTrue() {
        userBulkNotificationService = new UserBulkNotificationService(userRepository);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(2L)).thenReturn(true);

        BulkActionResult result = userBulkNotificationService.activate(
                new BulkNotificationActivationRequest(List.of(1L, 2L), true));

        assertThat(result.activatedCount()).isEqualTo(2);
        assertThat(result.dryRun()).isTrue();
        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any());
    }

    @Test
    void activate_shouldStillValidateDuplicates_whenDryRunIsTrue() {
        userBulkNotificationService = new UserBulkNotificationService(userRepository);

        assertThatThrownBy(() -> userBulkNotificationService.activate(
                new BulkNotificationActivationRequest(List.of(1L, 1L), true)))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void activate_shouldStillValidateExistence_whenDryRunIsTrue() {
        userBulkNotificationService = new UserBulkNotificationService(userRepository);
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> userBulkNotificationService.activate(
                new BulkNotificationActivationRequest(List.of(1L), true)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any());
    }
}
