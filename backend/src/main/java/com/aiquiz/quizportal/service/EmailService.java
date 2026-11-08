package com.aiquiz.quizportal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.dev.mode:true}")
    private boolean devMode;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        if (devMode || fromEmail == null || fromEmail.isBlank()) {
            // Strictly only in local development / unconfigured credentials mode
            logger.info("[DEV MODE ONLY] Password Reset OTP for {}: {}", toEmail, otp);
            System.out.println("==================================================");
            System.out.println("[DEV MODE ONLY] Password Reset OTP for " + toEmail + " is: " + otp);
            System.out.println("==================================================");
            return;
        }

        // Production Mode: Send email via SMTP without exposing OTP in console or logs
        if (mailSender == null) {
            logger.error("JavaMailSender is not configured. Cannot send password reset email to {}", toEmail);
            throw new IllegalStateException("Email service is currently unavailable");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromEmail != null && !fromEmail.isBlank()) {
                message.setFrom(fromEmail);
            }
            message.setTo(toEmail);
            message.setSubject("AI Quiz Portal - Password Reset OTP");
            message.setText("Hello,\n\n"
                    + "Your One-Time Password (OTP) for resetting your AI Quiz Portal password is:\n\n"
                    + otp + "\n\n"
                    + "This OTP is valid for 10 minutes. Please do not share this code with anyone.\n\n"
                    + "If you did not request a password reset, please ignore this email.\n\n"
                    + "Best regards,\nAI Quiz Portal Team");

            mailSender.send(message);
            logger.info("Password reset OTP email sent successfully to {}", toEmail);
        } catch (Exception ex) {
            logger.error("Failed to send password reset email to {}: {}", toEmail, ex.getMessage());
            throw new RuntimeException("Failed to send password reset email. Please try again later.");
        }
    }
}
