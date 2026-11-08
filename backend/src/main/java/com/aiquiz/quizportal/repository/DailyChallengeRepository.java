package com.aiquiz.quizportal.repository;

import com.aiquiz.quizportal.entity.DailyChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyChallengeRepository extends JpaRepository<DailyChallenge, Integer> {

    Optional<DailyChallenge> findByChallengeDate(LocalDate challengeDate);
}
