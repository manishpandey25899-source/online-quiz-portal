package com.aiquiz.quizportal.controller;

import com.aiquiz.quizportal.entity.User;
import com.aiquiz.quizportal.repository.UserRepository;
import com.aiquiz.quizportal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500",
        "https://comforting-gumdrop-a9db8a.netlify.app"
})
public class RegisterController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // Name: only English letters and spaces, 2 to 50 characters
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-z][A-Za-z ]{1,49}$");

    // Basic valid email format
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Minimum 8 characters, at least one letter and one number
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,64}$");

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {

        // -------------------------
        // NULL CHECK
        // -------------------------

        if (user == null) {
            return ResponseEntity
                    .badRequest()
                    .body("Registration data is required.");
        }

        // -------------------------
        // NAME VALIDATION
        // -------------------------

        String name = user.getName();

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Full name is required.");
        }

        name = name.trim().replaceAll("\\s+", " ");

        if (!NAME_PATTERN.matcher(name).matches()) {
            return ResponseEntity
                    .badRequest()
                    .body("Please enter a valid name using letters and spaces only.");
        }

        // -------------------------
        // EMAIL VALIDATION
        // -------------------------

        String email = user.getEmail();

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Email address is required.");
        }

        email = email.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ResponseEntity
                    .badRequest()
                    .body("Please enter a valid email address.");
        }

        // -------------------------
        // DUPLICATE EMAIL CHECK
        // -------------------------

        if (userRepository.findByEmail(email) != null) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("An account with this email already exists.");
        }

        // -------------------------
        // PASSWORD VALIDATION
        // -------------------------

        String password = user.getPassword();

        if (password == null || password.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Password is required.");
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return ResponseEntity
                    .badRequest()
                    .body("Password must be at least 8 characters long and contain at least one letter and one number.");
        }

        // -------------------------
        // FORCE NORMAL USER ROLE
        // -------------------------

        user.setName(name);
        user.setEmail(email);

        // New registrations are always students.
        // User cannot register himself as admin.
        user.setRole("student");

        // -------------------------
        // SAVE USER
        // -------------------------

        User savedUser = userService.saveUser(user);

        // Do not return password in response.
        savedUser.setPassword(null);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedUser);
    }
}