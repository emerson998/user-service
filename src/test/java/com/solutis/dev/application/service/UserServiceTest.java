package com.solutis.dev.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.solutis.dev.application.dto.user.UserRequest;
import com.solutis.dev.application.dto.user.UserResponse;
import com.solutis.dev.application.dto.user.UserUpdateRequest;
import com.solutis.dev.application.dto.user.UserUpsertRequest;
import com.solutis.dev.application.port.out.PasswordEncoderPort;
import com.solutis.dev.domain.exception.DuplicateResourceException;
import com.solutis.dev.domain.exception.ResourceNotFoundException;
import com.solutis.dev.domain.model.User;
import com.solutis.dev.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @InjectMocks
    private UserService userService;

    @Test
    void create_shouldEncodePasswordAndSaveUser_whenEmailNotTaken() {
        UserRequest request = new UserRequest("Alice", "alice@example.com", "12345678909", "password123", null, null);
        User saved = new User(1L, "Alice", "alice@example.com", "12345678909", "hashed", null, null, true, null);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userRepository.findByCpf(request.cpf())).thenReturn(Optional.empty());
        when(passwordEncoderPort.encode(request.password())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Alice");
        assertThat(response.cpf()).isEqualTo("12345678909");
        verify(passwordEncoderPort).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_shouldThrowDuplicateResourceException_whenEmailAlreadyExists() {
        UserRequest request = new UserRequest("Alice", "alice@example.com", "12345678909", "password123", null, null);
        User existing = new User(1L, "Alice", "alice@example.com", "12345678909", "hashed", null, null, true, null);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowDuplicateResourceException_whenCpfAlreadyExists() {
        UserRequest request = new UserRequest("Alice", "alice@example.com", "12345678909", "password123", null, null);
        User existing = new User(2L, "Bob", "bob@example.com", "12345678909", "hashed", null, null, true, null);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userRepository.findByCpf(request.cpf())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        UserUpdateRequest request = new UserUpdateRequest("Alice", null, null);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_shouldUpdateProfile_whenUserExists() {
        User existing = new User(1L, "Alice", "alice@example.com", "12345678909", "hashed", null, null, true, null);
        UserUpdateRequest request = new UserUpdateRequest("Alice Smith", "+55 11 99999-0000", "bio");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(existing);

        UserResponse response = userService.update(1L, request);

        assertThat(response.name()).isEqualTo("Alice Smith");
        assertThat(response.phone()).isEqualTo("+55 11 99999-0000");
        assertThat(response.bio()).isEqualTo("bio");
    }

    @Test
    void upsert_shouldCreateUser_whenEmailNotFoundAndPasswordProvided() {
        UserUpsertRequest request = new UserUpsertRequest("Alice", "alice@example.com", "12345678909", "password123", null, null);
        User saved = new User(1L, "Alice", "alice@example.com", "12345678909", "hashed", null, null, true, null);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userRepository.findByCpf(request.cpf())).thenReturn(Optional.empty());
        when(passwordEncoderPort.encode(request.password())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.upsert(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("alice@example.com");
        verify(passwordEncoderPort).encode("password123");
    }

    @Test
    void upsert_shouldThrowIllegalArgumentException_whenCreatingWithoutPassword() {
        UserUpsertRequest request = new UserUpsertRequest("Alice", "alice@example.com", "12345678909", null, null, null);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.upsert(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void upsert_shouldThrowDuplicateResourceException_whenCreatingWithCpfAlreadyTaken() {
        UserUpsertRequest request = new UserUpsertRequest("Alice", "alice@example.com", "12345678909", "password123", null, null);
        User existing = new User(2L, "Bob", "bob@example.com", "12345678909", "hashed", null, null, true, null);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userRepository.findByCpf(request.cpf())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.upsert(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void upsert_shouldUpdateProfileWithoutChangingPassword_whenEmailExistsAndPasswordBlank() {
        User existing = new User(1L, "Alice", "alice@example.com", "12345678909", "hashed", null, null, true, null);
        UserUpsertRequest request = new UserUpsertRequest(
                "Alice Smith", "alice@example.com", "12345678909", null, "+55 11 99999-0000", "bio");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(existing);

        UserResponse response = userService.upsert(request);

        assertThat(response.name()).isEqualTo("Alice Smith");
        assertThat(response.phone()).isEqualTo("+55 11 99999-0000");
        verify(passwordEncoderPort, never()).encode(any());
    }

    @Test
    void upsert_shouldChangePassword_whenEmailExistsAndPasswordProvided() {
        User existing = new User(1L, "Alice", "alice@example.com", "12345678909", "oldHash", null, null, true, null);
        UserUpsertRequest request = new UserUpsertRequest(
                "Alice", "alice@example.com", "12345678909", "newPassword123", null, null);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(existing));
        when(passwordEncoderPort.encode("newPassword123")).thenReturn("newHash");
        when(userRepository.save(any(User.class))).thenReturn(existing);

        userService.upsert(request);

        verify(passwordEncoderPort).encode("newPassword123");
        assertThat(existing.getPasswordHash()).isEqualTo("newHash");
    }

    @Test
    void delete_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void delete_shouldSoftDeleteUser_whenUserExists() {
        User existing = new User(1L, "Alice", "alice@example.com", "12345678909", "hashed", null, null, true, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(existing);

        userService.delete(1L);

        verify(userRepository).save(existing);
        assertThat(existing.isDeleted()).isTrue();
    }

    @Test
    void listAll_shouldReturnEmptyList_whenNoUsersExist() {
        when(userRepository.findAll()).thenReturn(List.of());

        assertThat(userService.listAll()).isEmpty();
    }

    @Test
    void listAll_shouldReturnMappedUsers_whenUsersExist() {
        User user = new User(1L, "Alice", "alice@example.com", "12345678909", "hashed", null, null, true, null);
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> result = userService.listAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).email()).isEqualTo("alice@example.com");
    }
}
