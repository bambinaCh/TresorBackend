package ch.bbw.pr.tresorbackend.repository;

import ch.bbw.pr.tresorbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UserRepository
 * @author Peter Rutschmann
 * @AUTHOR cj
 */
public interface UserRepository extends JpaRepository<User, Long> {
   Optional<User> findByEmail(String email);
//   Optional<User> findByResetToken(String token);

}
