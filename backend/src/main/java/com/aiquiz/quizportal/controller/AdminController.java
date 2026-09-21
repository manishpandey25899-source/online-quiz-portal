package com.aiquiz.quizportal.controller;

import com.aiquiz.quizportal.entity.User;
import com.aiquiz.quizportal.repository.QuestionRepository;
import com.aiquiz.quizportal.repository.QuizAttemptRepository;
import com.aiquiz.quizportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;


    // ==========================================
    // CHECK ADMIN USER
    // ==========================================

    private ResponseEntity<?> checkAdmin(Integer userId) {

        if (userId == null) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Access Denied");
        }


        Optional<User> userOptional =
                userRepository.findById(userId);


        if (userOptional.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Access Denied");
        }


        User user =
                userOptional.get();


        if (
                user.getRole() == null ||
                        !user.getRole().equalsIgnoreCase("admin")
        ) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Access Denied. Admin only.");
        }


        return null;
    }


    // ==========================================
    // ADMIN STATISTICS
    // ==========================================

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(
            @RequestParam Integer userId
    ) {

        ResponseEntity<?> accessCheck =
                checkAdmin(userId);


        if (accessCheck != null) {
            return accessCheck;
        }


        Map<String, Long> stats =
                new HashMap<>();


        stats.put(
                "users",
                userRepository.count()
        );


        stats.put(
                "questions",
                questionRepository.count()
        );


        stats.put(
                "attempts",
                quizAttemptRepository.count()
        );


        return ResponseEntity.ok(stats);
    }


    // ==========================================
    // GET ALL USERS - ADMIN ONLY
    // ==========================================

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam Integer userId
    ) {

        ResponseEntity<?> accessCheck =
                checkAdmin(userId);


        if (accessCheck != null) {
            return accessCheck;
        }


        List<User> users =
                userRepository.findAll();


        List<Map<String, Object>> userList =
                new ArrayList<>();


        for (User user : users) {

            Map<String, Object> userData =
                    new LinkedHashMap<>();


            userData.put(
                    "id",
                    user.getId()
            );


            userData.put(
                    "name",
                    user.getName()
            );


            userData.put(
                    "email",
                    user.getEmail()
            );


            userData.put(
                    "role",
                    user.getRole()
            );


            userData.put(
                    "createdAt",
                    user.getCreatedAt()
            );


            userList.add(userData);
        }


        return ResponseEntity.ok(
                userList
        );
    }


    // ==========================================
    // UPDATE USER ROLE - ADMIN ONLY
    // ==========================================

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Integer id,
            @RequestParam Integer userId,
            @RequestParam String role
    ) {

        ResponseEntity<?> accessCheck =
                checkAdmin(userId);


        if (accessCheck != null) {
            return accessCheck;
        }


        // Prevent admin from changing
        // their own role accidentally
        if (id.equals(userId)) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "You cannot change your own admin role."
                    );
        }


        // Only these two roles are allowed
        if (
                !role.equalsIgnoreCase("admin") &&
                        !role.equalsIgnoreCase("student")
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Role must be admin or student."
                    );
        }


        Optional<User> userOptional =
                userRepository.findById(id);


        if (userOptional.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User Not Found");
        }


        User user =
                userOptional.get();


        user.setRole(
                role.toLowerCase()
        );


        User updatedUser =
                userRepository.save(user);


        Map<String, Object> response =
                new LinkedHashMap<>();


        response.put(
                "id",
                updatedUser.getId()
        );


        response.put(
                "name",
                updatedUser.getName()
        );


        response.put(
                "email",
                updatedUser.getEmail()
        );


        response.put(
                "role",
                updatedUser.getRole()
        );


        response.put(
                "createdAt",
                updatedUser.getCreatedAt()
        );


        return ResponseEntity.ok(
                response
        );
    }
}