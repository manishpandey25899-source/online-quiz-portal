package com.aiquiz.quizportal.service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.host:${MAIL_HOST:smtp-relay.brevo.com}}")
    private String mailHost;

    @Value("${spring.mail.port:${MAIL_PORT:587}}")
    private int mailPort;

    @Value("${spring.mail.username:${MAIL_USERNAME:}}")
    private String mailUsername;

    @Value("${spring.mail.password:${MAIL_PASSWORD:}}")
    private String mailPassword;

    @Value("${app.mail.from:${MAIL_FROM:}}")
    private String mailFrom;

    @Value("${app.mail.from-name:${MAIL_FROM_NAME:AI Quiz Portal}}")
    private String mailFromName;

    @PostConstruct
    public void logEmailConfigStatus() {
        if (isSmtpConfigured()) {
            logger.info("EmailService initialized in Brevo SMTP mode: host={}, port={}, username={}, from={}, fromName={}",
                    getEffectiveHost(),
                    getEffectivePort(),
                    maskIdentifier(getEffectiveUsername()),
                    getEffectiveFrom(),
                    getEffectiveFromName());
        } else {
            logger.info("EmailService initialized in DEV MODE (SMTP credentials not configured; OTP will be printed to console)");
        }
    }

    public void sendOtpEmail(String toEmail, String otp) {
        if (!isSmtpConfigured()) {
            // Strictly only in local development / unconfigured credentials mode
            logger.info("[DEV MODE ONLY] Password Reset OTP for {}: {}", toEmail, otp);
            System.out.println("==================================================");
            System.out.println("[DEV MODE ONLY] Password Reset OTP for " + toEmail + " is: " + otp);
            System.out.println("==================================================");
            return;
        }

        // Brevo SMTP Mode: Send email via SMTP without exposing OTP or credentials in console or logs
        JavaMailSenderImpl sender = configureMailSender();

        try {
            MimeMessage mimeMessage = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String fromEmail = getEffectiveFrom();
            String fromName = getEffectiveFromName();

            if (fromEmail == null || fromEmail.isBlank()) {
                throw new IllegalStateException("MAIL_FROM (verified sender email in Brevo) must be configured.");
            }

            if (fromName != null && !fromName.isBlank()) {
                helper.setFrom(new InternetAddress(fromEmail, fromName, "UTF-8"));
            } else {
                helper.setFrom(fromEmail);
            }

            helper.setTo(toEmail);
            helper.setSubject("AI Quiz Portal - Password Reset OTP");

            String textBody = "Hello,\n\n"
                    + "Your One-Time Password (OTP) for resetting your AI Quiz Portal password is:\n\n"
                    + otp + "\n\n"
                    + "This OTP is valid for 15 minutes. Please do not share this code with anyone.\n\n"
                    + "If you did not request a password reset, please ignore this email.\n\n"
                    + "Best regards,\nAI Quiz Portal Team";

            String htmlBody = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head><meta charset=\"UTF-8\"></head>"
                    + "<body style=\"font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 24px;\">"
                    + "<div style=\"max-width: 520px; margin: 0 auto; background: #ffffff; border-radius: 12px; padding: 32px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); border: 1px solid #e5e7eb;\">"
                    + "<div style=\"text-align: center; margin-bottom: 24px;\">"
                    + "<h1 style=\"color: #4f46e5; margin: 0; font-size: 24px; font-weight: 700;\">AI Quiz Portal</h1>"
                    + "<p style=\"color: #6b7280; font-size: 14px; margin: 4px 0 0 0;\">Password Reset Verification</p>"
                    + "</div>"
                    + "<p style=\"color: #374151; font-size: 15px; line-height: 1.5;\">Hello,</p>"
                    + "<p style=\"color: #374151; font-size: 15px; line-height: 1.5;\">You recently requested to reset your password. Use the verification code below to complete the process:</p>"
                    + "<div style=\"background: #f3f4f6; border-radius: 8px; padding: 18px; text-align: center; margin: 24px 0; border: 1px dashed #d1d5db;\">"
                    + "<span style=\"font-size: 32px; font-weight: 700; letter-spacing: 8px; color: #1f2937; font-family: monospace;\">" + otp + "</span>"
                    + "</div>"
                    + "<p style=\"color: #6b7280; font-size: 13px; line-height: 1.5;\">&#9203; This OTP is valid for <strong>15 minutes</strong>. Do not share this OTP with anyone.</p>"
                    + "<p style=\"color: #6b7280; font-size: 13px; line-height: 1.5;\">If you did not make this request, you can safely ignore this email; your account remains secure.</p>"
                    + "<hr style=\"border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;\">"
                    + "<p style=\"color: #9ca3af; font-size: 12px; text-align: center; margin: 0;\">AI Quiz Portal Team &bull; Automated Security Notification</p>"
                    + "</div>"
                    + "</body>"
                    + "</html>";

            helper.setText(textBody, htmlBody);
            sender.send(mimeMessage);
            logger.info("Password reset OTP email sent successfully to {}", toEmail);
        } catch (Exception ex) {
            String safeError = sanitizeError(ex);
            logger.error("Failed to send password reset email via Brevo SMTP to {}. Error: {}", toEmail, safeError);
            throw new RuntimeException("Failed to send password reset email via Brevo SMTP: " + safeError, ex);
        }
    }

    public boolean isSmtpConfigured() {
        String username = getEffectiveUsername();
        String password = getEffectivePassword();
        return username != null && !username.isBlank()
                && password != null && !password.isBlank();
    }

    private JavaMailSenderImpl configureMailSender() {
        JavaMailSenderImpl sender;
        if (this.mailSender instanceof JavaMailSenderImpl impl) {
            sender = impl;
        } else {
            sender = new JavaMailSenderImpl();
            this.mailSender = sender;
        }

        sender.setHost(getEffectiveHost());
        sender.setPort(getEffectivePort());
        sender.setUsername(getEffectiveUsername());
        sender.setPassword(getEffectivePassword());

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        return sender;
    }

    public String getEffectiveHost() {
        if (mailHost != null && !mailHost.isBlank()) {
            return mailHost.trim();
        }
        String env = System.getenv("MAIL_HOST");
        return (env != null && !env.isBlank()) ? env.trim() : "smtp-relay.brevo.com";
    }

    public int getEffectivePort() {
        String env = System.getenv("MAIL_PORT");
        if (env != null && !env.isBlank()) {
            try {
                return Integer.parseInt(env.trim());
            } catch (NumberFormatException ignored) {}
        }
        return mailPort > 0 ? mailPort : 587;
    }

    public String getEffectiveUsername() {
        if (mailUsername != null && !mailUsername.isBlank()) {
            return mailUsername.trim();
        }
        String envUser = System.getenv("MAIL_USERNAME");
        return (envUser != null && !envUser.isBlank()) ? envUser.trim() : null;
    }

    public String getEffectivePassword() {
        String pass = null;
        if (mailPassword != null && !mailPassword.isBlank()) {
            pass = mailPassword.trim();
        } else {
            String envPass = System.getenv("MAIL_PASSWORD");
            if (envPass != null && !envPass.isBlank()) {
                pass = envPass.trim();
            }
        }
        return (pass != null && !pass.isBlank()) ? pass.trim() : null;
    }

    public String getEffectiveFrom() {
        if (mailFrom != null && !mailFrom.isBlank()) {
            return mailFrom.trim();
        }
        String env = System.getenv("MAIL_FROM");
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        String user = getEffectiveUsername();
        return (user != null && user.contains("@")) ? user : null;
    }

    public String getEffectiveFromName() {
        if (mailFromName != null && !mailFromName.isBlank()) {
            return mailFromName.trim();
        }
        String env = System.getenv("MAIL_FROM_NAME");
        return (env != null && !env.isBlank()) ? env.trim() : "AI Quiz Portal";
    }

    private String sanitizeError(Throwable ex) {
        if (ex == null) {
            return "Unknown SMTP error";
        }
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = ex.getClass().getSimpleName();
        }
        String pass = getEffectivePassword();
        if (pass != null && !pass.isBlank() && msg.contains(pass)) {
            msg = msg.replace(pass, "******");
        }
        return msg;
    }

    private String maskIdentifier(String id) {
        if (id == null || id.isBlank()) return "(none)";
        if (id.length() <= 4) return "****";
        return id.substring(0, 2) + "****" + id.substring(id.length() - 2);
    }
}
