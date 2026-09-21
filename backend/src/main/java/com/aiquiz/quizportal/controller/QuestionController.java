package com.aiquiz.quizportal.controller;

import com.aiquiz.quizportal.entity.Category;
import com.aiquiz.quizportal.entity.Question;
import com.aiquiz.quizportal.entity.User;
import com.aiquiz.quizportal.repository.CategoryRepository;
import com.aiquiz.quizportal.repository.QuestionRepository;
import com.aiquiz.quizportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class QuestionController {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;


    // ==========================================
    // GET ALL QUESTIONS
    // ==========================================

    @GetMapping
    public List<Question> getAllQuestions() {

        return questionRepository.findAll();
    }


    // ==========================================
    // GET RANDOM QUESTIONS BY CATEGORY
    // ==========================================

    @GetMapping("/category/{id}")
    public List<Question> getQuestionsByCategory(
            @PathVariable Integer id
    ) {

        return questionRepository
                .findRandomQuestionsByCategory(id, 15);
    }


    // ==========================================
    // ADD QUESTION - ADMIN ONLY
    // ==========================================

    @PostMapping
    public ResponseEntity<?> addQuestion(
            @RequestParam Integer userId,
            @RequestBody Question question
    ) {

        // Find user
        Optional<User> userOptional =
                userRepository.findById(userId);


        // User not found
        if (userOptional.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Access Denied");
        }


        User user =
                userOptional.get();


        // Check admin role
        if (
                user.getRole() == null ||
                        !user.getRole().equalsIgnoreCase("admin")
        ) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Access Denied. Admin only.");
        }


        // Category validation
        if (
                question.getCategory() == null ||
                        question.getCategory().getId() == null
        ) {

            return ResponseEntity
                    .badRequest()
                    .body("Category is required.");
        }


        Optional<Category> categoryOptional =
                categoryRepository.findById(
                        question.getCategory().getId()
                );


        if (categoryOptional.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Category Not Found.");
        }


        // Use existing category
        question.setCategory(
                categoryOptional.get()
        );


        // Save question
        Question savedQuestion =
                questionRepository.save(question);


        return ResponseEntity.ok(
                savedQuestion
        );
    }


    // ==========================================
    // UPDATE QUESTION - ADMIN ONLY
    // ==========================================

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuestion(
            @PathVariable Integer id,
            @RequestParam Integer userId,
            @RequestBody Question question
    ) {

        // Find user
        Optional<User> userOptional =
                userRepository.findById(userId);


        // User not found
        if (userOptional.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Access Denied");
        }


        User user =
                userOptional.get();


        // Check admin role
        if (
                user.getRole() == null ||
                        !user.getRole().equalsIgnoreCase("admin")
        ) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Access Denied. Admin only.");
        }


        // Find existing question
        Optional<Question> questionOptional =
                questionRepository.findById(id);


        if (questionOptional.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Question Not Found");
        }


        // Validate category
        if (
                question.getCategory() == null ||
                        question.getCategory().getId() == null
        ) {

            return ResponseEntity
                    .badRequest()
                    .body("Category is required.");
        }


        Optional<Category> categoryOptional =
                categoryRepository.findById(
                        question.getCategory().getId()
                );


        if (categoryOptional.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Category Not Found.");
        }


        // Existing question
        Question existingQuestion =
                questionOptional.get();


        // Update question fields
        existingQuestion.setQuestionText(
                question.getQuestionText()
        );

        existingQuestion.setOptionA(
                question.getOptionA()
        );

        existingQuestion.setOptionB(
                question.getOptionB()
        );

        existingQuestion.setOptionC(
                question.getOptionC()
        );

        existingQuestion.setOptionD(
                question.getOptionD()
        );

        existingQuestion.setCorrectAnswer(
                question.getCorrectAnswer()
        );


        // Update category
        existingQuestion.setCategory(
                categoryOptional.get()
        );


        // Save updated question
        Question updatedQuestion =
                questionRepository.save(
                        existingQuestion
                );


        return ResponseEntity.ok(
                updatedQuestion
        );
    }


    // ==========================================
    // DELETE QUESTION - ADMIN ONLY
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuestion(
            @PathVariable Integer id,
            @RequestParam Integer userId
    ) {

        // Find user
        Optional<User> userOptional =
                userRepository.findById(userId);


        // User not found
        if (userOptional.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Access Denied");
        }


        User user =
                userOptional.get();


        // Check admin role
        if (
                user.getRole() == null ||
                        !user.getRole().equalsIgnoreCase("admin")
        ) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Access Denied. Admin only.");
        }


        // Check question exists
        if (
                !questionRepository.existsById(id)
        ) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Question Not Found");
        }


        // Delete0
        questionRepository.deleteById(id);


        return ResponseEntity.ok(
                "Question Deleted Successfully"
        );
    }
}