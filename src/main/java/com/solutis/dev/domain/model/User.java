package com.solutis.dev.domain.model;

public class User {

    private final Long id;
    private String name;
    private final String email;
    private String passwordHash;
    private String phone;
    private String bio;
    private boolean enabled;

    public User(Long id, String name, String email, String passwordHash, String phone, String bio,
            boolean enabled) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.bio = bio;
        this.enabled = enabled;
    }

    public static User createNew(String name, String email, String passwordHash) {
        return new User(null, name, email, passwordHash, null, null, true);
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

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
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
}
