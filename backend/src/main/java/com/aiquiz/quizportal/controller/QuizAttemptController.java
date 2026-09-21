package com.aiquiz.quizportal.controller;

import com.aiquiz.quizportal.entity.QuizAttempt;
import com.aiquiz.quizportal.entity.User;
import com.aiquiz.quizportal.repository.QuizAttemptRepository;
import com.aiquiz.quizportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/attempts")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class QuizAttemptController {

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private UserRepository userRepository;


    // ==========================================
    // SAVE QUIZ ATTEMPT
    // ==========================================

    @PostMapping("/save")
    public QuizAttempt saveAttempt(
            @RequestBody QuizAttempt attempt) {

        return quizAttemptRepository.save(attempt);
    }


    // ==========================================
    // GET USER'S OWN QUIZ HISTORY
    // ==========================================

    @GetMapping("/user/{userId}")
    public List<QuizAttempt> getUserAttempts(
            @PathVariable Integer userId) {

        return quizAttemptRepository.findByUserId(userId);
    }


    // ==========================================
    // GET ALL QUIZ ATTEMPTS - ADMIN ONLY
    // ==========================================

    @GetMapping("/admin")
    public ResponseEntity<?> getAllAttemptsForAdmin(
            @RequestParam Integer userId) {

        // Find user
        Optional<User> userOptional =
                userRepository.findById(userId);


        // User not found
        if (userOptional.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Access Denied");
        }


        // Get user
        User user =
                userOptional.get();


        // Check admin role
        if (
                user.getRole() == null ||
                        !user.getRole().equalsIgnoreCase("admin")
        ) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Access Denied. Admin only.");
        }


        // Return all attempts
        List<QuizAttempt> attempts =
                quizAttemptRepository.findAll();


        return ResponseEntity.ok(attempts);
    }
}