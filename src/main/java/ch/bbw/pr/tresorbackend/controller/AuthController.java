package ch.bbw.pr.tresorbackend.controller;

import ch.bbw.pr.tresorbackend.model.EncryptCredentials;
import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.service.PasswordEncryptionService;
import ch.bbw.pr.tresorbackend.service.UserService;
import ch.bbw.pr.tresorbackend.util.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncryptionService passwordService;
    private final JwtUtil jwtUtil;

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody EncryptCredentials credentials) {
        User user = userService.findByEmail(credentials.getEmail());
        if (user == null || !passwordService.checkPassword(credentials.getEncryptPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
