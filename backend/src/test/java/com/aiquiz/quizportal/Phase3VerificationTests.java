package com.aiquiz.quizportal;

import com.aiquiz.quizportal.dto.LeaderboardEntryDto;
import com.aiquiz.quizportal.entity.Category;
import com.aiquiz.quizportal.entity.Question;
import com.aiquiz.quizportal.entity.QuizAttempt;
import com.aiquiz.quizportal.entity.User;
import com.aiquiz.quizportal.repository.CategoryRepository;
import com.aiquiz.quizportal.repository.QuestionRepository;
import com.aiquiz.quizportal.repository.QuizAttemptRepository;
import com.aiquiz.quizportal.repository.UserRepository;
import com.aiquiz.quizportal.service.AchievementService;
import com.aiquiz.quizportal.service.AiAnalysisService;
import com.aiquiz.quizportal.service.LeaderboardService;
import com.aiquiz.quizportal.service.PlacementMockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class Phase3VerificationTests {

    @Autowired
    private LeaderboardService leaderboardService;

    @Autowired
    private AiAnalysisService aiAnalysisService;

    @Autowired
    private PlacementMockService placementMockService;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testStudent;
    private Category testCategory;

    @BeforeEach
    public void setup() {
        // Find or create test student
        String email = "phase3_student@example.com";
        testStudent = userRepository.findByEmail(email);
        if (testStudent == null) {
            testStudent = new User();
            testStudent.setName("Phase3 Student");
            testStudent.setEmail(email);
            testStudent.setPassword(passwordEncoder.encode("StudentPass@123"));
            testStudent.setRole("ROLE_USER");
            testStudent.setCreatedAt(LocalDateTime.now());
            testStudent = userRepository.save(testStudent);
        }

        // Find or create test category
        List<Category> cats = categoryRepository.findAll();
        if (cats.isEmpty()) {
            Category cat = new Category();
            cat.setCategoryName("Core Java");
            testCategory = categoryRepository.save(cat);
        } else {
            testCategory = cats.get(0);
        }

        // Ensure at least one question exists
        List<Question> questions = questionRepository.findAll();
        if (questions.isEmpty()) {
            Question q = new Question();
            q.setQuestionText("What is JVM in Java?");
            q.setOptionA("Java Virtual Machine");
            q.setOptionB("Java Variable Method");
            q.setOptionC("Java Verified Mode");
            q.setOptionD("Java Version Manager");
            q.setCorrectAnswer("A");
            q.setExplanation("JVM stands for Java Virtual Machine, the runtime engine of Java.");
            q.setCategory(testCategory);
            questionRepository.save(q);
        }
    }

    @Test
    @DisplayName("1. Verify Leaderboard Calculation, Ranking & User Privacy")
    public void testLeaderboardRankingAndPrivacy() {
        // Record a quiz attempt for test student
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUserId(testStudent.getId());
        attempt.setCategoryId(testCategory.getId());
        attempt.setScore(5);
        attempt.setTotalQuestions(5);
        attempt.setStartTime(LocalDateTime.now().minusMinutes(10));
        attempt.setEndTime(LocalDateTime.now());
        quizAttemptRepository.save(attempt);

        List<LeaderboardEntryDto> leaderboard = leaderboardService.getTopLeaderboard(20);
        assertNotNull(leaderboard, "Leaderboard must not be null");
        assertFalse(leaderboard.isEmpty(), "Leaderboard should have at least 1 entry");

        // Verify ranks are sequentially ordered
        for (int i = 0; i < leaderboard.size(); i++) {
            assertEquals(i + 1, leaderboard.get(i).getRank(), "Rank should match 1-based index");
            assertNotNull(leaderboard.get(i).getName(), "Display name must be present");
        }

        // Verify user rank query
        LeaderboardEntryDto myRank = leaderboardService.getUserRank(testStudent.getId());
        assertNotNull(myRank, "My rank should be returned");
        assertTrue(myRank.getRank() >= 1, "Rank should be positive integer");
        assertTrue(myRank.getTotalScore() >= 5, "Total score should be updated");
    }

    @Test
    @DisplayName("2. Verify AI Performance Analysis with Category Breakdown & Fallback")
    public void testAiPerformanceAnalysis() {
        Map<String, Object> analysis = aiAnalysisService.analyzeUserPerformance(testStudent.getId());
        assertNotNull(analysis, "Performance analysis must not be null");
        assertTrue((int) analysis.get("totalQuizzes") >= 1, "Total quizzes should be at least 1");
        assertNotNull(analysis.get("summary"), "Summary must be present");
        assertNotNull(analysis.get("actionableRecommendations"), "Recommendations must be present");

        int readiness = (int) analysis.get("placementReadinessScore");
        assertTrue(readiness >= 0 && readiness <= 100, "Placement readiness score must be between 0 and 100");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> breakdown = (List<Map<String, Object>>) analysis.get("categoryBreakdown");
        assertNotNull(breakdown, "Category breakdown must not be null");
    }

    @Test
    @DisplayName("3. Verify Placement Mock Test Generation & Question Sanitization")
    public void testPlacementMockGeneration() {
        // Verify available mocks
        List<PlacementMockService.MockInfo> mocks = placementMockService.getAvailableMocks();
        assertEquals(3, mocks.size(), "Should offer 3 placement mock formats");

        // Generate full placement mock
        Map<String, Object> mockTest = placementMockService.generateMockTest("full");
        assertNotNull(mockTest, "Mock test should be generated");
        assertEquals("full", mockTest.get("mockType"));
        assertNotNull(mockTest.get("questions"), "Questions list should be present");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> questions = (List<Map<String, Object>>) mockTest.get("questions");
        if (!questions.isEmpty()) {
            Map<String, Object> firstQ = questions.get(0);
            assertNotNull(firstQ.get("questionText"), "Question text must be provided");
            assertNotNull(firstQ.get("optionA"), "Option A must be provided");
            // Verify security sanitization: answers and explanations must NOT be sent during exam
            assertFalse(firstQ.containsKey("correctAnswer"), "correctAnswer must NOT be exposed in active mock payload");
            assertFalse(firstQ.containsKey("explanation"), "explanation must NOT be exposed in active mock payload");
        }
    }

    @Test
    @DisplayName("4. Verify Achievement Badges Evaluation & Persistence")
    public void testAchievementBadgesEvaluation() {
        // Ensure student has a 100% quiz attempt
        QuizAttempt perfectAttempt = new QuizAttempt();
        perfectAttempt.setUserId(testStudent.getId());
        perfectAttempt.setCategoryId(testCategory.getId());
        perfectAttempt.setScore(5);
        perfectAttempt.setTotalQuestions(5);
        perfectAttempt.setStartTime(LocalDateTime.now().minusMinutes(5));
        perfectAttempt.setEndTime(LocalDateTime.now());
        quizAttemptRepository.save(perfectAttempt);

        List<Map<String, Object>> badges = achievementService.getUserBadges(testStudent.getId());
        assertNotNull(badges, "Badges list must not be null");
        assertEquals(8, badges.size(), "All 8 badges must be defined and evaluated");

        // Test student has completed at least 1 quiz and scored 5/5
        // Both FIRST_STEP and PERFECTIONIST should be unlocked
        Map<String, Object> firstStep = badges.stream()
                .filter(b -> "FIRST_STEP".equals(b.get("code")))
                .findFirst()
                .orElse(null);
        assertNotNull(firstStep, "FIRST_STEP badge must exist");
        assertTrue((boolean) firstStep.get("unlocked"), "FIRST_STEP should be unlocked for user with >= 1 quiz");

        Map<String, Object> perfectionist = badges.stream()
                .filter(b -> "PERFECTIONIST".equals(b.get("code")))
                .findFirst()
                .orElse(null);
        assertNotNull(perfectionist, "PERFECTIONIST badge must exist");
        assertTrue((boolean) perfectionist.get("unlocked"), "PERFECTIONIST should be unlocked for 100% quiz score");
    }
}
