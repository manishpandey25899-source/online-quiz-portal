package com.aiquiz.quizportal.repository;

import com.aiquiz.quizportal.entity.BookmarkedQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkedQuestionRepository extends JpaRepository<BookmarkedQuestion, Integer> {

    List<BookmarkedQuestion> findByUserIdOrderByCreatedAtDesc(Integer userId);

    Optional<BookmarkedQuestion> findByUserIdAndQuestionId(Integer userId, Integer questionId);

    boolean existsByUserIdAndQuestionId(Integer userId, Integer questionId);

    void deleteByUserIdAndQuestionId(Integer userId, Integer questionId);
}
