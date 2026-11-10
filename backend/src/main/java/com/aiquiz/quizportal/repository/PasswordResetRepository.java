package com.aiquiz.quizportal.repository;

import com.aiquiz.quizportal.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Integer> {

    long countByEmailAndCreatedAtAfter(String email, LocalDateTime since);

    long countByIpAddressAndCreatedAtAfter(String ipAddress, LocalDateTime since);

    List<PasswordReset> findByEmailAndUsedFalse(String email);

    Optional<PasswordReset> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    Optional<PasswordReset> findByResetTokenAndUsedFalse(String resetToken);
}
