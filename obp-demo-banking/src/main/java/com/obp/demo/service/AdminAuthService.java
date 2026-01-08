package com.obp.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for admin authentication.
 * Uses environment variables for admin credentials instead of hardcoded values.
 */
@Service
@Slf4j
public class AdminAuthService {

    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.admin.username:admin}")
    private String adminUsername;
    
    @Value("${app.admin.password:${ADMIN_PASSWORD:admin}}")
    private String adminPassword;
    
    // Store hashed password for comparison
    private String hashedAdminPassword;

    public AdminAuthService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        // Hash the admin password on initialization
        // In production, store the hashed password in a secure vault
        this.hashedAdminPassword = passwordEncoder.encode(adminPassword);
        log.info("Admin authentication service initialized for user: {}", adminUsername);
    }

    /**
     * Authenticate admin user
     * @param username the username
     * @param password the plain text password
     * @return true if authentication succeeds
     */
    public boolean authenticate(String username, String password) {
        if (!adminUsername.equals(username)) {
            log.warn("Authentication attempt with invalid username: {}", username);
            return false;
        }
        
        // For demo purposes, we compare with plain text
        // In production, use passwordEncoder.matches(password, hashedPassword)
        boolean authenticated = adminPassword.equals(password) || 
                               passwordEncoder.matches(password, hashedAdminPassword);
        
        if (authenticated) {
            log.info("Admin authentication successful for user: {}", username);
        } else {
            log.warn("Admin authentication failed for user: {}", username);
        }
        
        return authenticated;
    }
}
