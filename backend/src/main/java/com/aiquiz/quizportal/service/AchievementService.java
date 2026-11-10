package com.aiquiz.quizportal.service;

import com.aiquiz.quizportal.entity.QuizAttempt;
import com.aiquiz.quizportal.entity.UserAchievement;
import com.aiquiz.quizportal.entity.UserStreak;
import com.aiquiz.quizportal.repository.BookmarkedQuestionRepository;
import com.aiquiz.quizportal.repository.QuizAttemptRepository;
import com.aiquiz.quizportal.repository.UserAchievementRepository;
import com.aiquiz.quizportal.repository.UserStreakRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AchievementService {

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private BookmarkedQuestionRepository bookmarkedQuestionRepository;

    @Autowired
    private UserStreakRepository userStreakRepository;

    public static class BadgeDefinition {
        private String code;
        private String title;
        private String description;
        private String icon;

        public BadgeDefinition(String code, String title, String description, String icon) {
            this.code = code;
            this.title = title;
            this.description = description;
            this.icon = icon;
        }

        public String getCode() { return code; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }

    private static final List<BadgeDefinition> ALL_BADGES = Arrays.asList(
            new BadgeDefinition("FIRST_STEP", "First Step", "Completed your very first quiz", "fa-flag-checkered"),
            new BadgeDefinition("QUIZ_ENTHUSIAST", "Quiz Enthusiast", "Completed 5 quizzes on the portal", "fa-bolt"),
            new BadgeDefinition("QUIZ_MASTER", "Quiz Master", "Completed 10 or more quizzes", "fa-crown"),
            new BadgeDefinition("PERFECTIONIST", "Perfectionist", "Achieved a 100% score on a quiz", "fa-star"),
            new BadgeDefinition("KNOWLEDGE_SEEKER", "Knowledge Seeker", "Bookmarked 3 or more questions for review", "fa-bookmark"),
            new BadgeDefinition("STREAK_WARRIOR", "Streak Warrior", "Maintained a 3-day daily challenge streak", "fa-fire"),
            new BadgeDefinition("STREAK_CHAMPION", "Streak Champion", "Maintained a 7-day daily challenge streak", "fa-award"),
            new BadgeDefinition("ALL_ROUNDER", "All-Rounder", "Attempted quizzes across 3 or more subjects", "fa-layer-group")
    );

    @Transactional
    public List<Map<String, Object>> getUserBadges(Integer userId) {
        evaluateAndUnlock(userId);

        List<UserAchievement> unlocked = userAchievementRepository.findByUserIdOrderByUnlockedAtDesc(userId);
        Map<String, LocalDateTime> unlockedMap = new HashMap<>();
        for (UserAchievement ua : unlocked) {
            unlockedMap.put(ua.getBadgeCode(), ua.getUnlockedAt());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (BadgeDefinition def : ALL_BADGES) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", def.getCode());
            item.put("title", def.getTitle());
            item.put("description", def.getDescription());
            item.put("icon", def.getIcon());
            boolean isUnlocked = unlockedMap.containsKey(def.getCode());
            item.put("unlocked", isUnlocked);
            item.put("unlockedAt", isUnlocked ? unlockedMap.get(def.getCode()) : null);
            result.add(item);
        }

        return result;
    }

    @Transactional
    public void evaluateAndUnlock(Integer userId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByUserId(userId);
        int quizCount = (attempts != null) ? attempts.size() : 0;

        // 1. FIRST_STEP
        if (quizCount >= 1) {
            unlockBadge(userId, "FIRST_STEP", "First Step", "Completed your very first quiz", "fa-flag-checkered");
        }

        // 2. QUIZ_ENTHUSIAST
        if (quizCount >= 5) {
            unlockBadge(userId, "QUIZ_ENTHUSIAST", "Quiz Enthusiast", "Completed 5 quizzes on the portal", "fa-bolt");
        }

        // 3. QUIZ_MASTER
        if (quizCount >= 10) {
            unlockBadge(userId, "QUIZ_MASTER", "Quiz Master", "Completed 10 or more quizzes", "fa-crown");
        }

        // 4. PERFECTIONIST
        if (attempts != null) {
            boolean hasPerfectScore = attempts.stream()
                    .anyMatch(a -> a.getTotalQuestions() != null && a.getTotalQuestions() > 0
                            && a.getScore() != null && a.getScore().equals(a.getTotalQuestions()));
            if (hasPerfectScore) {
                unlockBadge(userId, "PERFECTIONIST", "Perfectionist", "Achieved a 100% score on a quiz", "fa-star");
            }
        }

        // 5. ALL_ROUNDER (>= 3 distinct categories)
        if (attempts != null) {
            long distinctCategories = attempts.stream()
                    .map(QuizAttempt::getCategoryId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();
            if (distinctCategories >= 3) {
                unlockBadge(userId, "ALL_ROUNDER", "All-Rounder", "Attempted quizzes across 3 or more subjects", "fa-layer-group");
            }
        }

        // 6. KNOWLEDGE_SEEKER (>= 3 bookmarks)
        try {
            int bookmarkCount = bookmarkedQuestionRepository.findByUserIdOrderByCreatedAtDesc(userId).size();
            if (bookmarkCount >= 3) {
                unlockBadge(userId, "KNOWLEDGE_SEEKER", "Knowledge Seeker", "Bookmarked 3 or more questions for review", "fa-bookmark");
            }
        } catch (Exception ignored) {}

        // 7 & 8. STREAKS
        try {
            Optional<UserStreak> streakOpt = userStreakRepository.findByUserId(userId);
            if (streakOpt.isPresent()) {
                int streak = streakOpt.get().getCurrentStreak();
                if (streak >= 3) {
                    unlockBadge(userId, "STREAK_WARRIOR", "Streak Warrior", "Maintained a 3-day daily challenge streak", "fa-fire");
                }
                if (streak >= 7) {
                    unlockBadge(userId, "STREAK_CHAMPION", "Streak Champion", "Maintained a 7-day daily challenge streak", "fa-award");
                }
            }
        } catch (Exception ignored) {}
    }

    private void unlockBadge(Integer userId, String code, String title, String description, String icon) {
        if (!userAchievementRepository.existsByUserIdAndBadgeCode(userId, code)) {
            UserAchievement ua = new UserAchievement(userId, code, title, description, icon);
            userAchievementRepository.save(ua);
        }
    }
}
