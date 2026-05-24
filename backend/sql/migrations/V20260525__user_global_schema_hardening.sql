-- User center, wallet accounting and global configuration schema hardening.
-- Apply once to an existing MySQL 8.0 database. This migration is additive:
-- existing runtime tables and columns are kept for backward compatibility.
USE xingqiu_dev;

-- ============================================================
-- 1. User core hardening
-- ============================================================
ALTER TABLE users
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' AFTER role,
    ADD COLUMN user_type VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER' AFTER status,
    ADD COLUMN registered_at DATETIME NULL AFTER user_type,
    ADD COLUMN last_login_at DATETIME NULL AFTER registered_at,
    ADD COLUMN deleted TINYINT(1) NOT NULL DEFAULT 0 AFTER last_login_at,
    ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER deleted;

UPDATE users
SET status = CASE WHEN role = 'DISABLED' THEN 'DISABLED' ELSE 'ACTIVE' END,
    registered_at = COALESCE(registered_at, created_at)
WHERE id IS NOT NULL;

ALTER TABLE users
    ADD INDEX idx_users_status_created (status, created_at),
    ADD INDEX idx_users_type_status (user_type, status),
    ADD INDEX idx_users_unionid (unionid),
    ADD CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'DISABLED', 'RISK_LOCKED', 'DELETED')),
    ADD CONSTRAINT chk_users_type CHECK (user_type IN ('CUSTOMER', 'TECHNICIAN', 'STAFF', 'ADMIN'));

CREATE TABLE user_auth_identities (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(32) NOT NULL,
    app_id VARCHAR(64) NOT NULL DEFAULT '',
    openid VARCHAR(128) NOT NULL,
    unionid VARCHAR(128),
    session_key_hash VARCHAR(128),
    bound_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_auth_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_auth_provider_openid (provider, app_id, openid),
    INDEX idx_auth_user (user_id),
    INDEX idx_auth_unionid (unionid),
    CONSTRAINT fk_auth_identity_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_auth_provider CHECK (provider IN ('WECHAT_MINI', 'WECHAT_OFFICIAL', 'PHONE', 'ADMIN_PASSWORD'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO user_auth_identities
    (id, user_id, provider, app_id, openid, unionid, bound_at, last_auth_at, created_at, updated_at)
SELECT id, id, 'WECHAT_MINI', 'default', openid, unionid,
       COALESCE(created_at, CURRENT_TIMESTAMP), last_login_at,
       COALESCE(created_at, CURRENT_TIMESTAMP), COALESCE(updated_at, CURRENT_TIMESTAMP)
FROM users
WHERE openid IS NOT NULL AND openid <> ''
ON DUPLICATE KEY UPDATE
    user_id = VALUES(user_id),
    unionid = VALUES(unionid),
    updated_at = VALUES(updated_at);

CREATE TABLE user_profiles (
    user_id BIGINT PRIMARY KEY,
    nickname_snapshot VARCHAR(64),
    avatar_url_snapshot VARCHAR(512),
    real_name VARCHAR(64),
    phone_masked VARCHAR(32),
    phone_hash VARCHAR(128),
    gender VARCHAR(16),
    province VARCHAR(64),
    city VARCHAR(64),
    metadata_json JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_user_profiles_gender CHECK (gender IS NULL OR gender IN ('UNKNOWN', 'MALE', 'FEMALE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO user_profiles
    (user_id, nickname_snapshot, avatar_url_snapshot, created_at, updated_at)
SELECT id, nickname, avatar_url, COALESCE(created_at, CURRENT_TIMESTAMP), COALESCE(updated_at, CURRENT_TIMESTAMP)
FROM users
ON DUPLICATE KEY UPDATE
    nickname_snapshot = VALUES(nickname_snapshot),
    avatar_url_snapshot = VALUES(avatar_url_snapshot),
    updated_at = VALUES(updated_at);

CREATE TABLE user_addresses (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(64) NOT NULL,
    phone_masked VARCHAR(32) NOT NULL,
    phone_hash VARCHAR(128),
    province VARCHAR(64) NOT NULL,
    city VARCHAR(64) NOT NULL,
    district VARCHAR(64),
    detail_encrypted VARCHAR(1024) NOT NULL,
    postal_code VARCHAR(16),
    is_default TINYINT(1) NOT NULL DEFAULT 0,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_addresses_user (user_id, status, is_default),
    CONSTRAINT fk_user_addresses_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_user_addresses_status CHECK (status IN ('ACTIVE', 'DELETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 2. RBAC foundation for miniapp users and admin users
-- ============================================================
CREATE TABLE roles (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(64) NOT NULL,
    scope VARCHAR(20) NOT NULL,
    description VARCHAR(256),
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_roles_scope_code (scope, code),
    INDEX idx_roles_scope_enabled (scope, enabled),
    CONSTRAINT chk_roles_scope CHECK (scope IN ('MINIAPP', 'ADMIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO roles (id, code, name, scope, description, enabled) VALUES
(1, 'USER', '普通用户', 'MINIAPP', '小程序默认用户', 1),
(2, 'TECHNICIAN', '技术人员', 'MINIAPP', '报修和巡检技术人员', 1),
(3, 'ADMIN', '小程序管理员', 'MINIAPP', '兼容旧用户角色字段', 1),
(101, 'ADMIN', '后台管理员', 'ADMIN', '后台管理最高权限', 1),
(102, 'OPERATOR', '后台运营', 'ADMIN', '后台运营人员', 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    enabled = VALUES(enabled),
    updated_at = CURRENT_TIMESTAMP;

CREATE TABLE user_role_rel (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    granted_by BIGINT,
    granted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME,
    PRIMARY KEY (user_id, role_id),
    INDEX idx_user_role_role (role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO user_role_rel (user_id, role_id, granted_at)
SELECT id,
       CASE role WHEN 'TECHNICIAN' THEN 2 WHEN 'ADMIN' THEN 3 ELSE 1 END,
       COALESCE(created_at, CURRENT_TIMESTAMP)
FROM users
WHERE role <> 'DISABLED';

CREATE TABLE admin_user_role_rel (
    admin_user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    granted_by BIGINT,
    granted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME,
    PRIMARY KEY (admin_user_id, role_id),
    INDEX idx_admin_user_role_role (role_id),
    CONSTRAINT fk_admin_user_role_admin FOREIGN KEY (admin_user_id) REFERENCES admin_users(id),
    CONSTRAINT fk_admin_user_role_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO admin_user_role_rel (admin_user_id, role_id, granted_at)
SELECT id,
       CASE role WHEN 'ADMIN' THEN 101 ELSE 102 END,
       COALESCE(created_at, CURRENT_TIMESTAMP)
FROM admin_users;

-- ============================================================
-- 3. Wallet accounting canonical tables
-- ============================================================
ALTER TABLE wallets
    ADD COLUMN currency VARCHAR(16) NOT NULL DEFAULT 'LIGHT_YEAR' AFTER user_id,
    ADD COLUMN total_credit_minor BIGINT NOT NULL DEFAULT 0 AFTER frozen_minor,
    ADD COLUMN total_debit_minor BIGINT NOT NULL DEFAULT 0 AFTER total_credit_minor,
    ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER total_debit_minor;

ALTER TABLE wallets
    ADD INDEX idx_wallets_currency (currency),
    ADD CONSTRAINT chk_wallets_currency CHECK (currency IN ('LIGHT_YEAR', 'CNY')),
    ADD CONSTRAINT chk_wallets_totals CHECK (total_credit_minor >= 0 AND total_debit_minor >= 0);

CREATE TABLE wallet_accounts (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    currency VARCHAR(16) NOT NULL DEFAULT 'LIGHT_YEAR',
    available_minor BIGINT NOT NULL DEFAULT 0,
    frozen_minor BIGINT NOT NULL DEFAULT 0,
    total_credit_minor BIGINT NOT NULL DEFAULT 0,
    total_debit_minor BIGINT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_wallet_accounts_user_currency (user_id, currency),
    CONSTRAINT fk_wallet_accounts_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_wallet_accounts_amounts CHECK (
        available_minor >= 0 AND frozen_minor >= 0 AND
        total_credit_minor >= 0 AND total_debit_minor >= 0
    ),
    CONSTRAINT chk_wallet_accounts_currency CHECK (currency IN ('LIGHT_YEAR', 'CNY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO wallet_accounts
    (id, user_id, currency, available_minor, frozen_minor, total_credit_minor, total_debit_minor, created_at, updated_at)
SELECT id, user_id, currency, balance_minor, frozen_minor, total_credit_minor, total_debit_minor,
       COALESCE(created_at, CURRENT_TIMESTAMP), COALESCE(updated_at, CURRENT_TIMESTAMP)
FROM wallets
ON DUPLICATE KEY UPDATE
    available_minor = VALUES(available_minor),
    frozen_minor = VALUES(frozen_minor),
    updated_at = VALUES(updated_at);

CREATE TABLE wallet_ledger_entries (
    id BIGINT PRIMARY KEY,
    wallet_id BIGINT,
    user_id BIGINT NOT NULL,
    currency VARCHAR(16) NOT NULL DEFAULT 'LIGHT_YEAR',
    direction VARCHAR(16) NOT NULL,
    amount_minor BIGINT NOT NULL,
    available_after_minor BIGINT NOT NULL,
    frozen_after_minor BIGINT NOT NULL DEFAULT 0,
    biz_type VARCHAR(32) NOT NULL,
    biz_id VARCHAR(64),
    idempotency_key VARCHAR(128),
    description VARCHAR(256),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_wallet_ledger_idem (idempotency_key),
    INDEX idx_wallet_ledger_user_created (user_id, created_at),
    INDEX idx_wallet_ledger_biz (biz_type, biz_id),
    CONSTRAINT fk_wallet_ledger_entries_wallet FOREIGN KEY (wallet_id) REFERENCES wallet_accounts(id),
    CONSTRAINT fk_wallet_ledger_entries_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_wallet_ledger_direction CHECK (direction IN ('CREDIT', 'DEBIT', 'FREEZE', 'UNFREEZE')),
    CONSTRAINT chk_wallet_ledger_amounts CHECK (
        amount_minor > 0 AND available_after_minor >= 0 AND frozen_after_minor >= 0
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO wallet_ledger_entries
    (id, wallet_id, user_id, currency, direction, amount_minor, available_after_minor,
     frozen_after_minor, biz_type, biz_id, description, created_at)
SELECT wl.id, wa.id, wl.user_id, wa.currency, wl.type, wl.amount_minor, wl.balance_after_minor,
       wa.frozen_minor, COALESCE(wl.ref_type, 'UNKNOWN'), wl.ref_id, wl.description,
       COALESCE(wl.created_at, CURRENT_TIMESTAMP)
FROM wallet_ledger wl
LEFT JOIN wallet_accounts wa ON wa.user_id = wl.user_id AND wa.currency = 'LIGHT_YEAR'
WHERE wl.amount_minor > 0
ON DUPLICATE KEY UPDATE
    description = VALUES(description);

ALTER TABLE withdraw_requests
    ADD COLUMN channel VARCHAR(32) NOT NULL DEFAULT 'WECHAT_TRANSFER' AFTER status,
    ADD COLUMN channel_transfer_id VARCHAR(128) AFTER wx_batch_id,
    ADD COLUMN requested_at DATETIME AFTER idempotency_key,
    ADD COLUMN completed_at DATETIME AFTER requested_at,
    ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER completed_at;

UPDATE withdraw_requests
SET requested_at = COALESCE(requested_at, created_at),
    completed_at = CASE WHEN status IN ('SUCCESS', 'FAILED') THEN COALESCE(completed_at, updated_at) ELSE completed_at END
WHERE id IS NOT NULL;

ALTER TABLE withdraw_requests
    ADD INDEX idx_withdraw_status_created (status, created_at),
    ADD INDEX idx_withdraw_channel_batch (channel, wx_batch_id),
    ADD CONSTRAINT chk_withdraw_status CHECK (status IN ('PENDING', 'PROCESSING', 'SUCCESS', 'FAILED', 'CANCELLED')),
    ADD CONSTRAINT chk_withdraw_amount CHECK (amount_minor > 0);

-- ============================================================
-- 4. Global configuration, dictionary, media and audit foundation
-- ============================================================
CREATE TABLE app_config_items (
    id BIGINT PRIMARY KEY,
    config_key VARCHAR(128) NOT NULL,
    config_type VARCHAR(32) NOT NULL DEFAULT 'JSON',
    value_json JSON NOT NULL,
    schema_json JSON,
    version INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    effective_at DATETIME,
    description VARCHAR(256),
    created_by BIGINT,
    updated_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_app_config_items_key (config_key),
    INDEX idx_app_config_items_status (status, effective_at),
    CONSTRAINT chk_app_config_items_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'OFFLINE')),
    CONSTRAINT chk_app_config_items_type CHECK (config_type IN ('JSON', 'LIST', 'SCALAR', 'FEATURE_FLAG'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO app_config_items
    (id, config_key, config_type, value_json, version, status, effective_at, description, created_at, updated_at)
SELECT id, config_key,
       CASE
           WHEN JSON_VALID(value_json) AND LEFT(TRIM(value_json), 1) = '[' THEN 'LIST'
           WHEN JSON_VALID(value_json) AND LEFT(TRIM(value_json), 1) = '{' THEN 'JSON'
           ELSE 'SCALAR'
       END,
       CASE WHEN JSON_VALID(value_json) THEN value_json ELSE JSON_QUOTE(COALESCE(value_json, '')) END,
       COALESCE(version, 1), 'PUBLISHED', effective_at, description, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM app_configs
WHERE config_key IS NOT NULL
ON DUPLICATE KEY UPDATE
    value_json = VALUES(value_json),
    version = VALUES(version),
    updated_at = CURRENT_TIMESTAMP;

CREATE TABLE app_config_revisions (
    id BIGINT PRIMARY KEY,
    config_key VARCHAR(128) NOT NULL,
    version INT NOT NULL,
    value_json JSON NOT NULL,
    schema_json JSON,
    change_note VARCHAR(512),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_app_config_revision (config_key, version),
    INDEX idx_app_config_revision_key (config_key, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO app_config_revisions
    (id, config_key, version, value_json, change_note, created_at)
SELECT id, config_key, COALESCE(version, 1),
       CASE WHEN JSON_VALID(value_json) THEN value_json ELSE JSON_QUOTE(COALESCE(value_json, '')) END,
       'Initial revision migrated from app_configs', CURRENT_TIMESTAMP
FROM app_configs
WHERE config_key IS NOT NULL;

CREATE TABLE app_config_publish_batches (
    id BIGINT PRIMARY KEY,
    batch_no VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    description VARCHAR(512),
    published_by BIGINT,
    published_at DATETIME,
    rollback_from_batch_no VARCHAR(64),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_config_publish_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ROLLED_BACK'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE dict_types (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    scope VARCHAR(32) NOT NULL DEFAULT 'GLOBAL',
    description VARCHAR(256),
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE dict_items (
    id BIGINT PRIMARY KEY,
    type_code VARCHAR(64) NOT NULL,
    item_code VARCHAR(64) NOT NULL,
    label VARCHAR(128) NOT NULL,
    value VARCHAR(256),
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    metadata_json JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dict_item (type_code, item_code),
    INDEX idx_dict_items_type_sort (type_code, enabled, sort_order),
    CONSTRAINT fk_dict_items_type FOREIGN KEY (type_code) REFERENCES dict_types(code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE media_files (
    id BIGINT PRIMARY KEY,
    storage VARCHAR(32) NOT NULL DEFAULT 'LOCAL',
    bucket VARCHAR(128),
    object_key VARCHAR(512) NOT NULL,
    url VARCHAR(512) NOT NULL,
    mime_type VARCHAR(128),
    size_bytes BIGINT,
    sha256 VARCHAR(128),
    uploaded_by BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_media_object (storage, object_key),
    INDEX idx_media_sha256 (sha256),
    INDEX idx_media_uploaded_by (uploaded_by),
    CONSTRAINT chk_media_status CHECK (status IN ('ACTIVE', 'DELETED')),
    CONSTRAINT chk_media_size CHECK (size_bytes IS NULL OR size_bytes >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE operation_audit_logs (
    id BIGINT PRIMARY KEY,
    actor_type VARCHAR(20) NOT NULL,
    actor_id BIGINT,
    action VARCHAR(64) NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    target_id VARCHAR(64),
    before_json JSON,
    after_json JSON,
    request_id VARCHAR(128),
    trace_id VARCHAR(128),
    ip VARCHAR(64),
    user_agent VARCHAR(512),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_actor (actor_type, actor_id, created_at),
    INDEX idx_audit_target (target_type, target_id, created_at),
    INDEX idx_audit_trace (trace_id),
    CONSTRAINT chk_audit_actor_type CHECK (actor_type IN ('USER', 'ADMIN', 'SYSTEM'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE idempotency_records (
    id BIGINT PRIMARY KEY,
    scope VARCHAR(64) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    request_hash VARCHAR(128) NOT NULL,
    response_json JSON,
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    owner_id BIGINT,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_idempotency_scope_key (scope, idempotency_key),
    INDEX idx_idempotency_expire (expires_at),
    CONSTRAINT chk_idempotency_status CHECK (status IN ('PROCESSING', 'SUCCEEDED', 'FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE scheduled_job_runs
    ADD COLUMN trace_id VARCHAR(128) AFTER job_name,
    ADD COLUMN duration_ms BIGINT AFTER end_at,
    ADD COLUMN error_message VARCHAR(1024) AFTER result;

ALTER TABLE scheduled_job_runs
    ADD INDEX idx_job_runs_name_start (job_name, start_at),
    ADD INDEX idx_job_runs_trace (trace_id),
    ADD CONSTRAINT chk_job_runs_duration CHECK (duration_ms IS NULL OR duration_ms >= 0);
