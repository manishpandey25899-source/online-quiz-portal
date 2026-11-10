package com.aiquiz.quizportal.repository;

import com.aiquiz.quizportal.entity.UserStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStreakRepository extends JpaRepository<UserStreak, Integer> {

    Optional<UserStreak> findByUserId(Integer userId);
}
