package com.aiquiz.quizportal.controller;

import com.aiquiz.quizportal.config.UserPrincipal;
import com.aiquiz.quizportal.entity.User;
import com.aiquiz.quizportal.repository.QuestionRepository;
import com.aiquiz.quizportal.repository.QuizAttemptRepository;
import com.aiquiz.quizportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500",
        "https://comforting-gumdrop-a9db8a.netlify.app"
})
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    // ==========================================
    // CHECK ADMIN USER
    // Validates authenticated principal first, falls back to userId parameter
    // ==========================================

    private ResponseEntity<?> checkAdmin(Integer userId, UserPrincipal principal) {

        if (principal != null) {
            if (principal.getRole() != null && principal.getRole().equalsIgnoreCase("admin")) {
                return null; // Access granted
            }
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access Denied. Admin only."));
        }

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access Denied"));
        }

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access Denied"));
        }

        User user = userOptional.get();

        if (user.getRole() == null || !user.getRole().equalsIgnoreCase("admin")) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access Denied. Admin only."));
        }

        return null;
    }

    // ==========================================
    // ADMIN STATISTICS
    // ==========================================

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(
            @RequestParam(required = false) Integer userId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        ResponseEntity<?> accessCheck = checkAdmin(userId, principal);

        if (accessCheck != null) {
            return accessCheck;
        }

        Map<String, Long> stats = new HashMap<>();

        stats.put("users", userRepository.count());
        stats.put("questions", questionRepository.count());
        stats.put("attempts", quizAttemptRepository.count());

        return ResponseEntity.ok(stats);
    }

    // ==========================================
    // GET ALL USERS - ADMIN ONLY
    // ==========================================

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) Integer userId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        ResponseEntity<?> accessCheck = checkAdmin(userId, principal);

        if (accessCheck != null) {
            return accessCheck;
        }

        List<User> users = userRepository.findAll();

        List<Map<String, Object>> userList = new ArrayList<>();

        for (User user : users) {

            Map<String, Object> userData = new LinkedHashMap<>();

            userData.put("id", user.getId());
            userData.put("name", user.getName());
            userData.put("email", user.getEmail());
            userData.put("role", user.getRole());
            userData.put("createdAt", user.getCreatedAt());

            userList.add(userData);
        }

        return ResponseEntity.ok(userList);
    }

    // ==========================================
    // UPDATE USER ROLE - ADMIN ONLY
    // ==========================================

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer userId,
            @RequestParam String role,
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        ResponseEntity<?> accessCheck = checkAdmin(userId, principal);

        if (accessCheck != null) {
            return accessCheck;
        }

        Integer callerId = (principal != null) ? principal.getId() : userId;

        // Prevent admin from changing their own role accidentally
        if (callerId != null && id.equals(callerId)) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "You cannot change your own admin role."));
        }

        // Only these two roles are allowed
        if (!role.equalsIgnoreCase("admin") && !role.equalsIgnoreCase("student")) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Role must be admin or student."));
        }

        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User Not Found"));
        }

        User user = userOptional.get();

        user.setRole(role.toLowerCase());

        User updatedUser = userRepository.save(user);

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("id", updatedUser.getId());
        response.put("name", updatedUser.getName());
        response.put("email", updatedUser.getEmail());
        response.put("role", updatedUser.getRole());
        response.put("createdAt", updatedUser.getCreatedAt());

        return ResponseEntity.ok(response);
    }
}
