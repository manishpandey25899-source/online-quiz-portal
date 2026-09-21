package com.aiquiz.quizportal.repository;

import com.aiquiz.quizportal.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Integer> {

    List<QuizAttempt> findByUserId(Integer userId);

}