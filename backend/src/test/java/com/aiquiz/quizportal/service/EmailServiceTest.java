package com.aiquiz.quizportal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class EmailServiceTest {

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService();
    }

    @Test
    @DisplayName("Should detect unconfigured SMTP credentials and use local dev mode")
    void testUnconfiguredSmtpDevMode() {
        // No credentials configured
        ReflectionTestUtils.setField(emailService, "mailUsername", "");
        ReflectionTestUtils.setField(emailService, "mailPassword", "");

        assertFalse(emailService.isSmtpConfigured(), "SMTP should not be configured when credentials are empty");

        // Sending OTP in dev mode should output to console without throwing
        assertDoesNotThrow(() -> {
            emailService.sendOtpEmail("student@example.com", "123456");
        });
    }

    @Test
    @DisplayName("Should detect configured SMTP credentials")
    void testConfiguredSmtpDetection() {
        ReflectionTestUtils.setField(emailService, "mailUsername", "789abcdef@smtp-brevo.com");
        ReflectionTestUtils.setField(emailService, "mailPassword", "xsmtpsib-secret-key-12345");

        assertTrue(emailService.isSmtpConfigured(), "SMTP should be configured when username and password are provided");
    }

    @Test
    @DisplayName("Should use default Brevo SMTP host and port when not overridden")
    void testDefaultBrevoHostAndPort() {
        ReflectionTestUtils.setField(emailService, "mailHost", "smtp-relay.brevo.com");
        ReflectionTestUtils.setField(emailService, "mailPort", 587);

        assertEquals("smtp-relay.brevo.com", emailService.getEffectiveHost());
        assertEquals(587, emailService.getEffectivePort());
    }

    @Test
    @DisplayName("Should use default sender name AI Quiz Portal")
    void testDefaultSenderName() {
        ReflectionTestUtils.setField(emailService, "mailFromName", "AI Quiz Portal");
        assertEquals("AI Quiz Portal", emailService.getEffectiveFromName());
    }

    @Test
    @DisplayName("Should prefer explicit MAIL_FROM over username")
    void testMailFromResolution() {
        ReflectionTestUtils.setField(emailService, "mailUsername", "789login@smtp-brevo.com");
        ReflectionTestUtils.setField(emailService, "mailFrom", "verified-sender@college.edu");

        assertEquals("verified-sender@college.edu", emailService.getEffectiveFrom());
    }

    @Test
    @DisplayName("Should fall back to username if MAIL_FROM is empty and username is an email")
    void testMailFromFallbackToUsernameEmail() {
        ReflectionTestUtils.setField(emailService, "mailUsername", "myaccount@gmail.com");
        ReflectionTestUtils.setField(emailService, "mailFrom", "");

        assertEquals("myaccount@gmail.com", emailService.getEffectiveFrom());
    }

    @Test
    @DisplayName("Should throw safe exception when sending fails with configured SMTP and NOT silently print OTP")
    void testSmtpFailureDoesNotSilentlyFallback() {
        ReflectionTestUtils.setField(emailService, "mailHost", "invalid-smtp-host.example.com");
        ReflectionTestUtils.setField(emailService, "mailPort", 587);
        ReflectionTestUtils.setField(emailService, "mailUsername", "testuser@smtp-brevo.com");
        ReflectionTestUtils.setField(emailService, "mailPassword", "xsmtpsib-super-secret-key");
        ReflectionTestUtils.setField(emailService, "mailFrom", "sender@example.com");

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            emailService.sendOtpEmail("recipient@example.com", "654321");
        });

        // Ensure error message does not expose password
        assertFalse(thrown.getMessage().contains("xsmtpsib-super-secret-key"), "Error must not contain password");
        assertTrue(thrown.getMessage().startsWith("Failed to send password reset email via Brevo SMTP:"));
    }
}
