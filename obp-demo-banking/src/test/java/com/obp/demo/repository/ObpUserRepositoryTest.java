package com.obp.demo.repository;

import com.obp.demo.model.ObpUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ObpUserRepository.
 * Uses @DataJpaTest for testing JPA repositories with an in-memory database.
 */
@DataJpaTest
@ActiveProfiles("test")
class ObpUserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ObpUserRepository userRepository;

    private ObpUser testUser;

    @BeforeEach
    void setUp() {
        testUser = new ObpUser();
        testUser.setUserId("user-123");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
    }

    @Test
    void testSaveUser() {
        // When
        ObpUser saved = userRepository.save(testUser);

        // Then
        assertNotNull(saved);
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals("testuser", saved.getUsername());
    }

    @Test
    void testFindByUsername() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<ObpUser> found = userRepository.findByUsername("testuser");

        // Then
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void testFindByEmail() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<ObpUser> found = userRepository.findByEmail("test@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testExistsByUsername() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        boolean exists = userRepository.existsByUsername("testuser");

        // Then
        assertTrue(exists);
    }

    @Test
    void testExistsByEmail() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Then
        assertTrue(exists);
    }

    @Test
    void testUniqueUsernameConstraint() {
        // Given
        entityManager.persistAndFlush(testUser);

        ObpUser duplicateUser = new ObpUser();
        duplicateUser.setUserId("user-456");
        duplicateUser.setUsername("testuser"); // Duplicate username
        duplicateUser.setEmail("different@example.com");
        duplicateUser.setFirstName("Different");
        duplicateUser.setLastName("User");

        // When & Then
        assertThrows(Exception.class, () -> {
            userRepository.saveAndFlush(duplicateUser);
        });
    }

    @Test
    void testUniqueEmailConstraint() {
        // Given
        entityManager.persistAndFlush(testUser);

        ObpUser duplicateUser = new ObpUser();
        duplicateUser.setUserId("user-456");
        duplicateUser.setUsername("differentuser");
        duplicateUser.setEmail("test@example.com"); // Duplicate email
        duplicateUser.setFirstName("Different");
        duplicateUser.setLastName("User");

        // When & Then
        assertThrows(Exception.class, () -> {
            userRepository.saveAndFlush(duplicateUser);
        });
    }
}
