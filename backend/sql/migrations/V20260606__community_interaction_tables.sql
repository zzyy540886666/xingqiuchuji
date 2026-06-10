-- ============================================================
-- V20260606__community_interaction_tables.sql
-- Description: Add collect_count to posts, create post_likes /
--   post_collects / user_follows tables, fix comments.status default.
-- Gap:     C01 community interaction backend (2026-06-06)
-- Related: Post.java collectCount field, PostLike, PostCollect,
--   UserFollow entities, Comment.status='ACTIVE' query filter
-- ============================================================

-- 1. Add collect_count column to existing posts table
ALTER TABLE posts
    ADD COLUMN IF NOT EXISTS collect_count INT DEFAULT 0 AFTER like_count;

-- 2. Fix comments.status default value (code queries WHERE status='ACTIVE')
ALTER TABLE comments
    ALTER COLUMN status SET DEFAULT 'ACTIVE';

-- 3. Create post_likes table
CREATE TABLE IF NOT EXISTS post_likes (
    id BIGINT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_user (post_id, user_id),
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Create post_collects table
CREATE TABLE IF NOT EXISTS post_collects (
    id BIGINT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_user (post_id, user_id),
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Create user_follows table
CREATE TABLE IF NOT EXISTS user_follows (
    id BIGINT PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    followee_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_follower_followee (follower_id, followee_id),
    INDEX idx_follower_id (follower_id),
    INDEX idx_followee_id (followee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
