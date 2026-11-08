package com.aiquiz.quizportal.service;

import com.aiquiz.quizportal.dto.LeaderboardEntryDto;
import com.aiquiz.quizportal.entity.QuizAttempt;
import com.aiquiz.quizportal.entity.User;
import com.aiquiz.quizportal.entity.UserStreak;
import com.aiquiz.quizportal.repository.QuizAttemptRepository;
import com.aiquiz.quizportal.repository.UserRepository;
import com.aiquiz.quizportal.repository.UserStreakRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserStreakRepository userStreakRepository;

    public List<LeaderboardEntryDto> getTopLeaderboard(int limit) {
        List<LeaderboardEntryDto> allRanked = computeRankedLeaderboard();
        if (limit > 0 && allRanked.size() > limit) {
            return allRanked.subList(0, limit);
        }
        return allRanked;
    }

    public LeaderboardEntryDto getUserRank(Integer userId) {
        List<LeaderboardEntryDto> allRanked = computeRankedLeaderboard();
        for (LeaderboardEntryDto entry : allRanked) {
            if (entry.getUserId().equals(userId)) {
                return entry;
            }
        }
        // If user has not taken any quiz yet
        LeaderboardEntryDto unranked = new LeaderboardEntryDto();
        unranked.setRank(-1);
        unranked.setUserId(userId);
        Optional<User> uOpt = userRepository.findById(userId);
        unranked.setName(uOpt.map(User::getName).orElse("Student"));
        unranked.setQuizzesTaken(0);
        unranked.setTotalScore(0);
        unranked.setTotalQuestions(0);
        unranked.setAveragePercentage(0.0);
        unranked.setStreakDays(userStreakRepository.findByUserId(userId).map(UserStreak::getCurrentStreak).orElse(0));
        return unranked;
    }

    private List<LeaderboardEntryDto> computeRankedLeaderboard() {
        List<QuizAttempt> attempts = quizAttemptRepository.findAll();
        if (attempts.isEmpty()) {
            return Collections.emptyList();
        }

        // Group by userId
        Map<Integer, List<QuizAttempt>> attemptsByUser = attempts.stream()
                .filter(a -> a.getUserId() != null)
                .collect(Collectors.groupingBy(QuizAttempt::getUserId));

        // Load users to filter out admins and get names
        Map<Integer, User> userMap = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u, (u1, u2) -> u1));

        // Load streaks
        Map<Integer, Integer> streakMap = userStreakRepository.findAll().stream()
                .collect(Collectors.toMap(UserStreak::getUserId, UserStreak::getCurrentStreak, (s1, s2) -> s1));

        List<LeaderboardEntryDto> entries = new ArrayList<>();

        for (Map.Entry<Integer, List<QuizAttempt>> entry : attemptsByUser.entrySet()) {
            Integer userId = entry.getKey();
            User user = userMap.get(userId);
            if (user == null) {
                continue;
            }
            // Skip admins from student leaderboard
            String role = user.getRole();
            if (role != null && (role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("ROLE_ADMIN"))) {
                continue;
            }

            List<QuizAttempt> userAttempts = entry.getValue();
            long totalScore = userAttempts.stream().mapToLong(a -> a.getScore() != null ? a.getScore() : 0).sum();
            long totalQuestions = userAttempts.stream().mapToLong(a -> a.getTotalQuestions() != null ? a.getTotalQuestions() : 0).sum();
            long count = userAttempts.size();

            double avgPct = totalQuestions > 0 ? ((double) totalScore * 100.0) / totalQuestions : 0.0;
            // Round to 1 decimal place
            avgPct = Math.round(avgPct * 10.0) / 10.0;

            int streak = streakMap.getOrDefault(userId, 0);

            LeaderboardEntryDto dto = new LeaderboardEntryDto();
            dto.setUserId(userId);
            dto.setName(formatDisplayName(user.getName()));
            dto.setQuizzesTaken(count);
            dto.setTotalScore(totalScore);
            dto.setTotalQuestions(totalQuestions);
            dto.setAveragePercentage(avgPct);
            dto.setStreakDays(streak);

            entries.add(dto);
        }

        // Sort by totalScore DESC, then averagePercentage DESC, then quizzesTaken DESC
        entries.sort(Comparator
                .comparingLong(LeaderboardEntryDto::getTotalScore).reversed()
                .thenComparing(Comparator.comparingDouble(LeaderboardEntryDto::getAveragePercentage).reversed())
                .thenComparing(Comparator.comparingLong(LeaderboardEntryDto::getQuizzesTaken).reversed())
        );

        // Assign ranks (1-based)
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }

        return entries;
    }

    private String formatDisplayName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Student";
        }
        String trimmed = fullName.trim();
        String[] parts = trimmed.split("\\s+");
        if (parts.length == 1) {
            return parts[0];
        }
        // Format as "FirstName L."
        return parts[0] + " " + parts[parts.length - 1].substring(0, 1).toUpperCase() + ".";
    }
}
