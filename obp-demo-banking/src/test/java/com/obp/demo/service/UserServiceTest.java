package com.obp.demo.service;

import com.obp.demo.dto.CreateUserRequest;
import com.obp.demo.exception.ResourceNotFoundException;
import com.obp.demo.model.ObpUser;
import com.obp.demo.repository.ObpUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private ObpApiService obpApiService;

    @Mock
    private ObpUserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest createUserRequest;
    private ObpUser obpUser;

    @BeforeEach
    void setUp() {
        createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("testuser");
        createUserRequest.setEmail("test@example.com");
        createUserRequest.setPassword("password123");
        createUserRequest.setFirstName("Test");
        createUserRequest.setLastName("User");

        obpUser = new ObpUser();
        obpUser.setUserId("user-123");
        obpUser.setUsername("testuser");
        obpUser.setEmail("test@example.com");
        obpUser.setFirstName("Test");
        obpUser.setLastName("User");
    }

    @Test
    void testCreateUser_Success() {
        // Given
        String userId = "user-123";
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(obpApiService.createUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(userId);
        when(userRepository.save(any(ObpUser.class))).thenReturn(obpUser);

        // When
        ObpUser result = userService.createUser(createUserRequest);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("testuser", result.getUsername());
        verify(obpApiService, times(1)).createUser(
                createUserRequest.getUsername(),
                createUserRequest.getEmail(),
                createUserRequest.getPassword(),
                createUserRequest.getFirstName(),
                createUserRequest.getLastName()
        );
        verify(userRepository, times(1)).save(any(ObpUser.class));
    }

    @Test
    void testCreateUser_UsernameExists() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(createUserRequest);
        });

        verify(obpApiService, never()).createUser(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(userRepository, never()).save(any(ObpUser.class));
    }

    @Test
    void testCreateUser_EmailExists() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(createUserRequest);
        });

        verify(obpApiService, never()).createUser(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(userRepository, never()).save(any(ObpUser.class));
    }

    @Test
    void testGetAllUsers() {
        // Given
        List<ObpUser> users = Arrays.asList(obpUser);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<ObpUser> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetUserById_Success() {
        // Given
        String userId = "user-123";
        when(userRepository.findById(userId)).thenReturn(Optional.of(obpUser));

        // When
        ObpUser result = userService.getUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testGetUserById_NotFound() {
        // Given
        String userId = "non-existent";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(userId);
        });

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testGetUserByUsername_Success() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(obpUser));

        // When
        ObpUser result = userService.getUserByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void testUserExists() {
        // Given
        String username = "testuser";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // When
        boolean result = userService.userExists(username);

        // Then
        assertTrue(result);
        verify(userRepository, times(1)).existsByUsername(username);
    }
}
