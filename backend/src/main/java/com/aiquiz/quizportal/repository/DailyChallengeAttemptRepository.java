package com.aiquiz.quizportal.repository;

import com.aiquiz.quizportal.entity.DailyChallengeAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyChallengeAttemptRepository extends JpaRepository<DailyChallengeAttempt, Integer> {

    Optional<DailyChallengeAttempt> findByUserIdAndChallengeDate(Integer userId, LocalDate challengeDate);

    List<DailyChallengeAttempt> findByUserIdOrderByChallengeDateDesc(Integer userId);

    boolean existsByUserIdAndChallengeDate(Integer userId, LocalDate challengeDate);
}
