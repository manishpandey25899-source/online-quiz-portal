package com.aiquiz.quizportal.controller;

import com.aiquiz.quizportal.config.UserPrincipal;
import com.aiquiz.quizportal.entity.Question;
import com.aiquiz.quizportal.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/practice")
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500",
        "https://comforting-gumdrop-a9db8a.netlify.app"
})
public class PracticeController {

    @Autowired
    private PracticeService practiceService;

    @PostMapping("/questions-by-ids")
    public ResponseEntity<?> getQuestionsByIds(@RequestBody Map<String, List<Integer>> requestBody) {
        List<Integer> questionIds = requestBody.get("questionIds");
        List<Question> questions = practiceService.getQuestionsByIds(questionIds);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<?> getBookmarkedPracticeQuestions(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Authentication required"));
        }

        List<Question> questions = practiceService.getBookmarkedQuestionsForPractice(principal.getId());
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getCategoryPracticeQuestions(@PathVariable Integer categoryId) {
        List<Question> questions = practiceService.getAllQuestionsByCategory(categoryId);
        return ResponseEntity.ok(questions);
    }
}
