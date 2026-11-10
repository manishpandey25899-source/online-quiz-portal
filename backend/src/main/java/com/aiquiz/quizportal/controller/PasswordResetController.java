package com.aiquiz.quizportal.controller;

import com.aiquiz.quizportal.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500",
        "https://comforting-gumdrop-a9db8a.netlify.app"
})
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        String email = requestBody.get("email");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Email address is required."));
        }

        String ipAddress = extractClientIp(request);

        try {
            passwordResetService.initiateForgotPassword(email, ipAddress);
            return ResponseEntity.ok(Map.of(
                    "message", "If an account with this email exists, a 6-digit OTP has been sent to your email."
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", (ex.getMessage() != null && !ex.getMessage().isBlank())
                            ? ex.getMessage()
                            : "An error occurred while processing your request. Please try again."));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String otp = requestBody.get("otp");

        if (email == null || email.trim().isEmpty() || otp == null || otp.trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Email and 6-digit OTP are required."));
        }

        try {
            String resetToken = passwordResetService.verifyOtp(email, otp);
            return ResponseEntity.ok(Map.of(
                    "resetToken", resetToken,
                    "message", "OTP verified successfully. You may now reset your password."
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Verification failed. Please try again."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String resetToken = requestBody.get("resetToken");
        String newPassword = requestBody.get("newPassword");

        if (email == null || email.trim().isEmpty()
                || resetToken == null || resetToken.trim().isEmpty()
                || newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Email, reset token, and new password are required."));
        }

        try {
            passwordResetService.resetPassword(email, resetToken, newPassword);
            return ResponseEntity.ok(Map.of(
                    "message", "Password reset successfully. You can now login with your new password."
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Password reset failed. Please try again."));
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isBlank()) {
            // First IP in the X-Forwarded-For chain is the client IP
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }
}
