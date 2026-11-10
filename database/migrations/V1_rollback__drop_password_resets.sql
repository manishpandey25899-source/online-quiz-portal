-- ============================================================================
-- Rollback Migration: V1_rollback__drop_password_resets.sql
-- Description: Drops password_resets table safely
-- ============================================================================

DROP TABLE IF EXISTS password_resets;
