package com.aiquiz.quizportal.controller;

import com.aiquiz.quizportal.config.UserPrincipal;
import com.aiquiz.quizportal.entity.Question;
import com.aiquiz.quizportal.service.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500",
        "https://comforting-gumdrop-a9db8a.netlify.app"
})
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    @PostMapping("/{questionId}")
    public ResponseEntity<?> addBookmark(
            @PathVariable Integer questionId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Authentication required"));
        }

        try {
            boolean added = bookmarkService.addBookmark(principal.getId(), questionId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "bookmarked", true,
                    "message", added ? "Question bookmarked successfully" : "Question is already bookmarked"
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<?> removeBookmark(
            @PathVariable Integer questionId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Authentication required"));
        }

        boolean removed = bookmarkService.removeBookmark(principal.getId(), questionId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "bookmarked", false,
                "message", removed ? "Bookmark removed" : "Bookmark not found"
        ));
    }

    @GetMapping
    public ResponseEntity<?> getBookmarks(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Authentication required"));
        }

        List<Question> questions = bookmarkService.getBookmarkedQuestions(principal.getId());
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/ids")
    public ResponseEntity<?> getBookmarkIds(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Authentication required"));
        }

        List<Integer> ids = bookmarkService.getBookmarkedQuestionIds(principal.getId());
        return ResponseEntity.ok(ids);
    }
}
