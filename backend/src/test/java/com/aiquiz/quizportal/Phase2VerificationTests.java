package com.aiquiz.quizportal;

import com.aiquiz.quizportal.entity.Question;
import com.aiquiz.quizportal.entity.User;
import com.aiquiz.quizportal.repository.QuestionRepository;
import com.aiquiz.quizportal.repository.UserRepository;
import com.aiquiz.quizportal.service.BookmarkService;
import com.aiquiz.quizportal.service.DailyChallengeService;
import com.aiquiz.quizportal.service.PracticeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class Phase2VerificationTests {

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private DailyChallengeService dailyChallengeService;

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Phase 2: Test Bookmark lifecycle (Add, Duplicate prevention, List, Remove)")
    void testBookmarkLifecycle() {
        System.out.println("--> Testing Bookmark lifecycle...");

        // Ensure a test user exists
        String testEmail = "bookmark_test_user@college.edu";
        User user = userRepository.findByEmail(testEmail);
        if (user == null) {
            user = new User();
            user.setName("Bookmark Tester");
            user.setEmail(testEmail);
            user.setPassword("$2a$10$dummyHashForTestingPhase2Only1234567890abcdef");
            user.setRole("student");
            user.setCreatedAt(LocalDateTime.now());
            user = userRepository.save(user);
        }

        // Get a valid question
        List<Question> questions = questionRepository.findAll();
        assertFalse(questions.isEmpty(), "Questions table must have questions");
        Question targetQ = questions.get(0);

        // Clean previous bookmarks
        jdbcTemplate.update("DELETE FROM bookmarked_questions WHERE user_id = ?", user.getId());

        // 1. Add Bookmark
        boolean added = bookmarkService.addBookmark(user.getId(), targetQ.getId());
        assertTrue(added, "Bookmark should be added successfully");

        // 2. Duplicate prevention
        boolean duplicate = bookmarkService.addBookmark(user.getId(), targetQ.getId());
        assertFalse(duplicate, "Duplicate bookmark must return false");

        // 3. List Bookmarks
        List<Question> bookmarkedQuestions = bookmarkService.getBookmarkedQuestions(user.getId());
        assertEquals(1, bookmarkedQuestions.size());
        assertEquals(targetQ.getId(), bookmarkedQuestions.get(0).getId());

        List<Integer> ids = bookmarkService.getBookmarkedQuestionIds(user.getId());
        assertTrue(ids.contains(targetQ.getId()));

        // 4. Remove Bookmark
        boolean removed = bookmarkService.removeBookmark(user.getId(), targetQ.getId());
        assertTrue(removed, "Bookmark should be removed successfully");
        assertEquals(0, bookmarkService.getBookmarkedQuestions(user.getId()).size());

        // Clean up
        userRepository.delete(user);
        System.out.println("Bookmark lifecycle test PASSED.");
    }

    @Test
    @DisplayName("Phase 2: Test Daily Challenge (Generate, Today check, Submit, Streak tracking, Duplicate block)")
    void testDailyChallengeLifecycle() {
        System.out.println("--> Testing Daily Challenge lifecycle...");

        String testEmail = "daily_test_user@college.edu";
        User user = userRepository.findByEmail(testEmail);
        if (user == null) {
            user = new User();
            user.setName("Daily Tester");
            user.setEmail(testEmail);
            user.setPassword("$2a$10$dummyHashForTestingPhase2Only1234567890abcdef");
            user.setRole("student");
            user.setCreatedAt(LocalDateTime.now());
            user = userRepository.save(user);
        }

        final Integer finalUserId = user.getId();

        // Clean test daily attempts and streaks
        jdbcTemplate.update("DELETE FROM daily_challenge_attempts WHERE user_id = ?", finalUserId);
        jdbcTemplate.update("DELETE FROM user_streaks WHERE user_id = ?", finalUserId);

        // 1. Get Today Challenge
        Map<String, Object> challenge = dailyChallengeService.getTodayChallenge(finalUserId);
        assertNotNull(challenge.get("challengeDate"));
        assertEquals(false, challenge.get("alreadyCompleted"));
        List<?> qs = (List<?>) challenge.get("questions");
        assertNotNull(qs);
        assertEquals(5, qs.size(), "Daily challenge must contain exactly 5 questions");

        // 2. Submit Daily Challenge
        Map<String, Object> submitResult = dailyChallengeService.submitDailyChallenge(finalUserId, 4, 5);
        assertEquals(true, submitResult.get("success"));
        assertEquals(4, submitResult.get("score"));
        assertEquals(1, submitResult.get("currentStreak"), "First completion must set streak to 1");

        // 3. Duplicate submission prevention
        assertThrows(IllegalArgumentException.class, () -> {
            dailyChallengeService.submitDailyChallenge(finalUserId, 5, 5);
        }, "Submitting twice on the same day must be rejected");

        // 4. Check today challenge status is now completed
        Map<String, Object> afterSubmit = dailyChallengeService.getTodayChallenge(finalUserId);
        assertEquals(true, afterSubmit.get("alreadyCompleted"));
        assertEquals(4, afterSubmit.get("score"));
        assertEquals(1, afterSubmit.get("currentStreak"));

        // Clean up
        jdbcTemplate.update("DELETE FROM daily_challenge_attempts WHERE user_id = ?", finalUserId);
        jdbcTemplate.update("DELETE FROM user_streaks WHERE user_id = ?", finalUserId);
        userRepository.delete(user);
        System.out.println("Daily Challenge lifecycle test PASSED.");
    }

    @Test
    @DisplayName("Phase 2: Test Practice API (By IDs & Explanations)")
    void testPracticeAndExplanations() {
        System.out.println("--> Testing Practice & Explanations...");

        List<Question> all = questionRepository.findAll();
        assertFalse(all.isEmpty());

        Question q1 = all.get(0);
        q1.setExplanation("The explanation for question 1");
        questionRepository.save(q1);

        Question reloaded = questionRepository.findById(q1.getId()).orElseThrow();
        assertEquals("The explanation for question 1", reloaded.getExplanation());

        // Test practice retrieval by IDs
        List<Question> practiceList = practiceService.getQuestionsByIds(List.of(q1.getId()));
        assertEquals(1, practiceList.size());
        assertEquals(q1.getId(), practiceList.get(0).getId());

        System.out.println("Practice & Explanations test PASSED.");
    }
}
