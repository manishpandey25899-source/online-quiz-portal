package com.aiquiz.quizportal.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_challenge_attempts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "challenge_date"})
})
public class DailyChallengeAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "challenge_date", nullable = false)
    private LocalDate challengeDate;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions = 5;

    @Column(name = "completed_at")
    private LocalDateTime completedAt = LocalDateTime.now();

    public DailyChallengeAttempt() {
    }

    public DailyChallengeAttempt(Integer userId, LocalDate challengeDate, Integer score, Integer totalQuestions) {
        this.userId = userId;
        this.challengeDate = challengeDate;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.completedAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public LocalDate getChallengeDate() {
        return challengeDate;
    }

    public void setChallengeDate(LocalDate challengeDate) {
        this.challengeDate = challengeDate;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
