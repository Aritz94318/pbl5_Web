package edu.mondragon.we2.pinkAlert.service;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import edu.mondragon.we2.pinkAlert.model.User;
import edu.mondragon.we2.pinkAlert.repository.UserRepository;
import edu.mondragon.we2.pinkAlert.model.Role;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest extends EasyMockSupport {

    private UserRepository userRepository;
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);

        user = new User();
        user.setId(1);
        user.setUsername("john");
        user.setEmail("john@test.com");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPasswordHash(encoder.encode("1234"));
    }


    @Test
    void testFindByIdentifierUsername() {
        EasyMock.expect(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("john", "john")).andReturn(Optional.of(user));

        EasyMock.replay(userRepository);

        Optional<User> result = userService.findByIdentifier("john");

        assertTrue(result.isPresent());
        assertEquals("john", result.get().getUsername());

        EasyMock.verify(userRepository);
    }

    @Test
    void testFindByIdentifierEmail() {
        EasyMock.expect(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("john@test.com", "john@test.com")).andReturn(Optional.of(user));

        EasyMock.replay(userRepository);

        Optional<User> result = userService.findByIdentifier("john@test.com");

        assertTrue(result.isPresent());
        assertEquals("john@test.com", result.get().getEmail());

        EasyMock.verify(userRepository);
    }

    @Test
    void testFindByIdentifierTrimmed() {
        EasyMock.expect(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("john", "john")).andReturn(Optional.of(user));

        EasyMock.replay(userRepository);

        Optional<User> result = userService.findByIdentifier("  john  ");

        assertTrue(result.isPresent());

        EasyMock.verify(userRepository);
    }

    @Test
    void testFindByIdentifierNull() {
        EasyMock.expect(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("", "")).andReturn(Optional.empty());

        EasyMock.replay(userRepository);

        Optional<User> result = userService.findByIdentifier(null);

        assertTrue(result.isEmpty());

        EasyMock.verify(userRepository);
    }

    @Test
    void testPasswordMatches() {
        boolean result = userService.matches(user, "1234");
        assertTrue(result);
    }

    @Test
    void testPasswordDoesNotMatch() {
        boolean result = userService.matches(user, "wrong");
        assertFalse(result);
    }

    @Test
    void testCreateUser() {
        EasyMock.expect(userRepository.save(EasyMock.anyObject(User.class))).andAnswer(() -> {
                    User u = (User) EasyMock.getCurrentArguments()[0];
                    u.setId(1);
                    return u;
                });

        EasyMock.replay(userRepository);

        User result = userService.createUser(new User(), "secret");

        assertNotNull(result);
        assertNotNull(result.getPasswordHash());
        assertNotEquals("secret", result.getPasswordHash());
        assertEquals(1, result.getId());

        EasyMock.verify(userRepository);
    }


    @Test
void testFindAll() {
    // Arrange
    List<User> userList = Arrays.asList(user, new User());
    EasyMock.expect(userRepository.findAll()).andReturn(userList);
    EasyMock.replay(userRepository);

    // Act
    List<User> result = userService.findAll();

    // Assert
    assertEquals(2, result.size());
    EasyMock.verify(userRepository);
}

@Test
void testFindByRole() {
    // Arrange
    Role testRole = Role.DOCTOR;
    List<User> doctors = Arrays.asList(user);
    EasyMock.expect(userRepository.findByRole(testRole)).andReturn(doctors);
    EasyMock.replay(userRepository);

    // Act
    List<User> result = userService.findByRole(testRole);

    // Assert
    assertEquals(1, result.size());
    assertEquals(user, result.get(0));
    EasyMock.verify(userRepository);
}

@Test
void testGetUserExists() {
    // Arrange
    Integer userId = 1;
    EasyMock.expect(userRepository.findById(userId)).andReturn(Optional.of(user));
    EasyMock.replay(userRepository);

    // Act
    User result = userService.get(userId);

    // Assert
    assertNotNull(result);
    assertEquals(userId, result.getId());
    EasyMock.verify(userRepository);
}

@Test
void testGetUserNotFound() {
    // Arrange
    Integer userId = 999;
    EasyMock.expect(userRepository.findById(userId)).andReturn(Optional.empty());
    EasyMock.replay(userRepository);

    // Act & Assert
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        userService.get(userId);
    });
    
    assertTrue(exception.getMessage().contains("User not found"));
    EasyMock.verify(userRepository);
}

@Test
void testSaveUser() {
    // Arrange
    User newUser = new User();
    newUser.setUsername("newUser");
    
    EasyMock.expect(userRepository.save(newUser)).andReturn(newUser);
    EasyMock.replay(userRepository);

    // Act
    User result = userService.save(newUser);

    // Assert
    assertNotNull(result);
    assertEquals("newUser", result.getUsername());
    EasyMock.verify(userRepository);
}

@Test
void testDeleteUser() {
    // Arrange
    Integer userId = 1;
    userRepository.deleteById(userId);
    EasyMock.expectLastCall();
    EasyMock.replay(userRepository);

    // Act
    userService.delete(userId);

    // Assert - No exception should be thrown
    EasyMock.verify(userRepository);
}

@Test
void testSetPassword() {
    // Arrange
    User testUser = new User();
    testUser.setUsername("test");
    String originalHash = testUser.getPasswordHash();

    // Act
    userService.setPassword(testUser, "newPassword123");

    // Assert
    assertNotNull(testUser.getPasswordHash());
    assertNotEquals(originalHash, testUser.getPasswordHash());
    assertTrue(new BCryptPasswordEncoder().matches("newPassword123", testUser.getPasswordHash()));
}

@Test
void testConstructor() {
    // Arrange & Act
    UserService service = new UserService(userRepository);

    // Assert
    assertNotNull(service);
}

@Test
void testPasswordMatchesWithNullUser() {
    // Arrange
    User nullUser = null;

    // Act & Assert
    assertThrows(NullPointerException.class, () -> {
        userService.matches(nullUser, "password");
    });
}


@Test
void testCreateUserWithEmptyPassword() {
    // Arrange
    User newUser = new User();
    newUser.setUsername("test");
    
    EasyMock.expect(userRepository.save(EasyMock.anyObject(User.class))).andReturn(newUser);
    EasyMock.replay(userRepository);

    // Act
    User result = userService.createUser(newUser, "");

    // Assert
    assertNotNull(result);
    assertNotNull(result.getPasswordHash()); // Should encode empty string
    EasyMock.verify(userRepository);
}

@Test
void testFindByIdentifierEmptyString() {
    // Arrange
    EasyMock.expect(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("", ""))
            .andReturn(Optional.empty());
    EasyMock.replay(userRepository);

    // Act
    Optional<User> result = userService.findByIdentifier("");

    // Assert
    assertTrue(result.isEmpty());
    EasyMock.verify(userRepository);
}

@Test
void testFindByIdentifierWithSpacesOnly() {
    // Arrange
    EasyMock.expect(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("", ""))
            .andReturn(Optional.empty());
    EasyMock.replay(userRepository);

    // Act
    Optional<User> result = userService.findByIdentifier("   ");

    // Assert
    assertTrue(result.isEmpty());
    EasyMock.verify(userRepository);
}
}
