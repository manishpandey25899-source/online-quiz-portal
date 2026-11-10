package com.aiquiz.quizportal.service;

import com.aiquiz.quizportal.entity.PasswordReset;
import com.aiquiz.quizportal.entity.User;
import com.aiquiz.quizportal.repository.PasswordResetRepository;
import com.aiquiz.quizportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class PasswordResetService {

    private static final int MAX_EMAIL_REQUESTS_15M = 3;
    private static final int MAX_IP_REQUESTS_15M = 5;
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final int OTP_VALIDITY_MINUTES = 15;
    private static final int RESET_TOKEN_VALIDITY_MINUTES = 5;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,64}$");

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private PasswordResetRepository passwordResetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${jwt.secret:AIQuizPortalSuperSecretKey2026ForBcaCollegeProjectDevelopmentOnly1234567890}")
    private String hmacSecret;

    @Transactional
    public void initiateForgotPassword(String email, String ipAddress) {
        String cleanEmail = email.trim().toLowerCase();
        LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);

        // 1. IP Rate Limiting
        long ipCount = passwordResetRepository.countByIpAddressAndCreatedAtAfter(ipAddress, fifteenMinutesAgo);
        if (ipCount >= MAX_IP_REQUESTS_15M) {
            throw new IllegalArgumentException("Too many password reset requests from this IP address. Please wait 15 minutes.");
        }

        // 2. Email Rate Limiting
        long emailCount = passwordResetRepository.countByEmailAndCreatedAtAfter(cleanEmail, fifteenMinutesAgo);
        if (emailCount >= MAX_EMAIL_REQUESTS_15M) {
            throw new IllegalArgumentException("Too many password reset requests for this email. Please wait 15 minutes.");
        }

        // 3. Verify user exists in database. If user does not exist, return gracefully to prevent user enumeration
        User user = userRepository.findByEmail(cleanEmail);
        if (user == null) {
            return;
        }

        // 4. Invalidate prior pending OTPs for this email
        List<PasswordReset> pending = passwordResetRepository.findByEmailAndUsedFalse(cleanEmail);
        for (PasswordReset pr : pending) {
            pr.setUsed(true);
        }
        passwordResetRepository.saveAll(pending);

        // 5. Generate cryptographically strong 6-digit OTP
        int otpNumber = secureRandom.nextInt(900000) + 100000;
        String otp = String.valueOf(otpNumber);

        // 6. Compute HMAC-SHA256 hash of OTP
        String otpHash = computeHmac(otp);

        // 7. Save PasswordReset record
        PasswordReset resetRecord = new PasswordReset();
        resetRecord.setEmail(cleanEmail);
        resetRecord.setIpAddress(ipAddress);
        resetRecord.setOtpHash(otpHash);
        resetRecord.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES));
        resetRecord.setOtpVerified(false);
        resetRecord.setUsed(false);
        resetRecord.setAttemptCount(0);
        resetRecord.setCreatedAt(LocalDateTime.now());
        passwordResetRepository.save(resetRecord);

        // 8. Send OTP email
        emailService.sendOtpEmail(cleanEmail, otp);
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public String verifyOtp(String email, String rawOtp) {
        String cleanEmail = email.trim().toLowerCase();

        Optional<PasswordReset> optRecord = passwordResetRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(cleanEmail);

        if (optRecord.isEmpty()) {
            throw new IllegalArgumentException("No pending password reset request found for this email.");
        }

        PasswordReset record = optRecord.get();

        if (record.getAttemptCount() >= MAX_OTP_ATTEMPTS) {
            record.setUsed(true);
            passwordResetRepository.save(record);
            throw new IllegalArgumentException("Maximum verification attempts exceeded. Please request a new OTP.");
        }

        if (LocalDateTime.now().isAfter(record.getOtpExpiry())) {
            record.setUsed(true);
            passwordResetRepository.save(record);
            throw new IllegalArgumentException("The OTP has expired. Please request a new one.");
        }

        String computedHash = computeHmac(rawOtp.trim());
        if (!computedHash.equals(record.getOtpHash())) {
            record.setAttemptCount(record.getAttemptCount() + 1);
            passwordResetRepository.save(record);
            int remaining = MAX_OTP_ATTEMPTS - record.getAttemptCount();
            throw new IllegalArgumentException("Invalid OTP. Remaining attempts: " + remaining);
        }

        // OTP verified successfully!
        // Issue a single-use cryptographically random reset token valid for 5 minutes
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String resetToken = HexFormat.of().formatHex(tokenBytes);

        record.setOtpVerified(true);
        record.setResetToken(resetToken);
        record.setResetTokenExpiry(LocalDateTime.now().plusMinutes(RESET_TOKEN_VALIDITY_MINUTES));
        passwordResetRepository.save(record);

        return resetToken;
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void resetPassword(String email, String resetToken, String newPassword) {
        String cleanEmail = email.trim().toLowerCase();

        if (newPassword == null || !PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain at least one letter and one number.");
        }

        Optional<PasswordReset> optRecord = passwordResetRepository.findByResetTokenAndUsedFalse(resetToken.trim());
        if (optRecord.isEmpty()) {
            throw new IllegalArgumentException("Invalid or expired reset token.");
        }

        PasswordReset record = optRecord.get();

        if (!record.getEmail().equalsIgnoreCase(cleanEmail)) {
            throw new IllegalArgumentException("Reset token does not match the provided email address.");
        }

        if (!Boolean.TRUE.equals(record.getOtpVerified())) {
            throw new IllegalArgumentException("OTP has not been verified.");
        }

        if (record.getResetTokenExpiry() == null || LocalDateTime.now().isAfter(record.getResetTokenExpiry())) {
            record.setUsed(true);
            passwordResetRepository.save(record);
            throw new IllegalArgumentException("Reset token has expired. Please request a new OTP.");
        }

        User user = userRepository.findByEmail(cleanEmail);
        if (user == null) {
            throw new IllegalArgumentException("User account not found.");
        }

        // Update password with BCrypt
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Invalidate reset token completely
        record.setUsed(true);
        record.setResetToken(null);
        passwordResetRepository.save(record);
    }

    private String computeHmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to compute HMAC hash", ex);
        }
    }
}
