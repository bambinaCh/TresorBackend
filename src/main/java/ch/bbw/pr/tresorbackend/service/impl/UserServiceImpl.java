package ch.bbw.pr.tresorbackend.service.impl;

import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.repository.UserRepository;
import ch.bbw.pr.tresorbackend.service.UserService;
import ch.bbw.pr.tresorbackend.service.PasswordEncryptionService;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;




/**
 * UserServiceImpl
 * @author Peter Rutschmann
 * @author CJ
 */
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

   private UserRepository userRepository;

   private final PasswordEncryptionService passwordEncryptionService;

   /**********************************/
   //      Token reset passwort  (RAM, nicht DB)    */
    private final Map<String, String> resetTokens = new HashMap<>();

   @Override
   public User createUser(User user) {
      return userRepository.save(user);
   }

   @Override
   public User getUserById(Long userId) {
      Optional<User> optionalUser = userRepository.findById(userId);
      return optionalUser.get();
   }

   @Override
   public User findByEmail(String email) {
      Optional<User> optionalUser = userRepository.findByEmail(email);
      return optionalUser.orElse(null);
   }

   @Override
   public List<User> getAllUsers() {
      return (List<User>) userRepository.findAll();
   }

   @Override
   public User updateUser(User user) {
      User existingUser = userRepository.findById(user.getId()).get();
      existingUser.setFirstName(user.getFirstName());
      existingUser.setLastName(user.getLastName());
      existingUser.setEmail(user.getEmail());
      User updatedUser = userRepository.save(existingUser);
      return updatedUser;
   }

   @Override
   public void deleteUser(Long userId) {
      userRepository.deleteById(userId);
   }


   /*************************/
   /*          RESET        */


   public String createResetToken(String email) {
      Optional<User> optionalUser = userRepository.findByEmail(email);
      if (optionalUser.isEmpty()) throw new RuntimeException("User not found");

      String token = UUID.randomUUID().toString();
      resetTokens.put(token, email);
      return token;
   }

   @Override
   public void resetPassword(String token, String newPassword) {
      String email = resetTokens.get(token);
      if (email == null) throw new RuntimeException("Invalid or expired token");

      User user = userRepository.findByEmail(email).orElseThrow();
      String hashed = passwordEncryptionService.hashPassword(newPassword);
      user.setPassword(hashed);
      userRepository.save(user);

      resetTokens.remove(token);
   }

}
