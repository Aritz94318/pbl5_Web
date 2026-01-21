package edu.mondragon.we2.pinkalert.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import edu.mondragon.we2.pinkalert.model.Role;
import edu.mondragon.we2.pinkalert.model.User;
import edu.mondragon.we2.pinkalert.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    public User save(User u) {
        return userRepository.save(u);
    }

    public void delete(Integer id) {
        userRepository.deleteById(id);
    }

    public void setPassword(User user, String rawPassword) {
        user.setPasswordHash(encoder.encode(rawPassword));
    }

}
