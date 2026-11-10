package com.aiquiz.quizportal.service;

import com.aiquiz.quizportal.entity.DailyChallenge;
import com.aiquiz.quizportal.entity.DailyChallengeAttempt;
import com.aiquiz.quizportal.entity.Question;
import com.aiquiz.quizportal.entity.UserStreak;
import com.aiquiz.quizportal.repository.DailyChallengeAttemptRepository;
import com.aiquiz.quizportal.repository.DailyChallengeRepository;
import com.aiquiz.quizportal.repository.QuestionRepository;
import com.aiquiz.quizportal.repository.UserStreakRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DailyChallengeService {

    @Autowired
    private DailyChallengeRepository dailyChallengeRepository;

    @Autowired
    private DailyChallengeAttemptRepository dailyChallengeAttemptRepository;

    @Autowired
    private UserStreakRepository userStreakRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Transactional
    public Map<String, Object> getTodayChallenge(Integer userId) {
        LocalDate today = LocalDate.now();

        // 1. Get or create today's challenge
        DailyChallenge challenge = dailyChallengeRepository.findByChallengeDate(today)
                .orElseGet(() -> createDailyChallengeForDate(today));

        // 2. Fetch the 5 questions
        List<Integer> questionIds = parseQuestionIds(challenge.getQuestionIds());
        List<Question> questions = questionRepository.findAllById(questionIds);

        // 3. Check if user already completed
        boolean alreadyCompleted = dailyChallengeAttemptRepository.existsByUserIdAndChallengeDate(userId, today);
        Optional<DailyChallengeAttempt> attemptOpt = dailyChallengeAttemptRepository.findByUserIdAndChallengeDate(userId, today);

        // 4. Get user streak
        UserStreak streak = getUserStreakEntity(userId);
        int currentStreak = calculateEffectiveStreak(streak, today);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("challengeDate", today.toString());
        result.put("alreadyCompleted", alreadyCompleted);
        result.put("currentStreak", currentStreak);
        result.put("maxStreak", streak.getMaxStreak());
        result.put("totalQuestions", questions.size());

        if (alreadyCompleted && attemptOpt.isPresent()) {
            result.put("score", attemptOpt.get().getScore());
        }

        result.put("questions", questions);
        return result;
    }

    @Transactional
    public Map<String, Object> submitDailyChallenge(Integer userId, Integer score, Integer totalQuestions) {
        LocalDate today = LocalDate.now();

        if (dailyChallengeAttemptRepository.existsByUserIdAndChallengeDate(userId, today)) {
            throw new IllegalArgumentException("You have already completed today's Daily Challenge.");
        }

        int total = (totalQuestions != null && totalQuestions > 0) ? totalQuestions : 5;
        int validScore = Math.max(0, Math.min(score != null ? score : 0, total));

        // 1. Save Attempt
        DailyChallengeAttempt attempt = new DailyChallengeAttempt(userId, today, validScore, total);
        dailyChallengeAttemptRepository.save(attempt);

        // 2. Update Streak
        UserStreak streak = getUserStreakEntity(userId);
        LocalDate lastDate = streak.getLastCompletedDate();
        LocalDate yesterday = today.minusDays(1);

        int newCurrentStreak;
        if (yesterday.equals(lastDate)) {
            newCurrentStreak = streak.getCurrentStreak() + 1;
        } else if (today.equals(lastDate)) {
            newCurrentStreak = streak.getCurrentStreak();
        } else {
            newCurrentStreak = 1;
        }

        streak.setCurrentStreak(newCurrentStreak);
        streak.setMaxStreak(Math.max(streak.getMaxStreak(), newCurrentStreak));
        streak.setLastCompletedDate(today);
        userStreakRepository.save(streak);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("score", validScore);
        response.put("totalQuestions", total);
        response.put("currentStreak", streak.getCurrentStreak());
        response.put("maxStreak", streak.getMaxStreak());
        response.put("message", "Daily Challenge completed successfully!");

        return response;
    }

    public Map<String, Object> getStreakInfo(Integer userId) {
        LocalDate today = LocalDate.now();
        UserStreak streak = getUserStreakEntity(userId);
        int effectiveStreak = calculateEffectiveStreak(streak, today);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("currentStreak", effectiveStreak);
        response.put("maxStreak", streak.getMaxStreak());
        response.put("lastCompletedDate", streak.getLastCompletedDate());
        return response;
    }

    private UserStreak getUserStreakEntity(Integer userId) {
        return userStreakRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserStreak newStreak = new UserStreak(userId);
                    return userStreakRepository.save(newStreak);
                });
    }

    private int calculateEffectiveStreak(UserStreak streak, LocalDate today) {
        if (streak.getLastCompletedDate() == null) {
            return 0;
        }
        LocalDate yesterday = today.minusDays(1);
        if (today.equals(streak.getLastCompletedDate()) || yesterday.equals(streak.getLastCompletedDate())) {
            return streak.getCurrentStreak();
        }
        return 0; // Streak broken
    }

    private DailyChallenge createDailyChallengeForDate(LocalDate date) {
        List<Question> allQuestions = questionRepository.findAll();
        Collections.shuffle(allQuestions);
        List<Question> selected = allQuestions.stream().limit(5).toList();

        String questionIds = selected.stream()
                .map(q -> String.valueOf(q.getId()))
                .collect(Collectors.joining(","));

        DailyChallenge challenge = new DailyChallenge(date, null, questionIds);
        return dailyChallengeRepository.save(challenge);
    }

    private List<Integer> parseQuestionIds(String idsString) {
        if (idsString == null || idsString.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(idsString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}
