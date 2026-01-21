package edu.mondragon.we2.pinkalert.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.mondragon.we2.pinkalert.model.Role;
import edu.mondragon.we2.pinkalert.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    List<User> findByRole(Role role);
}
