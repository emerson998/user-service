package com.solutis.dev.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void createNew_shouldInitializeWithEnabledTrueAndNoContactInfo() {
        User user = User.createNew("Alice", "alice@example.com", "hashed-password");

        assertThat(user.getId()).isNull();
        assertThat(user.getName()).isEqualTo("Alice");
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(user.getPhone()).isNull();
        assertThat(user.getBio()).isNull();
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void updateProfile_shouldUpdateNamePhoneAndBio() {
        User user = User.createNew("Alice", "alice@example.com", "hashed-password");

        user.updateProfile("Alice Smith", "+55 11 99999-0000", "Software engineer");

        assertThat(user.getName()).isEqualTo("Alice Smith");
        assertThat(user.getPhone()).isEqualTo("+55 11 99999-0000");
        assertThat(user.getBio()).isEqualTo("Software engineer");
    }

    @Test
    void changePassword_shouldUpdatePasswordHash() {
        User user = User.createNew("Alice", "alice@example.com", "old-hash");

        user.changePassword("new-hash");

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
    }

    @Test
    void activateAndDeactivate_shouldToggleEnabledFlag() {
        User user = new User(1L, "Alice", "alice@example.com", "hash", null, null, false);

        user.activate();
        assertThat(user.isEnabled()).isTrue();

        user.deactivate();
        assertThat(user.isEnabled()).isFalse();
    }
}
