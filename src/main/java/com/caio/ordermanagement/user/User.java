package com.caio.ordermanagement.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

import com.caio.ordermanagement.user.exceptions.InvalidUserException;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected User() {
    }

    public User(String name, String email, String password) {

        if (name == null || name.isBlank()) {
            throw new InvalidUserException("Name cannot be blank");
        }

        if (email == null || email.isBlank()) {
            throw new InvalidUserException("Email cannot be blank");
        }

        if (password == null || password.isBlank()) {
            throw new InvalidUserException("Password cannot be blank");
        }

        this.name = name;
        this.email = email;
        this.password = password;
        this.active = true;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
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

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void deactivate() {
        this.active = false;
    }
}
