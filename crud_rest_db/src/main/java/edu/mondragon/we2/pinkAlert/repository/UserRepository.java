package edu.mondragon.we2.pinkAlert.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.mondragon.we2.pinkAlert.model.User;
import edu.mondragon.we2.pinkalert.model.Role;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    List<User> findByRole(Role role);
}
