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

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

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

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

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
