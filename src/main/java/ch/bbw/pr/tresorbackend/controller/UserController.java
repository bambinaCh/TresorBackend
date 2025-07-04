package ch.bbw.pr.tresorbackend.controller;

import ch.bbw.pr.tresorbackend.model.ConfigProperties;
import ch.bbw.pr.tresorbackend.model.EmailAdress;
import ch.bbw.pr.tresorbackend.model.RegisterUser;
import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.service.PasswordEncryptionService;
import ch.bbw.pr.tresorbackend.service.UserService;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("api/users")
public class UserController {

   private UserService userService;
   private PasswordEncryptionService passwordService;
   private final ConfigProperties configProperties;
   private static final Logger logger = LoggerFactory.getLogger(UserController.class);

   @Autowired
   public UserController(ConfigProperties configProperties, UserService userService,
                         PasswordEncryptionService passwordService) {
      this.configProperties = configProperties;
      this.userService = userService;
      this.passwordService = passwordService;

      System.out.println("UserController.UserController: cross origin: " + configProperties.getOrigin());
      logger.info("UserController initialized: " + configProperties.getOrigin());
   }

   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping
   public ResponseEntity<String> createUser(@Valid @RequestBody RegisterUser registerUser, BindingResult bindingResult) {
      // CAPTCHA prüfen
      String captchaToken = registerUser.getCaptchaToken();
      if (captchaToken == null || captchaToken.isEmpty() || !isCaptchaValid(captchaToken)) {
         JsonObject obj = new JsonObject();
         obj.addProperty("message", "Captcha verification failed.");
         return ResponseEntity.badRequest().body(new Gson().toJson(obj));
      }

      // Input Validation
      if (bindingResult.hasErrors()) {
         List<String> errors = bindingResult.getFieldErrors().stream()
                 .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                 .collect(Collectors.toList());
         JsonArray arr = new JsonArray();
         errors.forEach(arr::add);
         JsonObject obj = new JsonObject();
         obj.add("message", arr);
         return ResponseEntity.badRequest().body(new Gson().toJson(obj));
      }

      if (!isPasswordStrong(registerUser.getPassword())) {
         JsonObject obj = new JsonObject();
         obj.addProperty("message", "Password too weak.");
         return ResponseEntity.badRequest().body(new Gson().toJson(obj));
      }

      User user = new User(
              null,
              registerUser.getFirstName(),
              registerUser.getLastName(),
              registerUser.getEmail(),
              passwordService.hashPassword(registerUser.getPassword()),
              "ROLE_USER"
      );

      userService.createUser(user);
      JsonObject obj = new JsonObject();
      obj.addProperty("answer", "User Saved");
      return ResponseEntity.accepted().body(new Gson().toJson(obj));
   }

   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @GetMapping("{id}")
   public ResponseEntity<User> getUserById(@PathVariable("id") Long userId) {
      User user = userService.getUserById(userId);
      return new ResponseEntity<>(user, HttpStatus.OK);
   }

   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @GetMapping
   public ResponseEntity<List<User>> getAllUsers() {
      List<User> users = userService.getAllUsers();
      return new ResponseEntity<>(users, HttpStatus.OK);
   }

   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PutMapping("{id}")
   public ResponseEntity<User> updateUser(@PathVariable("id") Long userId,
                                          @RequestBody User user) {
      user.setId(userId);
      User updatedUser = userService.updateUser(user);
      return new ResponseEntity<>(updatedUser, HttpStatus.OK);
   }

   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @DeleteMapping("{id}")
   public ResponseEntity<String> deleteUser(@PathVariable("id") Long userId) {
      userService.deleteUser(userId);
      return new ResponseEntity<>("User successfully deleted!", HttpStatus.OK);
   }

   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping("/byemail")
   public ResponseEntity<String> getUserIdByEmail(@RequestBody EmailAdress email, BindingResult bindingResult) {
      if (bindingResult.hasErrors()) {
         List<String> errors = bindingResult.getFieldErrors().stream()
                 .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                 .collect(Collectors.toList());
         JsonArray arr = new JsonArray();
         errors.forEach(arr::add);
         JsonObject obj = new JsonObject();
         obj.add("message", arr);
         return ResponseEntity.badRequest().body(new Gson().toJson(obj));
      }

      User user = userService.findByEmail(email.getEmail());
      if (user == null) {
         JsonObject obj = new JsonObject();
         obj.addProperty("message", "No user found with this email");
         return ResponseEntity.badRequest().body(new Gson().toJson(obj));
      }

      JsonObject obj = new JsonObject();
      obj.addProperty("answer", user.getId());
      return ResponseEntity.accepted().body(new Gson().toJson(obj));
   }

   /********************************/
  /////           PASSWORD        //

   private boolean isPasswordStrong(String password) {
      return password != null && password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
   }


   /**********************************/
   /*             CAPTCHA            */
   /*********************************/
   private boolean isCaptchaValid(String token) {
      try {
         String secret = "6Lf-2V8rAAAAAOKbakqsJAAm7MblgYF8YtATqGT1";
         String url = "https://www.google.com/recaptcha/api/siteverify";
         String params = "secret=" + secret + "&response=" + token;

         java.net.URL obj = new java.net.URL(url);
         java.net.HttpURLConnection con = (java.net.HttpURLConnection) obj.openConnection();
         con.setRequestMethod("POST");
         con.setDoOutput(true);
         java.io.OutputStream os = con.getOutputStream();
         os.write(params.getBytes());
         os.flush();
         os.close();

         java.io.InputStream is = con.getInputStream();
         java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
         String response = s.hasNext() ? s.next() : "";
         s.close();

         return response.contains("\"success\": true");
      } catch (Exception e) {
         System.out.println("Captcha verification failed: " + e.getMessage());
         return false;
      }
   }


   // ========================================
   // Passwort-Reset-Feature
   // ========================================

   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping("/auth/request-reset")
   public ResponseEntity<?> requestReset(@RequestBody Map<String, String> body) {
      String email = body.get("email");
      String token = userService.createResetToken(email);
      System.out.println("Reset link: http://localhost:5173/user/reset-password?token=" + token);
      return ResponseEntity.ok(Map.of("message", "Reset link sent."));
   }

   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping("/auth/reset-password")
   public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
      String token = body.get("token");
      String newPassword = body.get("newPassword");
      userService.resetPassword(token, newPassword);
      return ResponseEntity.ok(Map.of("message", "Password reset successful."));
   }

}
