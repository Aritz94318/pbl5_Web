package edu.mondragon.we2.pinkAlert.service;

import edu.mondragon.we2.pinkAlert.model.*;
import edu.mondragon.we2.pinkAlert.repository.DoctorRepository;
import edu.mondragon.we2.pinkAlert.repository.PatientRepository;
import edu.mondragon.we2.pinkAlert.repository.UserRepository;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest extends EasyMockSupport {

    private UserRepository userRepository;
    private DoctorRepository doctorRepository;
    private PatientRepository patientRepository;
    private UserService userService;
    private User user;

    @BeforeEach
    void setUp() {
        userRepository = EasyMock.mock(UserRepository.class);
        doctorRepository = EasyMock.mock(DoctorRepository.class);
        patientRepository = EasyMock.mock(PatientRepository.class);
        userService = new UserService(userRepository, doctorRepository, patientRepository);
        
        user = new User();
        user.setId(1);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setPasswordHash("hashedpassword");
        user.setRole(Role.PATIENT);
    }

    @Test
    void testFindByIdentifierByUsername() {
        EasyMock.expect(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("testuser", "testuser"))
                .andReturn(Optional.of(user));
        EasyMock.replay(userRepository);
        
        Optional<User> result = userService.findByIdentifier("testuser");
        
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        EasyMock.verify(userRepository);
    }

    @Test
    void testFindByIdentifierByEmail() {
        EasyMock.expect(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("test@example.com", "test@example.com"))
                .andReturn(Optional.of(user));
        EasyMock.replay(userRepository);
        
        Optional<User> result = userService.findByIdentifier("test@example.com");
        
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        EasyMock.verify(userRepository);
    }

    @Test
    void testFindAll() {
        EasyMock.expect(userRepository.findAll()).andReturn(List.of(user));
        EasyMock.replay(userRepository);
        
        List<User> result = userService.findAll();
        
        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
        EasyMock.verify(userRepository);
    }

    @Test
    void testFindByRole() {
        EasyMock.expect(userRepository.findByRole(Role.PATIENT)).andReturn(List.of(user));
        EasyMock.replay(userRepository);
        
        List<User> result = userService.findByRole(Role.PATIENT);
        
        assertEquals(1, result.size());
        assertEquals(Role.PATIENT, result.get(0).getRole());
        EasyMock.verify(userRepository);
    }

    @Test
    void testGetUserFound() {
        EasyMock.expect(userRepository.findById(1)).andReturn(Optional.of(user));
        EasyMock.replay(userRepository);
        
        User result = userService.get(1);
        
        assertNotNull(result);
        assertEquals(1, result.getId());
        EasyMock.verify(userRepository);
    }

    @Test
    void testGetUserNotFound() {
        EasyMock.expect(userRepository.findById(99)).andReturn(Optional.empty());
        EasyMock.replay(userRepository);
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> userService.get(99)
        );
        
        assertTrue(ex.getMessage().contains("User not found"));
        EasyMock.verify(userRepository);
    }

    @Test
    void testCreateUser() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setFullName("New User");
        newUser.setRole(Role.PATIENT);
        
        EasyMock.expect(userRepository.save(EasyMock.anyObject(User.class)))
                .andAnswer(() -> {
                    User u = (User) EasyMock.getCurrentArguments()[0];
                    u.setId(2);
                    return u;
                });
        EasyMock.replay(userRepository);
        
        User result = userService.createUser(newUser, "password123");
        
        assertNotNull(result.getId());
        assertEquals(2, result.getId());
        assertNotNull(result.getPasswordHash());
        assertNotEquals("password123", result.getPasswordHash());
        EasyMock.verify(userRepository);
    }

    @Test
    void testSaveUser() {
        EasyMock.expect(userRepository.saveAndFlush(user)).andReturn(user);
        EasyMock.replay(userRepository);
        
        User result = userService.save(user);
        
        assertNotNull(result);
        assertEquals(user, result);
        EasyMock.verify(userRepository);
    }

    @Test
    void testDeleteById() {
        userRepository.deleteById(1);
        EasyMock.expectLastCall().once();
        EasyMock.replay(userRepository);
        
        userService.delete(1);
        
        EasyMock.verify(userRepository);
    }

    @Test
    void testDeleteByEntity() {
        userRepository.delete(user);
        EasyMock.expectLastCall().once();
        EasyMock.replay(userRepository);
        
        userService.delete(user);
        
        EasyMock.verify(userRepository);
    }

    @Test
    void testDeleteUserCompletelyWithDoctorAndPatient() {
        Doctor doctor = new Doctor();
        doctor.setId(1);
        Patient patient = new Patient();
        patient.setId(1);
        
        user.setDoctor(doctor);
        user.linkPatient(patient);
        
        EasyMock.expect(userRepository.findById(1)).andReturn(Optional.of(user));
        EasyMock.expect(userRepository.save(user)).andReturn(user);
        userRepository.delete(user);
        EasyMock.expectLastCall().once();
        doctorRepository.delete(doctor);
        EasyMock.expectLastCall().once();
        patientRepository.delete(patient);
        EasyMock.expectLastCall().once();
        
        EasyMock.replay(userRepository, doctorRepository, patientRepository);
        
        userService.deleteUserCompletely(1);
        
        EasyMock.verify(userRepository, doctorRepository, patientRepository);
    }

    @Test
    void testDeleteUserCompletelyWithoutDoctorAndPatient() {
        EasyMock.expect(userRepository.findById(1)).andReturn(Optional.of(user));
        EasyMock.expect(userRepository.save(user)).andReturn(user);
        userRepository.delete(user);
        EasyMock.expectLastCall().once();
        
        EasyMock.replay(userRepository, doctorRepository, patientRepository);
        
        userService.deleteUserCompletely(1);
        
        EasyMock.verify(userRepository, doctorRepository, patientRepository);
    }
}