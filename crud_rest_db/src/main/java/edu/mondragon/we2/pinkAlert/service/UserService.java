package edu.mondragon.we2.pinkAlert.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import edu.mondragon.we2.pinkAlert.model.Doctor;
import edu.mondragon.we2.pinkAlert.model.Patient;
import edu.mondragon.we2.pinkAlert.model.Role;
import edu.mondragon.we2.pinkAlert.model.User;
import edu.mondragon.we2.pinkAlert.repository.DoctorRepository;
import edu.mondragon.we2.pinkAlert.repository.PatientRepository;
import edu.mondragon.we2.pinkAlert.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final DoctorRepository doctorRepository;

    private final PatientRepository patientRepository;

    public UserService(UserRepository userRepository, DoctorRepository doctorRepository,PatientRepository patientRepository) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository=patientRepository;
    }

    public Optional<User> findByIdentifier(String identifier) {
        String id = identifier == null ? "" : identifier.trim();
        return userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(id, id);
    }

    public boolean matches(User user, String rawPassword) {
        return encoder.matches(rawPassword, user.getPasswordHash());
    }

    public User createUser(User user, String rawPassword) {
        user.setPasswordHash(encoder.encode(rawPassword));
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public User get(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    @Transactional
    public User save(User u) {
        return userRepository.saveAndFlush(u);
    }

    public void delete(Integer id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public void delete(User user) {
        userRepository.delete(user);
    }

    public void setPassword(User user, String rawPassword) {
        user.setPasswordHash(encoder.encode(rawPassword));
    }

    @Transactional
    public void deleteUserCompletely(Integer id) {
        User u = get(id);

        Doctor doctor = u.getDoctor();
        Patient patient = u.getPatient();

        u.setRole(Role.ADMIN);
        u.setDoctor(null);
        u.unlinkPatient();

        userRepository.save(u);
        userRepository.delete(u);

        if (doctor != null) {
            doctorRepository.delete(doctor);
        }
        if (patient != null) {
            patientRepository.delete(patient);
        }
    }

}
