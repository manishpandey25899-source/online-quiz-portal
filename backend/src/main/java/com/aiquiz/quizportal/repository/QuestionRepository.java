package com.aiquiz.quizportal.repository;

import com.aiquiz.quizportal.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findByCategoryId(Integer categoryId);

    @Query(
            value = "SELECT * FROM questions WHERE category_id = ?1 ORDER BY RAND() LIMIT ?2",
            nativeQuery = true
    )
    List<Question> findRandomQuestionsByCategory(
            Integer categoryId,
            Integer limit
    );
}