package com.aiquiz.quizportal.controller;

import com.aiquiz.quizportal.config.UserPrincipal;
import com.aiquiz.quizportal.entity.QuizAttempt;
import com.aiquiz.quizportal.repository.QuizAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attempts")
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500",
        "https://comforting-gumdrop-a9db8a.netlify.app"
})
public class QuizAttemptController {

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    // ==========================================
    // SAVE QUIZ ATTEMPT (AUTHENTICATED)
    // Student identity is strictly derived from the authenticated principal
    // ==========================================

    @PostMapping("/save")
    public ResponseEntity<?> saveAttempt(
            @RequestBody QuizAttempt attempt,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required to submit quiz attempt"));
        }

        if (attempt == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Quiz attempt data is required"));
        }

        // Enforce user ID from verified server-side principal to prevent spoofing
        attempt.setUserId(principal.getId());

        QuizAttempt saved = quizAttemptRepository.save(attempt);
        return ResponseEntity.ok(saved);
    }

    // ==========================================
    // GET USER'S OWN QUIZ HISTORY (AUTHENTICATED)
    // Users can only view their own history unless they are an admin
    // ==========================================

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserAttempts(
            @PathVariable Integer userId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_ADMIN"));

        if (!isAdmin && !principal.getId().equals(userId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access Denied: You can only view your own quiz history"));
        }

        List<QuizAttempt> attempts = quizAttemptRepository.findByUserId(userId);
        return ResponseEntity.ok(attempts);
    }

    // ==========================================
    // GET ALL QUIZ ATTEMPTS - ADMIN ONLY
    // Handled by Spring Security role check, with fallback check
    // ==========================================

    @GetMapping("/admin")
    public ResponseEntity<?> getAllAttemptsForAdmin(
            @RequestParam(required = false) Integer userId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal != null) {
            boolean isAdmin = principal.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_ADMIN"));
            if (!isAdmin) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Access Denied. Admin only."));
            }
        }

        List<QuizAttempt> attempts = quizAttemptRepository.findAll();
        return ResponseEntity.ok(attempts);
    }
}
