-- Development seed data. The mini program never reads this file directly:
-- every screen loads these records through authenticated API/database queries.
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- Existing development databases are upgraded in place. Duplicate-column
-- errors on subsequent boots are ignored by spring.sql.init.continue-on-error.

-- ============================================================
-- Schema: community tables (ensure base tables exist before alters)
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
    collect_count INT DEFAULT 0,
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
    status VARCHAR(16) DEFAULT 'ACTIVE',
    created_at DATETIME,
    INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Schema upgrades (2026-06-06: community interaction tables)
-- ============================================================
ALTER TABLE posts ADD COLUMN collect_count INT DEFAULT 0;
ALTER TABLE comments ALTER COLUMN status SET DEFAULT 'ACTIVE';

CREATE TABLE IF NOT EXISTS post_likes (
    id BIGINT PRIMARY KEY, post_id BIGINT NOT NULL, user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_user (post_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS post_collects (
    id BIGINT PRIMARY KEY, post_id BIGINT NOT NULL, user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_user (post_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_follows (
    id BIGINT PRIMARY KEY, follower_id BIGINT NOT NULL, followee_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_follower_followee (follower_id, followee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE skus ADD COLUMN subtitle VARCHAR(256);
ALTER TABLE skus ADD COLUMN original_price_minor BIGINT DEFAULT 0;
ALTER TABLE skus ADD COLUMN adapted_scenes_text VARCHAR(512);
ALTER TABLE skus ADD COLUMN stock_status_text VARCHAR(128);
ALTER TABLE skus ADD COLUMN delivery_text VARCHAR(128);

CREATE TABLE IF NOT EXISTS sku_tags (
    id BIGINT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    sort_order INT DEFAULT 0,
    INDEX idx_sku_id (sku_id),
    UNIQUE KEY uk_sku_tag_name (sku_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sku_services (
    id BIGINT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    price_label VARCHAR(64) NOT NULL DEFAULT '鍏嶈垂',
    sort_order INT DEFAULT 0,
    INDEX idx_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sku_detail_sections (
    id BIGINT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    content TEXT NOT NULL,
    sort_order INT DEFAULT 0,
    INDEX idx_sku_id (sku_id)
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
    INDEX idx_group_sort (group_id, enabled, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
    INDEX idx_activity_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO brands (id, name, logo_url, sort_order, created_at, updated_at) VALUES
(1, '瀹囨爲绉戞妧', '', 1, NOW(), NOW()),
(2, '鍌呭埄鍙?, '', 2, NOW(), NOW());

INSERT IGNORE INTO scenes (id, name, description, image_url, sort_order) VALUES
(1, '绉戠爺鏁欒偛', '楂樻牎瀹為獙瀹ゃ€佺鐮旀満鏋勭殑鏁欏涓庣爺绌跺満鏅?, '', 1),
(2, '宸ヤ笟宸℃', '宸ュ巶涓庡洯鍖虹殑鑷姩鍖栧贰妫€鍦烘櫙', '', 2);

INSERT IGNORE INTO skus (id, name, type, brand_id, model_id, description, specs_json, status, stock, created_at, updated_at) VALUES
(1, 'G1-u2 浜哄舰鏈哄櫒浜?, 'RENT', 1, 1, '鏀寔鍏ㄨ韩杩愬姩鎺у埗涓庢櫤鑳戒氦浜?, '{"height":"1270mm","weight":"35kg"}', 'ONLINE', 16, NOW(), NOW()),
(2, 'Go2 鍥涜冻鏈哄櫒浜?, 'RENT', 1, 2, '閫傜敤浜庡伐涓氬贰妫€涓庢暀鑲插満鏅?, '{"weight":"15kg","battery":"2-4h"}', 'ONLINE', 20, NOW(), NOW()),
(3, 'GR-1 浜哄舰鏈哄櫒浜?, 'RENT', 2, 1, '閫傜敤浜庡睍鍘呰瑙ｃ€佺鐮旀暀鑲蹭笌鍟嗕笟鏈嶅姟', '{"height":"1650mm","weight":"55kg"}', 'ONLINE', 8, NOW(), NOW());

UPDATE skus SET model_id = 1 WHERE id = 1 AND model_id IS NULL;
UPDATE skus SET model_id = 2 WHERE id = 2 AND model_id IS NULL;

INSERT IGNORE INTO sku_media (id, sku_id, url, type, sort_order) VALUES
(1, 1, '', 'IMAGE', 0),
(2, 2, '', 'IMAGE', 0),
(3, 3, '', 'IMAGE', 0);

INSERT IGNORE INTO sku_prices (id, sku_id, price_type, price_minor, min_duration, max_duration, daily_rate_minor) VALUES
(1, 1, 'DAILY_RENT', 490000, 1, 365, 490000),
(2, 2, 'DAILY_RENT', 199900, 1, 365, 199900),
(3, 3, 'DAILY_RENT', 69900, 1, 365, 69900);

UPDATE skus SET
  subtitle = '浜哄舰鏈哄櫒浜虹璧佷笌璐拱涓€浣撳寲鏂规',
  original_price_minor = 1999900,
  adapted_scenes_text = '宸ヤ笟宸℃銆佹埛澶栧畨闃层€佸簲鎬ユ晳鎻淬€佺鐮旀暀鑲?,
  stock_status_text = '鐜拌揣鍏呰冻',
  delivery_text = '鍏ㄥ浗鍙鍙彂'
WHERE id = 1;

UPDATE skus SET
  subtitle = '鍥涜冻鏈哄櫒浜哄贰妫€涓庢暀鑲插満鏅柟妗?,
  original_price_minor = 899900,
  adapted_scenes_text = '宸ヤ笟宸℃銆佺鐮旀暀鑲层€佸晢涓氭湇鍔?,
  stock_status_text = '鐜拌揣鍏呰冻',
  delivery_text = '鍏ㄥ浗鍙鍙彂'
WHERE id = 2;

UPDATE skus SET
  subtitle = '鍌呭埄鍙朵汉褰㈡満鍣ㄤ汉杞婚噺绉熻祦鏂规',
  original_price_minor = 599900,
  adapted_scenes_text = '鍟嗕笟鏈嶅姟銆佸睍鍘呰瑙ｃ€佺鐮旀暀鑲?,
  stock_status_text = '鐜拌揣鍙',
  delivery_text = '鏀寔涓婇棬閮ㄧ讲'
WHERE id = 3;

INSERT IGNORE INTO sku_tags (id, sku_id, name, sort_order) VALUES
(1, 1, '鍙璧?, 1), (2, 1, '鍙喘涔?, 2), (3, 1, '鏀寔瑙嗛婕旂ず', 3),
(4, 2, '鍙璧?, 1), (5, 2, '宸ヤ笟宸℃', 2),
(6, 3, '鍙璧?, 1), (7, 3, '鍟嗕笟鏈嶅姟', 2), (8, 3, '灞曞巺璁茶В', 3);

INSERT IGNORE INTO sku_services (id, sku_id, name, price_label, sort_order) VALUES
(1, 1, '淇℃伅鐧昏涓庡煿璁?, '鍏嶈垂', 1),
(2, 1, '鎶€鏈敮鎸佷笌淇濋殰', '鍏嶈垂', 2),
(3, 1, '鍦烘櫙搴旂敤杞欢閰嶇疆', '鍏嶈垂', 3),
(4, 2, '淇℃伅鐧昏涓庡煿璁?, '鍏嶈垂', 1),
(5, 2, '杩滅▼鎶€鏈敮鎸?, '鍏嶈垂', 2),
(6, 3, '閮ㄧ讲璋冭瘯', '鍏嶈垂', 1),
(7, 3, '杩滅▼鎶€鏈敮鎸?, '鍏嶈垂', 2);

INSERT IGNORE INTO sku_detail_sections (id, sku_id, title, content, sort_order) VALUES
(1, 1, '瀹㈡湇鏈嶅姟鍔熻兘璇︾粏浠嬬粛涓庢湇鍔℃潯娆?, '鍖呭惈寮€绠卞煿璁€佽繙绋嬫妧鏈敮鎸併€佷娇鐢ㄦ敞鎰忎簨椤逛笌鍞悗鑼冨洿銆?, 1),
(2, 1, '浜у搧鍖呰娓呭崟鍙婇厤閫佽鏄?, '鍖呰鍖呭惈鏈哄櫒浜烘湰浣撱€佺數姹犮€佸厖鐢靛櫒銆佹帶鍒跺櫒涓庡熀纭€宸ュ叿锛屽叏鍥藉彲绉熷彲鍙戙€?, 2),
(3, 1, '閿€鍞?浜戠鐩稿叧鎶€鏈枃妗ｅ強璐拱椤荤煡璧勬牸', '璐拱鍓嶉渶纭搴旂敤鍦烘櫙銆佹巿鏉冭寖鍥淬€佽蒋浠堕€傞厤涓庝簯绔湇鍔″紑閫氭潯浠躲€?, 3),
(4, 2, '鍥涜冻鏈哄櫒浜虹璧佹湇鍔¤鏄?, '鍖呭惈宸℃璺嚎閰嶇疆銆侀仴鎺у煿璁€佽繑鍦烘娴嬪拰鍩虹缁存姢鏈嶅姟銆?, 1),
(5, 3, '鍌呭埄鍙舵満鍣ㄤ汉绉熻祦鏈嶅姟璇存槑', '鍖呭惈灞曞巺璁茶В鍔ㄤ綔閰嶇疆銆佸熀纭€浜や簰鍩硅銆佽繙绋嬫敮鎸佸拰杩斿満妫€娴嬨€?, 1);

INSERT IGNORE INTO catalog_filter_groups (id, code, title, filter_field, sort_order, enabled) VALUES
(1, 'brand', '鍝佺墝', 'BRAND_ID', 1, 1),
(2, 'price', '浠锋牸鍖洪棿', 'PRICE_RANGE', 2, 1),
(3, 'model', '閫傞厤鏈哄瀷', 'MODEL_ID', 3, 1),
(4, 'scene', '鐑棬鍦烘櫙', 'KEYWORD', 4, 1);

INSERT IGNORE INTO catalog_filter_options (id, group_id, label, value, min_price_minor, max_price_minor, sort_order, enabled) VALUES
(1, 1, '鍏ㄩ儴鍝佺墝', '', NULL, NULL, 0, 1),
(2, 1, '瀹囨爲绉戞妧', '1', NULL, NULL, 1, 1),
(3, 1, '鍌呭埄鍙?, '2', NULL, NULL, 2, 1),
(4, 2, '鍏ㄩ儴浠锋牸', '', NULL, NULL, 0, 1),
(5, 2, '0-1000', '', 0, 100000, 1, 1),
(6, 2, '1000-5000', '', 100000, 500000, 2, 1),
(7, 2, '5000-10000', '', 500000, 1000000, 3, 1),
(8, 2, '10000浠ヤ笂', '', 1000000, NULL, 4, 1),
(9, 3, '鍏ㄩ儴鏈哄瀷', '', NULL, NULL, 0, 1),
(10, 3, 'G1绯诲垪', '1', NULL, NULL, 1, 1),
(11, 3, 'Go2绯诲垪', '2', NULL, NULL, 2, 1),
(12, 4, '鍏ㄩ儴鍦烘櫙', '', NULL, NULL, 0, 1),
(13, 4, '鏁欒偛绉戠爺', '绉戠爺鏁欒偛', NULL, NULL, 1, 1),
(14, 4, '宸ヤ笟宸℃', '宸ヤ笟宸℃', NULL, NULL, 2, 1),
(15, 4, '鍟嗕笟鏈嶅姟', '鍟嗕笟鏈嶅姟', NULL, NULL, 3, 1);

INSERT IGNORE INTO marketing_activities
(id, title, subtitle, tag, cover_url, video_url, description, content_json, link_url, start_at, end_at, sort_order, status, created_at, updated_at)
VALUES
(1, '鏄ュ鏈哄櫒浜虹璧佹椿鍔?, '绉熻祦浣庤嚦 5 鎶橈紝鏀寔浼佷笟鐭涓庣鐮旇瘯鐢?, '鏄ュ鐒曟柊瀛?, '', '', '闈㈠悜绉戠爺鏁欒偛銆佸伐涓氬贰妫€鍜屽晢涓氬睍婕斿満鏅殑闄愭椂娲诲姩銆?, '[{"title":"娲诲姩鏉冪泭","content":"娲诲姩鏈熷唴鎻愪氦绉熻祦璁㈠崟鍙幏寰楁柟妗堥【闂€佽澶囪皟璇曞拰鍩虹鍩硅銆?},{"title":"閫傜敤鍟嗗搧","content":"閫傜敤浜庡钩鍙板湪绾垮彲绉熻祦鏈哄櫒浜猴紝鍏蜂綋搴撳瓨浠ュ悗绔?SKU 鐘舵€佷负鍑嗐€?},{"title":"娲诲姩瑙勫垯","content":"娲诲姩浠锋牸銆佹娂閲戝噺鍏嶅拰鏈嶅姟鏉冪泭浠ヨ鍗曠‘璁ら〉涓庡悎鍚屼负鍑嗐€?}]', '/pages/category/index?type=RENT', '2026-05-01 00:00:00', '2026-12-31 23:59:59', 1, 'PUBLISHED', NOW(), NOW());

INSERT IGNORE INTO scene_sku_rel (id, scene_id, sku_id) VALUES
(1, 1, 1), (2, 2, 2);

INSERT IGNORE INTO app_configs (id, config_key, value_json, version, description) VALUES
(100, 'banners', '[{"id":1,"imageUrl":"","linkUrl":"/pages/category/index","title":"鏅鸿兘璁惧绉熻祦"}]', 1, '棣栭〉杞挱鍥鹃厤缃?),
(101, 'hot_keywords', '["鍥涜冻鏈哄櫒浜?,"浜哄舰鏈哄櫒浜?,"宸ヤ笟宸℃"]', 1, '鎼滅储鐑瘝'),
(102, 'scene_tags', '{"1":["鍙","绉戠爺"],"2":["鍙","宸℃"]}', 1, '鍦烘櫙鏍囩閰嶇疆'),
(103, 'recommended_skus', '[1,2,3]', 1, '棣栭〉鎺ㄨ崘鍟嗗搧閰嶇疆');

-- Relational user-center fixture. To inspect it through a real WeChat login in
-- development, associate the authenticated openid with this fixture explicitly.
INSERT IGNORE INTO users (id, openid, nickname, avatar_url, role, created_at, updated_at) VALUES
(10001, 'seed-openid-profile-owner', '寮€鍙戦獙鏀剁敤鎴?, '/static/images/default-avatar.svg', 'USER', '2026-05-01 09:00:00', NOW()),
(10002, 'seed-openid-invitee', '鍙楅個鎴愬憳', '/static/images/default-avatar.svg', 'USER', '2026-05-05 09:00:00', NOW());

INSERT IGNORE INTO wallets (id, user_id, balance_minor, frozen_minor, created_at, updated_at) VALUES
(10001, 10001, 568000, 12000, '2026-05-01 09:00:00', NOW()),
(10002, 10002, 0, 0, '2026-05-05 09:00:00', NOW());

INSERT IGNORE INTO wallet_ledger
(id, user_id, type, amount_minor, ref_type, ref_id, balance_after_minor, description, created_at) VALUES
(10001, 10001, 'CREDIT', 500000, 'ASSET_REVENUE', '10001', 500000, '鎵樼鏀剁泭缁撶畻', '2026-05-15 12:00:00'),
(10002, 10001, 'CREDIT', 68000, 'COMMISSION', '10001', 568000, '鍒嗛攢浣ｉ噾缁撶畻', '2026-05-20 12:00:00');

INSERT IGNORE INTO user_membership
(id, user_id, level, is_native, lifetime_spend_minor, planet_card_expires_at, created_at, updated_at) VALUES
(10001, 10001, 1, 1, 32000, '2027-05-01 23:59:59', '2026-05-01 09:00:00', NOW());

INSERT IGNORE INTO membership_benefit_grants
(id, user_id, benefit_type, total_count, used_count, expires_at) VALUES
(10001, 10001, 'FREE_DEPOSIT', 2, 0, '2027-05-01 23:59:59'),
(10002, 10001, 'MAINTENANCE', 1, 0, '2027-05-01 23:59:59');

INSERT IGNORE INTO planet_card_skus
(id, name, duration_days, price_minor, benefit_level, stock, status) VALUES
(1, '鏈堝害鏄熺悆鍗?, 30, 9900, 1, 100, 'ON_SALE'),
(2, '骞村害鏄熺悆鍗?, 365, 89900, 2, 100, 'ON_SALE');

INSERT IGNORE INTO orders
(id, order_no, user_id, sku_id, order_type, status, amount_minor, deposit_minor, shipping_minor, discount_minor, payable_minor, created_at, updated_at) VALUES
(10001, 'XQ202605200001', 10001, 1, 'RENT', 'COMPLETED', 490000, 0, 0, 0, 490000, '2026-05-20 10:30:00', '2026-05-20 16:00:00'),
(10002, 'XQ202605230001', 10002, 2, 'RENT', 'COMPLETED', 199900, 0, 0, 0, 199900, '2026-05-23 10:30:00', '2026-05-23 16:00:00');

INSERT IGNORE INTO order_lines (id, order_id, sku_id, sku_name, quantity, unit_price_minor) VALUES
(10001, 10001, 1, 'G1-u2 浜哄舰鏈哄櫒浜?, 1, 490000),
(10002, 10002, 2, 'Go2 鍥涜冻鏈哄櫒浜?, 1, 199900);

INSERT IGNORE INTO order_events (id, order_id, from_status, to_status, operator, reason, created_at) VALUES
(10001, 10001, NULL, 'PENDING_PAY', 'USER:10001', '鍒涘缓璁㈠崟', '2026-05-20 10:30:00'),
(10002, 10001, 'FULFILLING', 'COMPLETED', 'SYSTEM', '灞ョ害瀹屾垚', '2026-05-20 16:00:00');

INSERT IGNORE INTO user_assets
(id, user_id, name, type, model_info, status, purchase_order_id, acquired_at, created_at) VALUES
(10001, 10001, 'G1-u2 璁惧 A', 'ROBOT', 'G1-u2', 'TRUSTEED', '10001', '2026-05-20 16:00:00', '2026-05-20 16:00:00'),
(10002, 10001, 'Go2 璁惧 B', 'ROBOT', 'Go2', 'IDLE', NULL, '2026-05-21 16:00:00', '2026-05-21 16:00:00');

INSERT IGNORE INTO trusteeship_slots
(id, asset_id, user_id, start_time, end_time, status, daily_rate_minor, total_revenue_minor, created_at) VALUES
(10001, 10001, 10001, '2026-05-21 00:00:00', '2026-06-21 23:59:59', 'ACTIVE', 490000, 500000, '2026-05-20 17:00:00');

INSERT IGNORE INTO asset_revenue_ledger
(id, asset_id, user_id, order_id, amount_minor, status, created_at) VALUES
(10001, 10001, 10001, 10001, 500000, 'SETTLED', '2026-05-15 12:00:00');

INSERT IGNORE INTO invite_relations
(id, inviter_user_id, invitee_user_id, level, bound_at, source) VALUES
(10001, 10001, 10002, 1, '2026-05-05 09:00:00', 'REGISTER');

INSERT IGNORE INTO commission_entries
(id, order_id, beneficiary_user_id, source_user_id, level, amount_minor, rate_percent, status, protect_until, settled_at, created_at) VALUES
(10001, 10002, 10001, 10002, 1, 9995, 5, 'SETTLED', '2026-05-30 16:00:00', '2026-05-31 12:00:00', '2026-05-23 16:00:00');

INSERT IGNORE INTO devices
(id, device_no, name, location, area, qr_code, type, model, manufacturer, status, created_at, updated_at) VALUES
(10001, 'G1-A-10001', 'G1-u2 璁惧 A', '娣卞湷鍗楀北绉戞妧鍥?A 鏍?, '娣卞湷鍗楀北', 'DEVICE-10001', 'ROBOT', 'G1-u2', '瀹囨爲绉戞妧', 'FAULT', '2026-05-20 16:00:00', NOW());

INSERT IGNORE INTO work_orders
(id, order_no, device_id, device_name, reporter_user_id, reporter_name, fault_type, fault_description, priority, status, created_at, updated_at) VALUES
(10001, 'WO202605240001', 10001, 'G1-u2 璁惧 A', 10001, '寮€鍙戦獙鏀剁敤鎴?, '杩愬姩寮傚父', '宸﹁吙鍏宠妭杩愯鏃跺嚭鐜版姈鍔?, 'HIGH', 'NEW', '2026-05-24 09:30:00', '2026-05-24 09:30:00');

INSERT IGNORE INTO work_order_logs
(id, work_order_id, action, operator_id, operator_name, from_status, to_status, remark, created_at) VALUES
(10001, 10001, 'CREATED', 10001, '寮€鍙戦獙鏀剁敤鎴?, NULL, 'NEW', '鐢ㄦ埛鎻愪氦鎶ヤ慨', '2026-05-24 09:30:00');

INSERT IGNORE INTO notifications
(id, user_id, type, title, content, ref_id, is_read, created_at) VALUES
(10001, 10001, 'ORDER', '璁㈠崟宸插畬鎴?, '鎮ㄧ殑绉熻祦璁㈠崟 XQ202605200001 宸插畬鎴愩€?, '10001', 0, '2026-05-20 16:00:00'),
(10002, 10001, 'ASSET_REVENUE', '鎵樼鏀剁泭鍒拌处', '鎵樼鏀剁泭宸茶繘鍏ヨ处鎴蜂綑棰濓紝璇锋煡鐪嬫祦姘淬€?, '10001', 0, '2026-05-15 12:00:00');

INSERT IGNORE INTO posts (id, user_id, circle_id, title, content, status, like_count, comment_count, created_at, updated_at) VALUES
(1, 10001, NULL, 'Go2 Pro 鏂版€ц兘鎻愬崌', 'Go2 Pro 鏂版€ц兘鎻愬崌锛佹劅璋㈠ぇ瀹跺鎴戜滑鐨勬敮鎸侊紝缁忚繃鍥㈤槦鐨勫姫鍔涳紝鎴戜滑瀵?Go2 Pro 杩涜浜嗗叏闈㈠崌绾э紝鎬ц兘鎻愬崌 30%锛岀画鑸鍔?1 灏忔椂锛?, 'APPROVED', 188, 45, '2026-05-24 09:00:00', '2026-05-24 09:00:00'),
(2, 10002, NULL, '鍥涜冻鏈哄櫒浜鸿垶韫堢紪鎺?, '鍒嗕韩涓€涓嬫垜浠洟闃熺敤 Go2 鍥涜冻鏈哄櫒浜鸿繘琛岀殑鑸炶箞缂栨帓锛佹暣涓」鐩巻鏃朵竴涓湀鐨勬椂闂磋皟鏁达紝浠庡姩浣滆璁″埌绋嬪簭璋冭瘯姣忎竴姝ラ兘鍏呮弧浜嗘寫鎴樺拰涔愯叮銆?, 'APPROVED', 256, 78, '2026-05-24 07:00:00', '2026-05-24 07:00:00'),
(3, 10001, NULL, '鏈哄櫒鐙楀鏉傚湴褰㈢ǔ瀹氭€?, '鎯抽棶涓嬶紝鏈哄櫒鐙楀湪澶嶆潅鍦板舰涓嬬殑绋冲畾鎬ф€庝箞鏍凤紵鎴戜滑椤圭洰闇€瑕佸湪灞卞湴鐜浣跨敤锛屾湁缁忛獙鐨勬湅鍙嬪彲浠ュ垎浜竴涓嬩綋楠屽悧锛?, 'APPROVED', 89, 42, '2026-05-24 04:00:00', '2026-05-24 04:00:00'),
(4, 10002, NULL, 'GR-1 寮€绠变綋楠?, '鍒氭敹鍒?GR-1 閫氱敤浜哄舰鏈哄櫒浜猴紝寮€绠变綋楠岄潪甯告锛佸仛宸ョ簿缁嗭紝鍏宠妭鐏垫椿搴﹁秴鍑洪鏈熴€傚噯澶囩敤鏉ュ仛绉戠爺椤圭洰鐨勮繍鍔ㄦ帶鍒跺疄楠屻€?, 'APPROVED', 312, 56, '2026-05-23 15:00:00', '2026-05-23 15:00:00'),
(5, 10001, NULL, 'Walker S 鑷富瀵艰埅 Demo', '鎴戜滑瀛︽牎瀹為獙瀹ょ敤 Walker S 鍋氫簡涓€涓嚜涓诲鑸殑 demo锛屾晥鏋滃緢涓嶉敊銆傛湁鍏磋叮鐨勫悓瀛﹀彲浠ユ潵浜ゆ祦涓€涓嬫妧鏈柟妗堛€?, 'APPROVED', 145, 33, '2026-05-23 10:00:00', '2026-05-23 10:00:00'),
(6, 10002, NULL, '杩滃緛 A1 绮惧害娴嬭瘯鎶ュ憡', '杩滃緛 A1 鏈烘鑷傜殑绮惧害娴嬭瘯鎶ュ憡鍑烘潵浜嗭紝閲嶅瀹氫綅绮惧害杈惧埌 0.02mm锛屽畬鍏ㄦ弧瓒虫垜浠簿瀵嗚閰嶇殑闇€姹傘€?, 'APPROVED', 201, 67, '2026-05-22 14:00:00', '2026-05-22 14:00:00');

INSERT IGNORE INTO post_media (id, post_id, url, type, sort_order) VALUES
(1, 1, '', 'IMAGE', 0),
(2, 1, '', 'IMAGE', 1),
(3, 2, '', 'IMAGE', 0),
(4, 3, '', 'IMAGE', 0),
(5, 4, '', 'IMAGE', 0),
(6, 5, '', 'IMAGE', 0);

-- 鍚庡彴绠＄悊缃戠珯婕旂ず鏁版嵁锛氱敤鎴枫€佽鍗曘€侀挶鍖呫€佸垎閿€銆佽繍缁磋褰曟寜缂栧彿淇濇寔涓€鑷淬€?INSERT IGNORE INTO users (id, openid, nickname, avatar_url, role, created_at, updated_at) VALUES
(10003, 'seed-openid-lab-manager', '瀹為獙瀹ら檲鑰佸笀', '/static/images/default-avatar.svg', 'USER', '2026-05-08 11:20:00', NOW()),
(10004, 'seed-openid-factory-ops', '宸ュ巶杩愮淮鏉庡伐', '/static/images/default-avatar.svg', 'TECHNICIAN', '2026-05-10 14:15:00', NOW()),
(10005, 'seed-openid-frozen-demo', '鍐荤粨婕旂ず鐢ㄦ埛', '/static/images/default-avatar.svg', 'DISABLED', '2026-05-12 16:40:00', NOW());

INSERT IGNORE INTO user_membership
(id, user_id, level, is_native, lifetime_spend_minor, planet_card_expires_at, created_at, updated_at) VALUES
(10003, 10003, 2, 0, 689900, '2027-05-08 23:59:59', '2026-05-08 11:20:00', NOW()),
(10004, 10004, 1, 0, 199900, '2027-05-10 23:59:59', '2026-05-10 14:15:00', NOW());

INSERT IGNORE INTO wallets (id, user_id, balance_minor, frozen_minor, created_at, updated_at) VALUES
(10003, 10003, 1399800, 50000, '2026-05-08 11:20:00', NOW()),
(10004, 10004, 286000, 0, '2026-05-10 14:15:00', NOW()),
(10005, 10005, 0, 0, '2026-05-12 16:40:00', NOW());

INSERT IGNORE INTO wallet_ledger
(id, user_id, type, amount_minor, ref_type, ref_id, balance_after_minor, description, created_at) VALUES
(10003, 10003, 'CREDIT', 980000, 'RECHARGE', 'RC202605080001', 980000, '浼佷笟棰勫厖鍊?, '2026-05-08 12:00:00'),
(10004, 10003, 'DEBIT', 490000, 'CONSUME', '10003', 490000, '浜哄舰鏈哄櫒浜虹璧佽鍗曟敮浠?, '2026-05-18 10:30:00'),
(10005, 10003, 'CREDIT', 899800, 'REFUND', 'RF202605190001', 1389800, '璁惧楠屾敹鍚庢娂閲戦€€鍥?, '2026-05-19 17:20:00'),
(10006, 10004, 'CREDIT', 286000, 'COMMISSION', '10002', 286000, '缁翠慨鍛橀個璇蜂剑閲戠粨绠?, '2026-05-23 18:00:00'),
(10007, 10003, 'DEBIT', 100000, 'WITHDRAW', '10001', 1289800, '鎻愪氦鎻愮幇鐢宠', '2026-05-24 09:00:00');

INSERT IGNORE INTO withdraw_requests
(id, user_id, amount_minor, status, wx_batch_id, fail_reason, idempotency_key, created_at, updated_at) VALUES
(10001, 10003, 100000, 'PENDING', NULL, NULL, 'wd-10003-2026052401', '2026-05-24 09:00:00', '2026-05-24 09:00:00'),
(10002, 10004, 68000, 'FAILED', 'WXBATCH2026052301', '璐︽埛鏍￠獙澶辫触', 'wd-10004-2026052301', '2026-05-23 13:10:00', '2026-05-23 13:30:00');

INSERT IGNORE INTO orders
(id, order_no, user_id, sku_id, order_type, status, amount_minor, deposit_minor, shipping_minor, discount_minor, payable_minor, created_at, updated_at) VALUES
(10003, 'XQ202605180001', 10003, 1, 'RENT', 'COMPLETED', 490000, 100000, 0, 0, 590000, '2026-05-18 10:30:00', '2026-05-19 17:20:00'),
(10004, 'XQ202605210001', 10004, 2, 'RENT', 'FULFILLING', 199900, 50000, 0, 0, 249900, '2026-05-21 09:30:00', '2026-05-22 10:10:00');

INSERT IGNORE INTO order_lines (id, order_id, sku_id, sku_name, quantity, unit_price_minor) VALUES
(10003, 10003, 1, 'G1-u2 浜哄舰鏈哄櫒浜?, 1, 490000),
(10004, 10004, 2, 'Go2 鍥涜冻鏈哄櫒浜?, 1, 199900);

INSERT IGNORE INTO commission_entries
(id, order_id, beneficiary_user_id, source_user_id, level, amount_minor, rate_percent, status, protect_until, settled_at, created_at) VALUES
(10002, 10003, 10004, 10003, 1, 24500, 5, 'PENDING_PROTECT', '2026-05-31 23:59:59', NULL, '2026-05-18 11:00:00'),
(10003, 10004, 10001, 10004, 2, 5997, 3, 'SETTLED', '2026-05-28 23:59:59', '2026-05-29 12:00:00', '2026-05-21 10:00:00');

INSERT IGNORE INTO commission_settlement_batches
(id, batch_no, settled_at, total_entries, total_amount_minor) VALUES
(10001, 'CSB202605310001', '2026-05-31 12:00:00', 2, 15992);

INSERT IGNORE INTO devices
(id, device_no, name, location, area, qr_code, responsible_technician_id, type, model, manufacturer, status, created_at, updated_at) VALUES
(10002, 'GO2-B-10002', 'Go2 宸℃璁惧 B', '娣卞湷鍗楀北宸ュ巶 B1', '娣卞湷鍗楀北', 'DEVICE-10002', 10004, 'ROBOT', 'Go2', '瀹囨爲绉戞妧', 'NORMAL', '2026-05-21 16:00:00', NOW()),
(10003, 'G1-C-10003', 'G1 婕旂ず璁惧 C', '骞垮窞灞曞巺 2 鍙烽', '骞垮窞', 'DEVICE-10003', 10004, 'ROBOT', 'G1-u2', '瀹囨爲绉戞妧', 'NORMAL', '2026-05-22 16:00:00', NOW());

INSERT IGNORE INTO work_orders
(id, order_no, device_id, device_name, reporter_user_id, reporter_name, fault_type, fault_description, priority, status, assigned_technician_id, created_at, updated_at) VALUES
(10002, 'WO202605240002', 10002, 'Go2 宸℃璁惧 B', 10003, '瀹為獙瀹ら檲鑰佸笀', '缃戠粶寮傚父', '宸℃杩囩▼涓仴娴嬭繛鎺ュ伓鍙戜腑鏂€?, 'MEDIUM', 'IN_PROGRESS', 10004, '2026-05-24 11:00:00', '2026-05-24 11:30:00'),
(10003, 'WO202605230001', 10003, 'G1 婕旂ず璁惧 C', 10004, '宸ュ巶杩愮淮鏉庡伐', '鐢垫睜寮傚父', '婕旂ず鍚庣數姹犲仴搴峰害浣庝簬棰勬湡銆?, 'LOW', 'COMPLETED', 10004, '2026-05-23 15:00:00', '2026-05-23 18:00:00');

INSERT IGNORE INTO inspection_templates
(id, name, description, check_items, created_by, created_at, updated_at) VALUES
(10001, '鏈哄櫒浜烘瘡鏃ュ畨鍏ㄥ贰妫€娓呭崟', '绉熻祦鏈哄櫒浜鸿澶囨瘡鏃ュ贰妫€娓呭崟銆?, '["鐢垫睜","鍏宠妭","缃戠粶","澶栧３"]', 1, '2026-05-20 09:00:00', NOW());

INSERT IGNORE INTO inspection_plans
(id, template_id, name, area, frequency, assigned_technician_id, status, next_run_at, created_at, updated_at) VALUES
(10001, 10001, '鍗楀北宸℃鏈哄櫒浜烘瘡鏃ュ贰妫€', '娣卞湷鍗楀北', '姣忔棩', 10004, 'PENDING', '2026-05-25 09:00:00', '2026-05-20 09:00:00', NOW()),
(10002, 10001, '灞曚細璁惧姣忓懆宸℃', '骞垮窞', '姣忓懆', 10004, 'IN_PROGRESS', '2026-05-27 10:00:00', '2026-05-21 09:00:00', NOW());

INSERT IGNORE INTO inspection_tasks
(id, plan_id, template_id, template_name, assigned_technician_id, status, deadline, started_at, completed_at, created_at) VALUES
(10001, 10001, 10001, '鏈哄櫒浜烘瘡鏃ュ畨鍏ㄥ贰妫€娓呭崟', 10004, 'PENDING', '2026-05-25 18:00:00', NULL, NULL, '2026-05-24 08:00:00'),
(10002, 10002, 10001, '鏈哄櫒浜烘瘡鏃ュ畨鍏ㄥ贰妫€娓呭崟', 10004, 'IN_PROGRESS', '2026-05-27 18:00:00', '2026-05-24 10:00:00', NULL, '2026-05-24 08:30:00');

-- 绀惧尯绠＄悊婕旂ず鏁版嵁锛氳瘽棰樸€佹垚鍛樸€佸笘瀛愩€佽瘎璁恒€佸鏍歌褰曚繚鎸佸悓涓€鎵圭敤鎴峰拰鍟嗗搧涓婁笅鏂囥€?INSERT IGNORE INTO circles
(id, name, description, icon_url, member_count, post_count, created_at) VALUES
(10001, '绉戠爺鏁欒偛浜ゆ祦鍦?, '楂樻牎銆佸疄楠屽鍜屽紑鍙戣€呭洿缁曟満鍣ㄤ汉鏁欏銆佺畻娉曢獙璇併€佽绋嬪疄璺典氦娴併€?, '', 186, 7, '2026-05-01 09:00:00'),
(10002, '宸ヤ笟宸℃瀹炶返鍦?, '鍥尯宸℃銆佸伐鍘傝繍缁淬€佽澶囬儴缃插拰鐜板満闂澶嶇洏銆?, '', 124, 5, '2026-05-03 10:00:00'),
(10003, '绉熻祦浣撻獙鍙嶉鍦?, '鐢ㄦ埛绉熻祦浣撻獙銆佷氦浠橀獙鏀躲€佸敭鍚庣淮鎶ゅ拰缁寤鸿銆?, '', 98, 4, '2026-05-06 14:00:00'),
(10004, '寮€鍙戣皟璇曠瓟鐤戝湀', '鎺у埗绋嬪簭銆佷紶鎰熷櫒銆佺綉缁滆繛鎺ュ拰搴旂敤闆嗘垚绛旂枒銆?, '', 76, 3, '2026-05-08 16:00:00');

INSERT IGNORE INTO circle_members
(id, circle_id, user_id, role, joined_at) VALUES
(10001, 10001, 10001, 'OWNER', '2026-05-01 09:10:00'),
(10002, 10001, 10003, 'MEMBER', '2026-05-08 11:30:00'),
(10003, 10002, 10004, 'OWNER', '2026-05-10 14:20:00'),
(10004, 10002, 10003, 'MEMBER', '2026-05-18 10:45:00'),
(10005, 10003, 10001, 'OWNER', '2026-05-12 09:00:00'),
(10006, 10003, 10002, 'MEMBER', '2026-05-12 10:00:00'),
(10007, 10004, 10004, 'OWNER', '2026-05-18 18:20:00'),
(10008, 10004, 10003, 'MEMBER', '2026-05-19 09:30:00');

UPDATE posts SET circle_id = 10001, topic_id = 10001 WHERE id IN (1, 4, 5) AND circle_id IS NULL;
UPDATE posts SET circle_id = 10002, topic_id = 10002 WHERE id IN (2, 3, 6) AND circle_id IS NULL;

INSERT IGNORE INTO posts
(id, user_id, circle_id, topic_id, title, content, status, like_count, comment_count, is_pinned, created_at, updated_at) VALUES
(10001, 10003, 10001, 10001, '楂樻牎璇剧▼鍑嗗鐢?G1 鍋氳繍鍔ㄦ帶鍒跺疄楠?, '鏈懆浼氭妸 G1-u2 鎺ュ叆璇剧▼瀹為獙锛屼富瑕侀獙璇佹鎬佸垏鎹€佸Э鎬佷繚鎸佸拰鎬ュ仠淇濇姢锛屾杩庢湁鍚岀被璇剧▼缁忛獙鐨勮€佸笀琛ュ厖楠屾敹娓呭崟銆?, 'APPROVED', 96, 18, 1, '2026-05-24 10:20:00', '2026-05-24 10:20:00'),
(10002, 10004, 10002, 10002, '鍗楀北宸ュ巶澶滈棿宸℃缃戠粶鎶栧姩澶嶇洏', 'Go2 鍦?B1 鍖哄煙澶滈棿宸℃鏃跺嚭鐜颁袱娆￠仴娴嬪欢杩燂紝鍒濇鍒ゆ柇涓庡急瑕嗙洊鍜屾极娓稿垏鎹㈡湁鍏筹紝宸茬粡鍑嗗琛ョ偣娴嬭瘯銆?, 'MANUAL_REVIEW', 42, 9, 0, '2026-05-24 11:10:00', '2026-05-24 11:20:00'),
(10003, 10001, 10003, 10003, '绉熻祦浜や粯鏃跺缓璁鍔犵數姹犲仴搴锋姤鍛?, '鏈€杩戦獙鏀惰澶囨椂鐢ㄦ埛寰堝叧蹇冪画鑸拰鐢垫睜鍋ュ悍搴︼紝寤鸿浜や粯鍗曢噷澧炲姞鍋ュ悍鎶ュ憡鍜屾渶杩戜竴娆″贰妫€璁板綍銆?, 'APPROVED', 73, 12, 0, '2026-05-23 17:40:00', '2026-05-23 17:40:00'),
(10004, 10002, 10004, 10004, '鏈烘鑷傛湯绔伐鍏锋爣瀹氬弬鏁版眰鍔?, '杩滃緛 A1 鏇存崲澶瑰叿鍚庢湯绔亸宸瘮杈冩槑鏄撅紝鎯崇‘璁ゆ爣瀹氭祦绋嬮噷鏄惁闇€瑕侀噸鏂板啓鍏ュ伐鍏峰潗鏍囥€?, 'AUDITING', 15, 4, 0, '2026-05-23 13:30:00', '2026-05-23 13:30:00'),
(10005, 10005, 10003, 10003, '閲嶅鍙戝竷鐨勬棤鏁堜綋楠屽笘', '杩欐潯鍐呭鐢ㄤ簬鍚庡彴鎾ら攢娴佺▼婕旂ず锛屼笉鍦ㄥ皬绋嬪簭淇℃伅娴佸睍绀恒€?, 'REVOKED', 3, 1, 0, '2026-05-22 20:00:00', '2026-05-23 09:00:00'),
(10006, 10003, 10002, 10002, '宸℃璁惧浠庝粨搴撳埌鐜板満鐨勪氦浠樻竻鍗?, '寤鸿鎶婁簩缁寸爜缁戝畾銆佽繙绋嬭瘖鏂€佸浠舵鏌ュ拰璐ｄ换鎶€甯堢‘璁ゆ斁鍦ㄥ悓涓€寮犱氦浠樻竻鍗曢噷锛屽噺灏戣法閮ㄩ棬娌熼€氭垚鏈€?, 'APPROVED', 64, 11, 0, '2026-05-22 09:30:00', '2026-05-22 09:30:00');

INSERT IGNORE INTO comments
(id, post_id, user_id, parent_id, content, status, created_at) VALUES
(10001, 10001, 10004, NULL, '寤鸿鎶婃€ュ仠鍜屼汉宸ユ帴绠℃斁鍒扮涓€鑺傝锛屽鐢熷厛鐔熸倝瀹夊叏杈圭晫銆?, 'VISIBLE', '2026-05-24 10:35:00'),
(10002, 10001, 10001, 10001, '杩欎釜寤鸿寰堝ソ锛屾垜浼氭斁杩涜鍓嶈鏄庡拰楠屾敹琛ㄣ€?, 'VISIBLE', '2026-05-24 10:50:00'),
(10003, 10002, 10003, NULL, '鎴戜滑鍦ㄥ洯鍖轰篃閬囧埌杩囩被浼兼儏鍐碉紝琛ョ偣鍚庡欢杩熸槑鏄句笅闄嶃€?, 'VISIBLE', '2026-05-24 11:40:00'),
(10004, 10003, 10002, NULL, '鏀寔澧炲姞鎶ュ憡锛屽敭鍚庢矡閫氫細鏇存竻妤氥€?, 'VISIBLE', '2026-05-23 18:05:00'),
(10005, 10006, 10004, NULL, '娓呭崟閲屽彲浠ュ啀鍔犱竴椤瑰浠界綉缁滄祴璇曘€?, 'VISIBLE', '2026-05-22 10:20:00');

INSERT IGNORE INTO post_audit_logs
(id, post_id, audit_source, result, raw_response, created_at) VALUES
(10001, 10001, '绯荤粺瀹℃牳', '閫氳繃', '鍐呭姝ｅ父锛岃嚜鍔ㄥ彂甯冦€?, '2026-05-24 10:21:00'),
(10002, 10002, '绯荤粺瀹℃牳', '浜哄伐澶嶆牳', '鍖呭惈鐜板満鏁呴殰澶嶇洏锛岀瓑寰呰繍钀ョ‘璁ゃ€?, '2026-05-24 11:21:00'),
(10003, 10005, '浜哄伐澶勭悊', '宸叉挙閿€', '閲嶅鍐呭锛屽悗鍙版挙閿€銆?, '2026-05-23 09:00:00'),
(10004, 10006, '绯荤粺瀹℃牳', '閫氳繃', '鍐呭姝ｅ父锛岃嚜鍔ㄥ彂甯冦€?, '2026-05-22 09:31:00');

