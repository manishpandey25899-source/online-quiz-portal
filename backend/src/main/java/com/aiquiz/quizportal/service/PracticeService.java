package com.aiquiz.quizportal.service;

import com.aiquiz.quizportal.entity.Question;
import com.aiquiz.quizportal.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PracticeService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private BookmarkService bookmarkService;

    public List<Question> getQuestionsByIds(List<Integer> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyList();
        }
        return questionRepository.findAllById(questionIds);
    }

    public List<Question> getBookmarkedQuestionsForPractice(Integer userId) {
        return bookmarkService.getBookmarkedQuestions(userId);
    }

    public List<Question> getAllQuestionsByCategory(Integer categoryId) {
        return questionRepository.findRandomQuestionsByCategory(categoryId, 30);
    }
}
