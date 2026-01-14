/* package edu.mondragon.we2.pinkAlert.service;

import edu.mondragon.we2.pinkAlert.model.User;
import edu.mondragon.we2.pinkAlert.repository.UserRepository;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
}
 */