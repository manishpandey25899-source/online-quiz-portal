package com.aiquiz.quizportal.repository;

import com.aiquiz.quizportal.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Integer> {

    List<UserAchievement> findByUserIdOrderByUnlockedAtDesc(Integer userId);

    Optional<UserAchievement> findByUserIdAndBadgeCode(Integer userId, String badgeCode);

    boolean existsByUserIdAndBadgeCode(Integer userId, String badgeCode);
}
