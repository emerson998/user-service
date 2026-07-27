package com.solutis.dev.domain.model;

import java.time.Instant;

public class User {

    private final Long id;
    private String name;
    private final String email;
    private final String cpf;
    private String passwordHash;
    private String phone;
    private String bio;
    private boolean enabled;
    private Instant deletedAt;
    private boolean notificationsEnabled;
    private final Role role;

    public User(Long id, String name, String email, String cpf, String passwordHash, String phone, String bio,
            boolean enabled, Instant deletedAt, boolean notificationsEnabled, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.bio = bio;
        this.enabled = enabled;
        this.deletedAt = deletedAt;
        this.notificationsEnabled = notificationsEnabled;
        this.role = role;
    }

    public static User createNew(String name, String email, String cpf, String passwordHash) {
        return new User(null, name, email, cpf, passwordHash, null, null, true, null, false, Role.USER);
    }

    public void updateProfile(String name, String phone, String bio) {
        this.name = name;
        this.phone = phone;
        this.bio = bio;
    }

    public void changePassword(String newHash) {
        this.passwordHash = newHash;
    }

    public void activate() {
        this.enabled = true;
    }

    public void deactivate() {
        this.enabled = false;
    }

    public void delete() {
        this.deletedAt = Instant.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void enableNotifications() {
        this.notificationsEnabled = true;
    }

    public void disableNotifications() {
        this.notificationsEnabled = false;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCpf() {
        return cpf;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public String getBio() {
        return bio;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public Role getRole() {
        return role;
    }
}
