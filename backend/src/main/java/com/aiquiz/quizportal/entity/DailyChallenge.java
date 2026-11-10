package com.aiquiz.quizportal.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_challenges")
public class DailyChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "challenge_date", nullable = false, unique = true)
    private LocalDate challengeDate;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "question_ids", nullable = false)
    private String questionIds;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public DailyChallenge() {
    }

    public DailyChallenge(LocalDate challengeDate, Integer categoryId, String questionIds) {
        this.challengeDate = challengeDate;
        this.categoryId = categoryId;
        this.questionIds = questionIds;
        this.createdAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getChallengeDate() {
        return challengeDate;
    }

    public void setChallengeDate(LocalDate challengeDate) {
        this.challengeDate = challengeDate;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getQuestionIds() {
        return questionIds;
    }

    public void setQuestionIds(String questionIds) {
        this.questionIds = questionIds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
