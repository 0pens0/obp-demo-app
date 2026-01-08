package com.obp.demo.service;

import com.obp.demo.dto.CreateUserRequest;
import com.obp.demo.exception.ResourceNotFoundException;
import com.obp.demo.model.ObpUser;
import com.obp.demo.repository.ObpUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing OBP users.
 * Handles user creation, retrieval, and persistence operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final ObpApiService obpApiService;
    private final ObpUserRepository userRepository;

    /**
     * Create a new user in OBP sandbox and persist locally
     * @param request the user creation request
     * @return the created user
     * @throws RuntimeException if user creation fails
     */
    public ObpUser createUser(CreateUserRequest request) {
        // Check if user already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }
        
        try {
            // Create user in OBP sandbox
            String userId = obpApiService.createUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName()
            );

            // Create and persist user entity
            ObpUser user = new ObpUser();
            user.setUserId(userId);
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            // Note: Password is not stored locally for security reasons

            ObpUser savedUser = userRepository.save(user);
            log.info("User created successfully: {} with OBP ID: {}", savedUser.getUsername(), userId);
            return savedUser;
        } catch (IllegalArgumentException e) {
            log.error("Validation error creating user: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }

    /**
     * Get all users
     * @return list of all users
     */
    @Transactional(readOnly = true)
    public List<ObpUser> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get user by ID
     * @param userId the user ID
     * @return the user
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public ObpUser getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    /**
     * Get user by username
     * @param username the username
     * @return the user
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public ObpUser getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    /**
     * Check if user exists by username
     * @param username the username to check
     * @return true if user exists
     */
    @Transactional(readOnly = true)
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }
}
