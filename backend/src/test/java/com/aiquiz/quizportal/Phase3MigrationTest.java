package com.aiquiz.quizportal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class Phase3MigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void applyPhase3Migration() {
        System.out.println("--> Checking and applying Phase 3 user_achievements table...");

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS user_achievements (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                badge_code VARCHAR(50) NOT NULL,
                title VARCHAR(100) NOT NULL,
                description VARCHAR(255) NOT NULL,
                icon VARCHAR(50) NOT NULL,
                unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY unique_user_badge (user_id, badge_code),
                CONSTRAINT fk_achievements_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
        """);

        List<String> tables = jdbcTemplate.query(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()",
                (rs, rowNum) -> rs.getString("table_name")
        );
        assertTrue(tables.contains("user_achievements"), "user_achievements table must exist");
        System.out.println("--> Phase 3 user_achievements table verified on MySQL!");
    }
}
