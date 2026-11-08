package com.aiquiz.quizportal.controller;

import com.aiquiz.quizportal.config.UserPrincipal;
import com.aiquiz.quizportal.service.DailyChallengeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/daily-challenge")
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500",
        "https://comforting-gumdrop-a9db8a.netlify.app"
})
public class DailyChallengeController {

    @Autowired
    private DailyChallengeService dailyChallengeService;

    @GetMapping("/today")
    public ResponseEntity<?> getTodayChallenge(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Authentication required"));
        }

        Map<String, Object> challenge = dailyChallengeService.getTodayChallenge(principal.getId());
        return ResponseEntity.ok(challenge);
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitDailyChallenge(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Authentication required"));
        }

        Integer score = body.get("score") != null ? Integer.parseInt(body.get("score").toString()) : 0;
        Integer totalQuestions = body.get("totalQuestions") != null ? Integer.parseInt(body.get("totalQuestions").toString()) : 5;

        try {
            Map<String, Object> result = dailyChallengeService.submitDailyChallenge(principal.getId(), score, totalQuestions);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/streak")
    public ResponseEntity<?> getStreak(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Authentication required"));
        }

        Map<String, Object> streak = dailyChallengeService.getStreakInfo(principal.getId());
        return ResponseEntity.ok(streak);
    }
}
