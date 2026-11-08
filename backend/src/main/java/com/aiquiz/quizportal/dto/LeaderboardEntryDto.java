package com.aiquiz.quizportal.dto;

public class LeaderboardEntryDto {

    private int rank;
    private Integer userId;
    private String name;
    private long quizzesTaken;
    private long totalScore;
    private long totalQuestions;
    private double averagePercentage;
    private int streakDays;

    public LeaderboardEntryDto() {
    }

    public LeaderboardEntryDto(int rank, Integer userId, String name, long quizzesTaken,
                               long totalScore, long totalQuestions, double averagePercentage, int streakDays) {
        this.rank = rank;
        this.userId = userId;
        this.name = name;
        this.quizzesTaken = quizzesTaken;
        this.totalScore = totalScore;
        this.totalQuestions = totalQuestions;
        this.averagePercentage = averagePercentage;
        this.streakDays = streakDays;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getQuizzesTaken() {
        return quizzesTaken;
    }

    public void setQuizzesTaken(long quizzesTaken) {
        this.quizzesTaken = quizzesTaken;
    }

    public long getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(long totalScore) {
        this.totalScore = totalScore;
    }

    public long getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(long totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public double getAveragePercentage() {
        return averagePercentage;
    }

    public void setAveragePercentage(double averagePercentage) {
        this.averagePercentage = averagePercentage;
    }

    public int getStreakDays() {
        return streakDays;
    }

    public void setStreakDays(int streakDays) {
        this.streakDays = streakDays;
    }
}
