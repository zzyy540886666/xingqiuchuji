-- ============================================================
-- 星球出机 — 数据库初始化脚本 (43 张表)
-- MySQL 8.0, InnoDB, utf8mb4
-- ============================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS xingqiu_dev DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xingqiu_dev;

-- ============================================================
-- 1. users (认证与用户)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY,
    openid VARCHAR(128) NOT NULL,
    unionid VARCHAR(128),
    nickname VARCHAR(64),
    avatar_url VARCHAR(512),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_openid (openid),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 2. admin_users (后台管理员)
-- ============================================================
CREATE TABLE IF NOT EXISTS admin_users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(256) NOT NULL,
    display_name VARCHAR(64),
    role VARCHAR(32) DEFAULT 'OPERATOR',
    enabled TINYINT(1) DEFAULT 1,
    last_login_at DATETIME,
    created_at DATETIME,
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 3. orders (订单)
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    order_type VARCHAR(20) NOT NULL,
    status VARCHAR(32) NOT NULL,
    amount_minor BIGINT NOT NULL DEFAULT 0,
    deposit_minor BIGINT NOT NULL DEFAULT 0,
    shipping_minor BIGINT NOT NULL DEFAULT 0,
    discount_minor BIGINT NOT NULL DEFAULT 0,
    payable_minor BIGINT NOT NULL DEFAULT 0,
    rent_start_date DATE,
    rent_end_date DATE,
    address_json TEXT,
    idempotency_key VARCHAR(128),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    UNIQUE KEY uk_user_idempotency (user_id, idempotency_key),
    INDEX idx_idempotency (idempotency_key),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_order_amounts CHECK (amount_minor >= 0 AND deposit_minor >= 0 AND shipping_minor >= 0 AND discount_minor >= 0 AND payable_minor >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 4. order_lines (订单行)
-- ============================================================
CREATE TABLE IF NOT EXISTS order_lines (
    id BIGINT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    sku_id BIGINT,
    sku_name VARCHAR(128),
    quantity INT DEFAULT 1,
    unit_price_minor BIGINT DEFAULT 0,
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 5. order_events (订单事件/流水)
-- ============================================================
CREATE TABLE IF NOT EXISTS order_events (
    id BIGINT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    from_status VARCHAR(32),
    to_status VARCHAR(32),
    operator VARCHAR(64),
    reason VARCHAR(512),
    created_at DATETIME,
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 6. payment_orders (支付单)
-- ============================================================
CREATE TABLE IF NOT EXISTS payment_orders (
    id BIGINT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    out_trade_no VARCHAR(64) NOT NULL UNIQUE,
    transaction_id VARCHAR(64),
    amount_minor BIGINT DEFAULT 0,
    channel VARCHAR(32) DEFAULT 'WECHAT_JSAPI',
    status VARCHAR(20) DEFAULT 'PENDING',
    prepay_id VARCHAR(128),
    pay_sign VARCHAR(256),
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_order_id (order_id),
    INDEX idx_out_trade_no (out_trade_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 7. contracts (合同)
-- ============================================================
CREATE TABLE IF NOT EXISTS contracts (
    id BIGINT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    template_version VARCHAR(16),
    cos_key VARCHAR(256),
    pdf_hash VARCHAR(128),
    sign_status VARCHAR(20) DEFAULT 'PENDING',
    created_at DATETIME,
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 8-9-10-11. catalog (商品目录)
-- ============================================================
CREATE TABLE IF NOT EXISTS skus (
    id BIGINT PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    type VARCHAR(20),
    brand_id BIGINT,
    model_id BIGINT,
    description TEXT,
    specs_json TEXT,
    subtitle VARCHAR(256),
    original_price_minor BIGINT DEFAULT 0,
    adapted_scenes_text VARCHAR(512),
    stock_status_text VARCHAR(128),
    delivery_text VARCHAR(128),
    status VARCHAR(16) DEFAULT 'ONLINE',
    stock INT DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_brand_id (brand_id),
    CONSTRAINT chk_sku_price_stock CHECK (stock >= 0 AND original_price_minor >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sku_media (
    id BIGINT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    url VARCHAR(512),
    type VARCHAR(16),
    sort_order INT DEFAULT 0,
    INDEX idx_sku_id (sku_id),
    CONSTRAINT fk_sku_media_sku FOREIGN KEY (sku_id) REFERENCES skus(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sku_prices (
    id BIGINT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    price_type VARCHAR(32),
    price_minor BIGINT DEFAULT 0,
    min_duration INT,
    max_duration INT,
    daily_rate_minor BIGINT DEFAULT 0,
    INDEX idx_sku_id (sku_id),
    CONSTRAINT fk_sku_prices_sku FOREIGN KEY (sku_id) REFERENCES skus(id),
    CONSTRAINT chk_sku_prices_amount CHECK (price_minor >= 0 AND daily_rate_minor >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sku_tags (
    id BIGINT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    sort_order INT DEFAULT 0,
    INDEX idx_sku_id (sku_id),
    UNIQUE KEY uk_sku_tag_name (sku_id, name),
    CONSTRAINT fk_sku_tags_sku FOREIGN KEY (sku_id) REFERENCES skus(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sku_services (
    id BIGINT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    price_label VARCHAR(64) NOT NULL DEFAULT '免费',
    sort_order INT DEFAULT 0,
    INDEX idx_sku_id (sku_id),
    CONSTRAINT fk_sku_services_sku FOREIGN KEY (sku_id) REFERENCES skus(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sku_detail_sections (
    id BIGINT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    content TEXT NOT NULL,
    sort_order INT DEFAULT 0,
    INDEX idx_sku_id (sku_id),
    CONSTRAINT fk_sku_detail_sections_sku FOREIGN KEY (sku_id) REFERENCES skus(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS catalog_filter_groups (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(64) NOT NULL,
    filter_field VARCHAR(32) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    INDEX idx_enabled_sort (enabled, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS catalog_filter_options (
    id BIGINT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    label VARCHAR(64) NOT NULL,
    value VARCHAR(128),
    min_price_minor BIGINT,
    max_price_minor BIGINT,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    INDEX idx_group_sort (group_id, enabled, sort_order),
    CONSTRAINT fk_catalog_filter_options_group FOREIGN KEY (group_id) REFERENCES catalog_filter_groups(id),
    CONSTRAINT chk_catalog_filter_price CHECK ((min_price_minor IS NULL OR min_price_minor >= 0) AND (max_price_minor IS NULL OR max_price_minor >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS scenes (
    id BIGINT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(512),
    image_url VARCHAR(512),
    sort_order INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS scene_sku_rel (
    id BIGINT PRIMARY KEY,
    scene_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    INDEX idx_scene_id (scene_id),
    UNIQUE KEY uk_scene_sku (scene_id, sku_id),
    CONSTRAINT fk_scene_sku_scene FOREIGN KEY (scene_id) REFERENCES scenes(id),
    CONSTRAINT fk_scene_sku_sku FOREIGN KEY (sku_id) REFERENCES skus(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 12-13-14. wallet (钱包)
-- ============================================================
CREATE TABLE IF NOT EXISTS wallets (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance_minor BIGINT NOT NULL DEFAULT 0,
    frozen_minor BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_id (user_id),
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_wallet_amounts CHECK (balance_minor >= 0 AND frozen_minor >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS wallet_ledger (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(16) NOT NULL,
    amount_minor BIGINT NOT NULL DEFAULT 0,
    ref_type VARCHAR(32),
    ref_id VARCHAR(64),
    balance_after_minor BIGINT NOT NULL DEFAULT 0,
    description VARCHAR(256),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    CONSTRAINT fk_wallet_ledger_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_wallet_ledger_amount CHECK (amount_minor >= 0 AND balance_after_minor >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS withdraw_requests (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount_minor BIGINT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    wx_batch_id VARCHAR(64),
    fail_reason VARCHAR(256),
    idempotency_key VARCHAR(128),
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    UNIQUE KEY uk_idempotency (idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 15-16-17. asset (分身资产)
-- ============================================================
CREATE TABLE IF NOT EXISTS user_assets (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(16) NOT NULL,
    model_info VARCHAR(256),
    image_url VARCHAR(512),
    status VARCHAR(16) NOT NULL DEFAULT 'IDLE',
    purchase_order_id VARCHAR(64),
    acquired_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_user_assets_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS trusteeship_slots (
    id BIGINT PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    start_time DATETIME,
    end_time DATETIME,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    daily_rate_minor BIGINT NOT NULL DEFAULT 0,
    total_revenue_minor BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_asset_id (asset_id),
    CONSTRAINT fk_trusteeship_asset FOREIGN KEY (asset_id) REFERENCES user_assets(id),
    CONSTRAINT fk_trusteeship_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_trusteeship_period CHECK (end_time > start_time),
    CONSTRAINT chk_trusteeship_amount CHECK (daily_rate_minor > 0 AND total_revenue_minor >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS asset_revenue_ledger (
    id BIGINT PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT,
    amount_minor BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_SETTLE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_asset_id (asset_id),
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_asset_revenue_asset FOREIGN KEY (asset_id) REFERENCES user_assets(id),
    CONSTRAINT fk_asset_revenue_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_asset_revenue_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT chk_asset_revenue_amount CHECK (amount_minor >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 18-19-20-21. member (会员)
-- ============================================================
CREATE TABLE IF NOT EXISTS user_membership (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    level INT NOT NULL DEFAULT 0,
    is_native TINYINT(1) NOT NULL DEFAULT 0,
    lifetime_spend_minor BIGINT NOT NULL DEFAULT 0,
    planet_card_expires_at DATETIME,
    created_at DATETIME,
    updated_at DATETIME,
    UNIQUE KEY uk_user_id (user_id),
    CONSTRAINT fk_membership_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_membership_level CHECK (level BETWEEN 0 AND 3),
    CONSTRAINT chk_membership_spend CHECK (lifetime_spend_minor >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS membership_benefit_grants (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    benefit_type VARCHAR(32) NOT NULL,
    total_count INT NOT NULL DEFAULT 0,
    used_count INT NOT NULL DEFAULT 0,
    expires_at DATETIME,
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_benefit_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_benefit_usage CHECK (total_count >= 0 AND used_count >= 0 AND used_count <= total_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS membership_events (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_type VARCHAR(32),
    description VARCHAR(256),
    created_at DATETIME,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS planet_card_skus (
    id BIGINT PRIMARY KEY,
    name VARCHAR(128),
    duration_days INT DEFAULT 365,
    price_minor BIGINT DEFAULT 0,
    benefit_level INT DEFAULT 1,
    stock INT DEFAULT 0,
    status VARCHAR(16) DEFAULT 'ON_SALE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 22-31. repair (报修工单与巡检)
-- ============================================================
CREATE TABLE IF NOT EXISTS devices (
    id BIGINT PRIMARY KEY,
    device_no VARCHAR(64) UNIQUE,
    name VARCHAR(128),
    location VARCHAR(256),
    area VARCHAR(64),
    qr_code VARCHAR(256),
    responsible_technician_id BIGINT,
    type VARCHAR(32),
    model VARCHAR(64),
    manufacturer VARCHAR(64),
    install_date DATETIME,
    status VARCHAR(16),
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_device_no (device_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS work_orders (
    id BIGINT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    device_id BIGINT,
    device_name VARCHAR(128),
    reporter_user_id BIGINT NOT NULL,
    reporter_name VARCHAR(64),
    reporter_phone VARCHAR(20),
    fault_type VARCHAR(32),
    fault_description TEXT,
    priority VARCHAR(16) DEFAULT 'MEDIUM',
    status VARCHAR(24) DEFAULT 'NEW',
    assigned_technician_id BIGINT,
    images TEXT,
    solution_description TEXT,
    completed_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_reporter (reporter_user_id),
    INDEX idx_technician (assigned_technician_id),
    INDEX idx_status (status),
    CONSTRAINT fk_work_order_device FOREIGN KEY (device_id) REFERENCES devices(id),
    CONSTRAINT fk_work_order_reporter FOREIGN KEY (reporter_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS work_order_logs (
    id BIGINT PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    action VARCHAR(32),
    operator_id BIGINT,
    operator_name VARCHAR(64),
    from_status VARCHAR(24),
    to_status VARCHAR(24),
    remark VARCHAR(512),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_work_order_id (work_order_id),
    CONSTRAINT fk_work_order_log_order FOREIGN KEY (work_order_id) REFERENCES work_orders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inspection_templates (
    id BIGINT PRIMARY KEY,
    name VARCHAR(128),
    description VARCHAR(512),
    check_items TEXT,
    created_by BIGINT,
    created_at DATETIME,
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inspection_plans (
    id BIGINT PRIMARY KEY,
    template_id BIGINT,
    name VARCHAR(128),
    area VARCHAR(64),
    frequency VARCHAR(32),
    assigned_technician_id BIGINT,
    status VARCHAR(16),
    next_run_at DATETIME,
    created_at DATETIME,
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inspection_tasks (
    id BIGINT PRIMARY KEY,
    plan_id BIGINT,
    template_id BIGINT,
    template_name VARCHAR(128),
    assigned_technician_id BIGINT,
    status VARCHAR(20) DEFAULT 'PENDING',
    deadline DATETIME,
    started_at DATETIME,
    completed_at DATETIME,
    created_at DATETIME,
    INDEX idx_technician (assigned_technician_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inspection_records (
    id BIGINT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    check_item VARCHAR(128),
    result VARCHAR(16),
    issue_description TEXT,
    images TEXT,
    recorded_at DATETIME,
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS feedbacks (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT,
    images TEXT,
    status VARCHAR(16) DEFAULT 'PENDING',
    handler_id BIGINT,
    reply VARCHAR(512),
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS knowledge_articles (
    id BIGINT PRIMARY KEY,
    title VARCHAR(256),
    content TEXT,
    category VARCHAR(64),
    tags VARCHAR(256),
    author_id BIGINT,
    status VARCHAR(16),
    view_count INT DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    title VARCHAR(128) NOT NULL,
    content VARCHAR(512) NOT NULL,
    ref_id VARCHAR(64),
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id_read (user_id, is_read),
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 32-37. community (社区)
-- ============================================================
CREATE TABLE IF NOT EXISTS posts (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    circle_id BIGINT,
    topic_id BIGINT,
    title VARCHAR(256),
    content TEXT,
    status VARCHAR(24) DEFAULT 'AUDITING',
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    is_pinned TINYINT(1) DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME,
    deleted INT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS post_media (
    id BIGINT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    url VARCHAR(512),
    type VARCHAR(16),
    sort_order INT DEFAULT 0,
    INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS post_audit_logs (
    id BIGINT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    audit_source VARCHAR(32),
    result VARCHAR(16),
    raw_response TEXT,
    created_at DATETIME,
    INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS circles (
    id BIGINT PRIMARY KEY,
    name VARCHAR(128),
    description VARCHAR(512),
    icon_url VARCHAR(512),
    member_count INT DEFAULT 0,
    post_count INT DEFAULT 0,
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS circle_members (
    id BIGINT PRIMARY KEY,
    circle_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(16) DEFAULT 'MEMBER',
    joined_at DATETIME,
    UNIQUE KEY uk_circle_user (circle_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT,
    content TEXT,
    status VARCHAR(16) DEFAULT 'VISIBLE',
    created_at DATETIME,
    INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 38-39-40. distribution (分销)
-- ============================================================
CREATE TABLE IF NOT EXISTS invite_relations (
    id BIGINT PRIMARY KEY,
    inviter_user_id BIGINT NOT NULL,
    invitee_user_id BIGINT NOT NULL,
    level INT NOT NULL DEFAULT 1,
    bound_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    source VARCHAR(32),
    INDEX idx_inviter (inviter_user_id),
    INDEX idx_invitee (invitee_user_id),
    UNIQUE KEY uk_inviter_invitee_level (inviter_user_id, invitee_user_id, level),
    CONSTRAINT fk_invite_inviter FOREIGN KEY (inviter_user_id) REFERENCES users(id),
    CONSTRAINT fk_invite_invitee FOREIGN KEY (invitee_user_id) REFERENCES users(id),
    CONSTRAINT chk_invite_level CHECK (level IN (1, 2) AND inviter_user_id <> invitee_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS commission_entries (
    id BIGINT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    beneficiary_user_id BIGINT NOT NULL,
    source_user_id BIGINT NOT NULL,
    level INT DEFAULT 1,
    amount_minor BIGINT NOT NULL DEFAULT 0,
    rate_percent INT NOT NULL DEFAULT 5,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_PROTECT',
    protect_until DATETIME,
    settled_at DATETIME,
    created_at DATETIME,
    INDEX idx_beneficiary (beneficiary_user_id),
    INDEX idx_status (status),
    UNIQUE KEY uk_order_beneficiary_level (order_id, beneficiary_user_id, level),
    CONSTRAINT fk_commission_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_commission_beneficiary FOREIGN KEY (beneficiary_user_id) REFERENCES users(id),
    CONSTRAINT fk_commission_source FOREIGN KEY (source_user_id) REFERENCES users(id),
    CONSTRAINT chk_commission_amount CHECK (amount_minor >= 0 AND rate_percent BETWEEN 0 AND 100 AND level IN (1, 2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS commission_settlement_batches (
    id BIGINT PRIMARY KEY,
    batch_no VARCHAR(64) UNIQUE,
    settled_at DATETIME,
    total_entries INT DEFAULT 0,
    total_amount_minor BIGINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 41. brands (品牌)
-- ============================================================
CREATE TABLE IF NOT EXISTS brands (
    id BIGINT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    logo_url VARCHAR(512),
    sort_order INT DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add brand relation after both skus and brands exist. Existing databases may
-- already have the constraint; init runs with continue-on-error in dev.
ALTER TABLE skus
    ADD CONSTRAINT fk_skus_brand FOREIGN KEY (brand_id) REFERENCES brands(id);

-- ============================================================
-- 42. app_configs (运营配置)
-- ============================================================
CREATE TABLE IF NOT EXISTS app_configs (
    id BIGINT PRIMARY KEY,
    config_key VARCHAR(64) NOT NULL UNIQUE,
    value_json TEXT,
    version INT DEFAULT 1,
    effective_at DATETIME,
    description VARCHAR(256)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 42. scheduled_job_runs (定时任务运行记录)
-- ============================================================
CREATE TABLE IF NOT EXISTS marketing_activities (
    id BIGINT PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    subtitle VARCHAR(256),
    tag VARCHAR(64),
    cover_url VARCHAR(512),
    video_url VARCHAR(512),
    description VARCHAR(1024),
    content_json TEXT,
    link_url VARCHAR(512),
    start_at DATETIME NOT NULL,
    end_at DATETIME,
    sort_order INT NOT NULL DEFAULT 0,
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_activity_status_time (status, start_at, end_at),
    INDEX idx_activity_sort (sort_order),
    CONSTRAINT chk_activity_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'OFFLINE')),
    CONSTRAINT chk_activity_period CHECK (end_at IS NULL OR end_at >= start_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS scheduled_job_runs (
    id BIGINT PRIMARY KEY,
    job_name VARCHAR(64),
    start_at DATETIME,
    end_at DATETIME,
    status VARCHAR(16),
    result TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 初始化种子数据 (运营配置默认值)
-- ============================================================
SET NAMES utf8mb4;

INSERT INTO app_configs (id, config_key, value_json, version, description) VALUES
(1, 'app.full', '{"version":"1.0.0","topics":[{"id":"topic-1","name":"展会应用"},{"id":"topic-2","name":"工业巡检"},{"id":"topic-3","name":"科研教育"},{"id":"topic-4","name":"表演娱乐"}],"planetCards":[{"skuId":"pc-001","name":"星际旅者卡","priceMinor":19900,"description":"解锁Lv.2权益"},{"skuId":"pc-002","name":"银河领主卡","priceMinor":49900,"description":"解锁Lv.3权益"}],"membershipRules":[],"qaBlocks":{},"trusteeshipPricingNote":"平台统一定价"}', 1, '全量配置默认值');

INSERT INTO admin_users (id, username, password_hash, display_name, role, enabled) VALUES
(1, 'admin', '$2a$10$/3lB7z0fgqUAUaMW5XXvze1a8emC9JcAdPMrpf4Qmwgx8BC8ivNte', '管理员', 'ADMIN', 1);
