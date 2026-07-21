package com.urlshortener.service;

import com.urlshortener.entity.User;
import com.urlshortener.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

/**
 * Service class that manages user logic, registrations, and database queries.
 * Implements Spring Security {@link UserDetailsService} to load users during authentication.
 */
@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs the UserService.
     *
     * @param userRepository  data access for User entities
     * @param passwordEncoder utility to encode raw passwords
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Spring Security hook method to retrieve user details from the database during login.
     *
     * @param username the username identifying the user whose data is required.
     * @return a fully populated {@link UserDetails} object containing user credentials
     * @throws UsernameNotFoundException if the username is not registered in the system
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Authentication failed: User not found with username '{}'", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.emptyList()
        );
    }

    /**
     * Finds a user record based on their username.
     *
     * @param username user login handle
     * @return an {@link Optional} containing the User if registered
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Creates and registers a new User account.
     * Performs validations on the inputs: username cannot be blank/taken, password must be at least 6 characters.
     * Encodes the password using BCrypt before persisting the user.
     *
     * @param username registration username
     * @param password registration plain text password
     * @return the saved {@link User} entity
     * @throws IllegalArgumentException if the parameters fail validation checks
     */
    public User registerUser(String username, String password) throws IllegalArgumentException {
        if (username == null || username.isBlank()) {
            logger.warn("Registration failed: Username cannot be empty");
            throw new IllegalArgumentException("Username is required.");
        }
        if (userRepository.existsByUsername(username)) {
            logger.warn("Registration failed: Username '{}' already exists", username);
            throw new IllegalArgumentException("This username is already taken. Please choose another.");
        }
        if (password == null || password.length() < 6) {
            logger.warn("Registration failed: Password too short for username '{}'", username);
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        User user = new User(username, passwordEncoder.encode(password));
        User savedUser = userRepository.save(user);
        logger.info("Successfully registered user: {}", username);
        return savedUser;
    }
}
