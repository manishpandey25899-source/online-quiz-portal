-- ============================================================================
-- Rollback Migration: V2_rollback__drop_phase2.sql
-- ============================================================================

DROP TABLE IF EXISTS daily_challenge_attempts;
DROP TABLE IF EXISTS user_streaks;
DROP TABLE IF EXISTS daily_challenges;
DROP TABLE IF EXISTS bookmarked_questions;
-- ALTER TABLE questions DROP COLUMN explanation;
