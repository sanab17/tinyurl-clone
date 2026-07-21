package com.urlshortener.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Database entity representing a registered system user.
 * Maps to the "users" database table. Holds authentication credentials and creation metadata.
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique username chosen by the user, used for logging in.
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Hashed password string of the user.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Date and time when the user registration took place.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Default constructor required by JPA.
     */
    public User() {
    }

    /**
     * Constructs a new User entity with username, password, and the current timestamp.
     *
     * @param username user login handle
     * @param password encoded login password
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle callback executed before persisting a user.
     * Guarantees the creation date is initialized with the current system time.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
