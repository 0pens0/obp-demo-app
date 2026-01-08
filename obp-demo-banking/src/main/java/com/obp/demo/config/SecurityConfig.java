package com.obp.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Security configuration for the application.
 * Handles authentication, authorization, and password encoding.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Password encoder bean using BCrypt.
     * BCrypt is a strong, adaptive hashing function recommended for password storage.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strength factor 12 (recommended for production)
    }

    /**
     * Security filter chain configuration.
     * Defines URL patterns, authentication requirements, and session management.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF configuration - disabled for API endpoints, enabled for form submissions
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**") // Disable CSRF for REST API
                .ignoringRequestMatchers("/actuator/**") // Disable CSRF for Actuator
            )
            // Authorization configuration
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/",
                    "/admin/login",
                    "/admin/authenticate",
                    "/customer/login",
                    "/customer/authenticate",
                    "/css/**",
                    "/js/**",
                    "/api/chat",
                    "/actuator/health",
                    "/actuator/info"
                ).permitAll()
                // Admin endpoints require ADMIN role
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Customer endpoints require CUSTOMER role
                .requestMatchers("/customer/**").hasRole("CUSTOMER")
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            // Session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1) // Prevent multiple sessions per user
                .maxSessionsPreventsLogin(false) // Allow new login to invalidate old session
            )
            // Disable default form login (using custom authentication)
            .formLogin(form -> form.disable())
            // Disable HTTP Basic authentication
            .httpBasic(basic -> basic.disable());
        
        return http.build();
    }
}
