package com.caio.ordermanagement.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.regex.Pattern;
import java.time.Instant;

import com.caio.ordermanagement.user.exceptions.InvalidUserException;

@Entity
@Table(name = "users")
public class User {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MAX_PASSWORD_LENGTH = 255;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 254)
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

        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidUserException("Name cannot exceed 100 characters");
        }

        if (email == null || email.isBlank()) {
            throw new InvalidUserException("Email cannot be blank");
        }

        if (email.length() > MAX_EMAIL_LENGTH) {
            throw new InvalidUserException("Email cannot exceed 254 characters");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidUserException("Invalid email");
        }

        if (password == null || password.isBlank()) {
            throw new InvalidUserException("Password cannot be blank");
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new InvalidUserException("Password cannot exceed 255 characters");
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
