package com.emerson.dev.usuarios.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

import com.emerson.dev.usuarios.domain.exception.SaldoInsuficienteException;

public class User {

    public static final BigDecimal SALDO_INICIAL = new BigDecimal("10000");

    private Long id;
    private String name;
    private String email;
    private String passwordHash;
    private String phone;
    private String bio;
    private boolean enabled;
    private BigDecimal saldo;

    public User(Long id, String name, String email, String passwordHash, String phone, String bio,
            boolean enabled, BigDecimal saldo) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.bio = bio;
        this.enabled = enabled;
        this.saldo = saldo == null ? SALDO_INICIAL : saldo;
    }

    public static User createNew(String name, String email, String passwordHash) {
        return new User(null, name, email, passwordHash, null, null, true, SALDO_INICIAL);
    }

    public void debitar(BigDecimal valor) {
        if (saldo.compareTo(valor) < 0) {
            throw new SaldoInsuficienteException(id, saldo, valor);
        }
        this.saldo = this.saldo.subtract(valor);
    }

    public void creditar(BigDecimal valor) {
        this.saldo = this.saldo.add(valor);
    }

    public void updateProfile(String name, String phone, String bio) {
        this.name = name;
        this.phone = phone;
        this.bio = bio;
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

    public BigDecimal getSaldo() {
        return saldo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
