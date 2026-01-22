package edu.mondragon.we2.pinkAlert.controller;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import edu.mondragon.we2.pinkAlert.model.User;
import edu.mondragon.we2.pinkAlert.repository.UserRepository;
import edu.mondragon.we2.pinkAlert.service.UserService;
import edu.mondragon.we2.pinkAlert.model.Role;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LoginControllerTest extends EasyMockSupport {

    private UserRepository userRepository;
    private UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);

        userService = new UserService(userRepository);

        LoginController controller = new LoginController(userService);

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/");
        viewResolver.setSuffix(".jsp");

        mockMvc = MockMvcBuilders.standaloneSetup(controller).setViewResolvers(viewResolver).build();
    }

    @Test
    void testShowLoginPageFromRoot() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void testShowLoginPageFromLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void testDoctorLoginSuccess() throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        User doctor = new User();
        doctor.setUsername("doctor");
        doctor.setEmail("doctor@test.com");
        doctor.setPasswordHash(encoder.encode("123"));
        doctor.setRole(Role.DOCTOR);

        EasyMock.expect(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("doctor", "doctor"))
                .andReturn(Optional.of(doctor));

        EasyMock.replay(userRepository);

        mockMvc.perform(post("/login").param("username", "doctor").param("password", "123"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/doctor/dashboard"));

        EasyMock.verify(userRepository);
    }

    @Test
    void testPatientLoginSuccess() throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        User patient = new User();
        patient.setUsername("patient");
        patient.setEmail("patient@test.com");
        patient.setPasswordHash(encoder.encode("123"));
        patient.setRole(Role.PATIENT);

        EasyMock.expect(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("patient", "patient"))
                .andReturn(Optional.of(patient));

        EasyMock.replay(userRepository);

        mockMvc.perform(post("/login").param("username", "patient").param("password", "123"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/patient/dashboard"));

        EasyMock.verify(userRepository);
    }

    @Test
    void testInvalidLogin() throws Exception {
        EasyMock.expect(userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase("bad", "bad"))
                .andReturn(Optional.empty());

        EasyMock.replay(userRepository);

        mockMvc.perform(post("/login").param("username", "bad").param("password", "bad")).andExpect(status().isOk())
                .andExpect(view().name("login")).andExpect(model().attributeExists("error"));

        EasyMock.verify(userRepository);
    }
}
