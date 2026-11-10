package com.aiquiz.quizportal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class Phase2MigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void applyPhase2Migration() {
        System.out.println("--> Checking and applying Phase 2 migrations...");

        // 1. Check explanation column on questions
        List<String> questionCols = jdbcTemplate.query(
                "SELECT column_name FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'questions'",
                (rs, rowNum) -> rs.getString("column_name")
        );

        if (!questionCols.contains("explanation")) {
            System.out.println("Adding explanation column to questions table...");
            jdbcTemplate.execute("ALTER TABLE questions ADD COLUMN explanation TEXT DEFAULT NULL");
            System.out.println("explanation column added.");
        } else {
            System.out.println("explanation column already exists in questions table.");
        }

        // 2. bookmarked_questions table
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS bookmarked_questions (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                question_id INT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY unique_user_question (user_id, question_id),
                CONSTRAINT fk_bookmarks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                CONSTRAINT fk_bookmarks_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
        """);
        System.out.println("bookmarked_questions table verified.");

        // 3. daily_challenges table
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS daily_challenges (
                id INT AUTO_INCREMENT PRIMARY KEY,
                challenge_date DATE NOT NULL UNIQUE,
                category_id INT DEFAULT NULL,
                question_ids VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
        """);
        System.out.println("daily_challenges table verified.");

        // 4. daily_challenge_attempts table
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS daily_challenge_attempts (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                challenge_date DATE NOT NULL,
                score INT NOT NULL,
                total_questions INT NOT NULL DEFAULT 5,
                completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY unique_user_daily (user_id, challenge_date),
                CONSTRAINT fk_daily_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
        """);
        System.out.println("daily_challenge_attempts table verified.");

        // 5. user_streaks table
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS user_streaks (
                user_id INT PRIMARY KEY,
                current_streak INT DEFAULT 0,
                max_streak INT DEFAULT 0,
                last_completed_date DATE DEFAULT NULL,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                CONSTRAINT fk_streak_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
        """);
        System.out.println("user_streaks table verified.");

        // Verify all tables exist
        List<String> tables = jdbcTemplate.query(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()",
                (rs, rowNum) -> rs.getString("table_name")
        );
        assertTrue(tables.contains("bookmarked_questions"), "bookmarked_questions must exist");
        assertTrue(tables.contains("daily_challenges"), "daily_challenges must exist");
        assertTrue(tables.contains("daily_challenge_attempts"), "daily_challenge_attempts must exist");
        assertTrue(tables.contains("user_streaks"), "user_streaks must exist");

        System.out.println("--> All Phase 2 tables successfully verified on MySQL!");
    }
}
