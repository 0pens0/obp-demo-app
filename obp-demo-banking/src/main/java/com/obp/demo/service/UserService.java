package com.obp.demo.service;

import com.obp.demo.dto.CreateUserRequest;
import com.obp.demo.model.ObpUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final ObpApiService obpApiService;
    private final ConcurrentMap<String, ObpUser> createdUsers = new ConcurrentHashMap<>();

    public ObpUser createUser(CreateUserRequest request) {
        try {
            String userId = obpApiService.createUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName()
            );

            ObpUser user = new ObpUser();
            user.setUserId(userId);
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());

            createdUsers.put(userId, user);
            log.info("User created successfully: {}", userId);
            return user;
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
        }
    }

    public List<ObpUser> getAllUsers() {
        return new ArrayList<>(createdUsers.values());
    }

    public ObpUser getUserById(String userId) {
        return createdUsers.get(userId);
    }
}
