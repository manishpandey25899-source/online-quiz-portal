-- ============================================================================
-- Migration: V1__create_password_resets.sql
-- Description: Creates password_resets table for secure OTP-based password recovery
-- Supports: Email + IP rate limiting, HMAC OTP hashing, single-use reset tokens
-- ============================================================================

CREATE TABLE IF NOT EXISTS password_resets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    reset_token VARCHAR(255) DEFAULT NULL,
    otp_expiry DATETIME NOT NULL,
    reset_token_expiry DATETIME DEFAULT NULL,
    otp_verified BOOLEAN DEFAULT FALSE,
    used BOOLEAN DEFAULT FALSE,
    attempt_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_ip_address (ip_address),
    INDEX idx_reset_token (reset_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
