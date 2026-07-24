package com.solutis.dev.infrastructure.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String cpf;

    private String passwordHash;

    private String phone;

    private String bio;

    private boolean enabled;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    private Long version;

    protected UserJpaEntity() {
    }

    public UserJpaEntity(Long id, String name, String email, String cpf, String passwordHash, String phone,
            String bio, boolean enabled, Instant deletedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.bio = bio;
        this.enabled = enabled;
        this.deletedAt = deletedAt;
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

    public Long getVersion() {
        return version;
    }

    public void updateMutableFields(String name, String passwordHash, String phone, String bio, boolean enabled,
            Instant deletedAt) {
        this.name = name;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.bio = bio;
        this.enabled = enabled;
        this.deletedAt = deletedAt;
    }
}
