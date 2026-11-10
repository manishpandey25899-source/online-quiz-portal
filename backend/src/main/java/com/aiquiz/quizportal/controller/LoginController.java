package com.aiquiz.quizportal.controller;

import com.aiquiz.quizportal.config.JwtUtil;
import com.aiquiz.quizportal.entity.User;
import com.aiquiz.quizportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500",
        "https://comforting-gumdrop-a9db8a.netlify.app"
})
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {

        if (user == null || user.getEmail() == null || user.getPassword() == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Email and password are required."));
        }

        String email = user.getEmail().trim().toLowerCase();
        User dbUser = userRepository.findByEmail(email);

        if (dbUser == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid Email or Password"));
        }

        String dbPassword = dbUser.getPassword();
        boolean passwordMatches = false;

        // Check if password is an existing BCrypt hash ($2a$, $2b$, or $2y$)
        if (dbPassword != null && (dbPassword.startsWith("$2a$") || dbPassword.startsWith("$2b$") || dbPassword.startsWith("$2y$"))) {
            passwordMatches = passwordEncoder.matches(user.getPassword(), dbPassword);
        } else if (dbPassword != null && dbPassword.equals(user.getPassword())) {
            // Legacy plaintext password matched!
            // Gracefully upgrade to BCrypt in database
            dbUser.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(dbUser);
            passwordMatches = true;
        }

        if (!passwordMatches) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid Email or Password"));
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(dbUser.getId(), dbUser.getEmail(), dbUser.getRole(), dbUser.getName());

        // Construct response supporting both new JWT clients and backward-compatible properties
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("token", token);
        response.put("id", dbUser.getId());
        response.put("name", dbUser.getName());
        response.put("email", dbUser.getEmail());
        response.put("role", dbUser.getRole());

        Map<String, Object> userPayload = new LinkedHashMap<>();
        userPayload.put("id", dbUser.getId());
        userPayload.put("name", dbUser.getName());
        userPayload.put("email", dbUser.getEmail());
        userPayload.put("role", dbUser.getRole());
        response.put("user", userPayload);

        return ResponseEntity.ok(response);
    }
}
