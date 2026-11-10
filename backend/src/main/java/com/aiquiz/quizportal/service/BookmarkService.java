package com.aiquiz.quizportal.service;

import com.aiquiz.quizportal.entity.BookmarkedQuestion;
import com.aiquiz.quizportal.entity.Question;
import com.aiquiz.quizportal.repository.BookmarkedQuestionRepository;
import com.aiquiz.quizportal.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BookmarkService {

    @Autowired
    private BookmarkedQuestionRepository bookmarkRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Transactional
    public boolean addBookmark(Integer userId, Integer questionId) {
        if (bookmarkRepository.existsByUserIdAndQuestionId(userId, questionId)) {
            return false; // Already bookmarked
        }

        Optional<Question> questionOpt = questionRepository.findById(questionId);
        if (questionOpt.isEmpty()) {
            throw new IllegalArgumentException("Question not found with ID: " + questionId);
        }

        BookmarkedQuestion bookmark = new BookmarkedQuestion(userId, questionOpt.get());
        bookmarkRepository.save(bookmark);
        return true;
    }

    @Transactional
    public boolean removeBookmark(Integer userId, Integer questionId) {
        Optional<BookmarkedQuestion> bookmarkOpt = bookmarkRepository.findByUserIdAndQuestionId(userId, questionId);
        if (bookmarkOpt.isPresent()) {
            bookmarkRepository.delete(bookmarkOpt.get());
            return true;
        }
        return false;
    }

    public List<Question> getBookmarkedQuestions(Integer userId) {
        List<BookmarkedQuestion> bookmarks = bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return bookmarks.stream().map(BookmarkedQuestion::getQuestion).toList();
    }

    public List<Integer> getBookmarkedQuestionIds(Integer userId) {
        List<BookmarkedQuestion> bookmarks = bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return bookmarks.stream().map(b -> b.getQuestion().getId()).toList();
    }
}
