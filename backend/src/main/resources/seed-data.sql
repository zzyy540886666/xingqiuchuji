-- Development seed data. The mini program never reads this file directly:
-- every screen loads these records through authenticated API/database queries.
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- Existing development databases are upgraded in place. Duplicate-column
-- errors on subsequent boots are ignored by spring.sql.init.continue-on-error.
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
    price_label VARCHAR(64) NOT NULL DEFAULT '免费',
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
(1, '宇树科技', '/static/images/brand-unitree.png', 1, NOW(), NOW()),
(2, '傅利叶', '/static/images/brand-fourier.png', 2, NOW(), NOW());

INSERT IGNORE INTO scenes (id, name, description, image_url, sort_order) VALUES
(1, '科研教育', '高校实验室、科研机构的教学与研究场景', '/static/images/scene-education.png', 1),
(2, '工业巡检', '工厂与园区的自动化巡检场景', '/static/images/scene-inspection.png', 2);

INSERT IGNORE INTO skus (id, name, type, brand_id, description, specs_json, status, stock, created_at, updated_at) VALUES
(1, 'G1-u2 人形机器人', 'RENT', 1, '支持全身运动控制与智能交互', '{"height":"1270mm","weight":"35kg"}', 'ONLINE', 16, NOW(), NOW()),
(2, 'Go2 四足机器人', 'RENT', 1, '适用于工业巡检与教育场景', '{"weight":"15kg","battery":"2-4h"}', 'ONLINE', 20, NOW(), NOW());

INSERT IGNORE INTO sku_media (id, sku_id, url, type, sort_order) VALUES
(1, 1, '/static/images/robot-humanoid.png', 'IMAGE', 0),
(2, 2, '/static/images/robot-dog.png', 'IMAGE', 0);

INSERT IGNORE INTO sku_prices (id, sku_id, price_type, price_minor, min_duration, max_duration, daily_rate_minor) VALUES
(1, 1, 'DAILY_RENT', 490000, 1, 365, 490000),
(2, 2, 'DAILY_RENT', 199900, 1, 365, 199900);

UPDATE skus SET
  subtitle = '人形机器人租赁与购买一体化方案',
  original_price_minor = 1999900,
  adapted_scenes_text = '工业巡检、户外安防、应急救援、科研教育',
  stock_status_text = '现货充足',
  delivery_text = '全国可租可发'
WHERE id = 1;

UPDATE skus SET
  subtitle = '四足机器人巡检与教育场景方案',
  original_price_minor = 899900,
  adapted_scenes_text = '工业巡检、科研教育、商业服务',
  stock_status_text = '现货充足',
  delivery_text = '全国可租可发'
WHERE id = 2;

INSERT IGNORE INTO sku_tags (id, sku_id, name, sort_order) VALUES
(1, 1, '可租赁', 1), (2, 1, '可购买', 2), (3, 1, '支持视频演示', 3),
(4, 2, '可租赁', 1), (5, 2, '工业巡检', 2);

INSERT IGNORE INTO sku_services (id, sku_id, name, price_label, sort_order) VALUES
(1, 1, '信息登记与培训', '免费', 1),
(2, 1, '技术支持与保障', '免费', 2),
(3, 1, '场景应用软件配置', '免费', 3),
(4, 2, '信息登记与培训', '免费', 1),
(5, 2, '远程技术支持', '免费', 2);

INSERT IGNORE INTO sku_detail_sections (id, sku_id, title, content, sort_order) VALUES
(1, 1, '客服服务功能详细介绍与服务条款', '包含开箱培训、远程技术支持、使用注意事项与售后范围。', 1),
(2, 1, '产品包装清单及配送说明', '包装包含机器人本体、电池、充电器、控制器与基础工具，全国可租可发。', 2),
(3, 1, '销售/云端相关技术文档及购买须知资格', '购买前需确认应用场景、授权范围、软件适配与云端服务开通条件。', 3),
(4, 2, '四足机器人租赁服务说明', '包含巡检路线配置、遥控培训、返场检测和基础维护服务。', 1);

INSERT IGNORE INTO catalog_filter_groups (id, code, title, filter_field, sort_order, enabled) VALUES
(1, 'brand', '品牌', 'BRAND_ID', 1, 1),
(2, 'price', '价格区间', 'PRICE_RANGE', 2, 1),
(3, 'model', '适配机型', 'MODEL_ID', 3, 1),
(4, 'scene', '热门场景', 'KEYWORD', 4, 1);

INSERT IGNORE INTO catalog_filter_options (id, group_id, label, value, min_price_minor, max_price_minor, sort_order, enabled) VALUES
(1, 1, '全部品牌', '', NULL, NULL, 0, 1),
(2, 1, '宇树科技', '1', NULL, NULL, 1, 1),
(3, 1, '傅利叶', '2', NULL, NULL, 2, 1),
(4, 2, '全部价格', '', NULL, NULL, 0, 1),
(5, 2, '0-1000', '', 0, 100000, 1, 1),
(6, 2, '1000-5000', '', 100000, 500000, 2, 1),
(7, 2, '5000-10000', '', 500000, 1000000, 3, 1),
(8, 2, '10000以上', '', 1000000, NULL, 4, 1),
(9, 3, '全部机型', '', NULL, NULL, 0, 1),
(10, 3, 'G1系列', '1', NULL, NULL, 1, 1),
(11, 3, 'Go2系列', '2', NULL, NULL, 2, 1),
(12, 4, '全部场景', '', NULL, NULL, 0, 1),
(13, 4, '教育科研', '科研教育', NULL, NULL, 1, 1),
(14, 4, '工业巡检', '工业巡检', NULL, NULL, 2, 1),
(15, 4, '商业服务', '商业服务', NULL, NULL, 3, 1);

INSERT IGNORE INTO marketing_activities
(id, title, subtitle, tag, cover_url, video_url, description, content_json, link_url, start_at, end_at, sort_order, status, created_at, updated_at)
VALUES
(1, '春季机器人租赁活动', '租赁低至 5 折，支持企业短租与科研试用', '春季焕新季', '/static/images/hero-robot.jpg', '', '面向科研教育、工业巡检和商业展演场景的限时活动。', '[{"title":"活动权益","content":"活动期内提交租赁订单可获得方案顾问、设备调试和基础培训。"},{"title":"适用商品","content":"适用于平台在线可租赁机器人，具体库存以后端 SKU 状态为准。"},{"title":"活动规则","content":"活动价格、押金减免和服务权益以订单确认页与合同为准。"}]', '/pages/category/index?type=RENT', '2026-05-01 00:00:00', '2026-12-31 23:59:59', 1, 'PUBLISHED', NOW(), NOW());

INSERT IGNORE INTO scene_sku_rel (id, scene_id, sku_id) VALUES
(1, 1, 1), (2, 2, 2);

INSERT IGNORE INTO app_configs (id, config_key, value_json, version, description) VALUES
(100, 'banners', '[{"id":1,"imageUrl":"/static/images/hero-robot.jpg","linkUrl":"/pages/category/index","title":"智能设备租赁"}]', 1, '首页轮播图配置'),
(101, 'hot_keywords', '["四足机器人","人形机器人","工业巡检"]', 1, '搜索热词'),
(102, 'scene_tags', '{"1":["可租","科研"],"2":["可租","巡检"]}', 1, '场景标签配置');

-- Relational user-center fixture. To inspect it through a real WeChat login in
-- development, associate the authenticated openid with this fixture explicitly.
INSERT IGNORE INTO users (id, openid, nickname, avatar_url, role, created_at, updated_at) VALUES
(10001, 'seed-openid-profile-owner', '开发验收用户', '/static/images/avatar.png', 'USER', '2026-05-01 09:00:00', NOW()),
(10002, 'seed-openid-invitee', '受邀成员', '/static/images/avatar.png', 'USER', '2026-05-05 09:00:00', NOW());

INSERT IGNORE INTO wallets (id, user_id, balance_minor, frozen_minor, created_at, updated_at) VALUES
(10001, 10001, 568000, 12000, '2026-05-01 09:00:00', NOW()),
(10002, 10002, 0, 0, '2026-05-05 09:00:00', NOW());

INSERT IGNORE INTO wallet_ledger
(id, user_id, type, amount_minor, ref_type, ref_id, balance_after_minor, description, created_at) VALUES
(10001, 10001, 'CREDIT', 500000, 'ASSET_REVENUE', '10001', 500000, '托管收益结算', '2026-05-15 12:00:00'),
(10002, 10001, 'CREDIT', 68000, 'COMMISSION', '10001', 568000, '分销佣金结算', '2026-05-20 12:00:00');

INSERT IGNORE INTO user_membership
(id, user_id, level, is_native, lifetime_spend_minor, planet_card_expires_at, created_at, updated_at) VALUES
(10001, 10001, 1, 1, 32000, '2027-05-01 23:59:59', '2026-05-01 09:00:00', NOW());

INSERT IGNORE INTO membership_benefit_grants
(id, user_id, benefit_type, total_count, used_count, expires_at) VALUES
(10001, 10001, 'FREE_DEPOSIT', 2, 0, '2027-05-01 23:59:59'),
(10002, 10001, 'MAINTENANCE', 1, 0, '2027-05-01 23:59:59');

INSERT IGNORE INTO planet_card_skus
(id, name, duration_days, price_minor, benefit_level, stock, status) VALUES
(1, '月度星球卡', 30, 9900, 1, 100, 'ON_SALE'),
(2, '年度星球卡', 365, 89900, 2, 100, 'ON_SALE');

INSERT IGNORE INTO orders
(id, order_no, user_id, sku_id, order_type, status, amount_minor, deposit_minor, shipping_minor, discount_minor, payable_minor, created_at, updated_at) VALUES
(10001, 'XQ202605200001', 10001, 1, 'RENT', 'COMPLETED', 490000, 0, 0, 0, 490000, '2026-05-20 10:30:00', '2026-05-20 16:00:00'),
(10002, 'XQ202605230001', 10002, 2, 'RENT', 'COMPLETED', 199900, 0, 0, 0, 199900, '2026-05-23 10:30:00', '2026-05-23 16:00:00');

INSERT IGNORE INTO order_lines (id, order_id, sku_id, sku_name, quantity, unit_price_minor) VALUES
(10001, 10001, 1, 'G1-u2 人形机器人', 1, 490000),
(10002, 10002, 2, 'Go2 四足机器人', 1, 199900);

INSERT IGNORE INTO order_events (id, order_id, from_status, to_status, operator, reason, created_at) VALUES
(10001, 10001, NULL, 'PENDING_PAY', 'USER:10001', '创建订单', '2026-05-20 10:30:00'),
(10002, 10001, 'FULFILLING', 'COMPLETED', 'SYSTEM', '履约完成', '2026-05-20 16:00:00');

INSERT IGNORE INTO user_assets
(id, user_id, name, type, model_info, status, purchase_order_id, acquired_at, created_at) VALUES
(10001, 10001, 'G1-u2 设备 A', 'ROBOT', 'G1-u2', 'TRUSTEED', '10001', '2026-05-20 16:00:00', '2026-05-20 16:00:00'),
(10002, 10001, 'Go2 设备 B', 'ROBOT', 'Go2', 'IDLE', NULL, '2026-05-21 16:00:00', '2026-05-21 16:00:00');

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
(10001, 'G1-A-10001', 'G1-u2 设备 A', '深圳南山科技园 A 栋', '深圳南山', 'DEVICE-10001', 'ROBOT', 'G1-u2', '宇树科技', 'FAULT', '2026-05-20 16:00:00', NOW());

INSERT IGNORE INTO work_orders
(id, order_no, device_id, device_name, reporter_user_id, reporter_name, fault_type, fault_description, priority, status, created_at, updated_at) VALUES
(10001, 'WO202605240001', 10001, 'G1-u2 设备 A', 10001, '开发验收用户', '运动异常', '左腿关节运行时出现抖动', 'HIGH', 'NEW', '2026-05-24 09:30:00', '2026-05-24 09:30:00');

INSERT IGNORE INTO work_order_logs
(id, work_order_id, action, operator_id, operator_name, from_status, to_status, remark, created_at) VALUES
(10001, 10001, 'CREATED', 10001, '开发验收用户', NULL, 'NEW', '用户提交报修', '2026-05-24 09:30:00');

INSERT IGNORE INTO notifications
(id, user_id, type, title, content, ref_id, is_read, created_at) VALUES
(10001, 10001, 'ORDER', '订单已完成', '您的租赁订单 XQ202605200001 已完成。', '10001', 0, '2026-05-20 16:00:00'),
(10002, 10001, 'ASSET_REVENUE', '托管收益到账', '托管收益已进入账户余额，请查看流水。', '10001', 0, '2026-05-15 12:00:00');

INSERT IGNORE INTO posts (id, user_id, circle_id, title, content, status, like_count, comment_count, created_at, updated_at) VALUES
(1, 10001, NULL, 'Go2 Pro 新性能提升', 'Go2 Pro 新性能提升！感谢大家对我们的支持，经过团队的努力，我们对 Go2 Pro 进行了全面升级，性能提升 30%，续航增加 1 小时！', 'APPROVED', 188, 45, '2026-05-24 09:00:00', '2026-05-24 09:00:00'),
(2, 10002, NULL, '四足机器人舞蹈编排', '分享一下我们团队用 Go2 四足机器人进行的舞蹈编排！整个项目历时一个月的时间调整，从动作设计到程序调试每一步都充满了挑战和乐趣。', 'APPROVED', 256, 78, '2026-05-24 07:00:00', '2026-05-24 07:00:00'),
(3, 10001, NULL, '机器狗复杂地形稳定性', '想问下，机器狗在复杂地形下的稳定性怎么样？我们项目需要在山地环境使用，有经验的朋友可以分享一下体验吗？', 'APPROVED', 89, 42, '2026-05-24 04:00:00', '2026-05-24 04:00:00'),
(4, 10002, NULL, 'GR-1 开箱体验', '刚收到 GR-1 通用人形机器人，开箱体验非常棒！做工精细，关节灵活度超出预期。准备用来做科研项目的运动控制实验。', 'APPROVED', 312, 56, '2026-05-23 15:00:00', '2026-05-23 15:00:00'),
(5, 10001, NULL, 'Walker S 自主导航 Demo', '我们学校实验室用 Walker S 做了一个自主导航的 demo，效果很不错。有兴趣的同学可以来交流一下技术方案。', 'APPROVED', 145, 33, '2026-05-23 10:00:00', '2026-05-23 10:00:00'),
(6, 10002, NULL, '远征 A1 精度测试报告', '远征 A1 机械臂的精度测试报告出来了，重复定位精度达到 0.02mm，完全满足我们精密装配的需求。', 'APPROVED', 201, 67, '2026-05-22 14:00:00', '2026-05-22 14:00:00');

INSERT IGNORE INTO post_media (id, post_id, url, type, sort_order) VALUES
(1, 1, '/static/images/post-1.png', 'IMAGE', 0),
(2, 1, '/static/images/post-2.png', 'IMAGE', 1),
(3, 2, '/static/images/post-2.png', 'IMAGE', 0),
(4, 3, '/static/images/post-3.png', 'IMAGE', 0),
(5, 4, '/static/images/robot-humanoid.png', 'IMAGE', 0),
(6, 5, '/static/images/robot-dog.png', 'IMAGE', 0);

-- 后台管理网站演示数据：用户、订单、钱包、分销、运维记录按编号保持一致。
INSERT IGNORE INTO users (id, openid, nickname, avatar_url, role, created_at, updated_at) VALUES
(10003, 'seed-openid-lab-manager', '实验室陈老师', '/static/images/avatar.png', 'USER', '2026-05-08 11:20:00', NOW()),
(10004, 'seed-openid-factory-ops', '工厂运维李工', '/static/images/avatar.png', 'TECHNICIAN', '2026-05-10 14:15:00', NOW()),
(10005, 'seed-openid-frozen-demo', '冻结演示用户', '/static/images/avatar.png', 'DISABLED', '2026-05-12 16:40:00', NOW());

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
(10003, 10003, 'CREDIT', 980000, 'RECHARGE', 'RC202605080001', 980000, '企业预充值', '2026-05-08 12:00:00'),
(10004, 10003, 'DEBIT', 490000, 'CONSUME', '10003', 490000, '人形机器人租赁订单支付', '2026-05-18 10:30:00'),
(10005, 10003, 'CREDIT', 899800, 'REFUND', 'RF202605190001', 1389800, '设备验收后押金退回', '2026-05-19 17:20:00'),
(10006, 10004, 'CREDIT', 286000, 'COMMISSION', '10002', 286000, '维修员邀请佣金结算', '2026-05-23 18:00:00'),
(10007, 10003, 'DEBIT', 100000, 'WITHDRAW', '10001', 1289800, '提交提现申请', '2026-05-24 09:00:00');

INSERT IGNORE INTO withdraw_requests
(id, user_id, amount_minor, status, wx_batch_id, fail_reason, idempotency_key, created_at, updated_at) VALUES
(10001, 10003, 100000, 'PENDING', NULL, NULL, 'wd-10003-2026052401', '2026-05-24 09:00:00', '2026-05-24 09:00:00'),
(10002, 10004, 68000, 'FAILED', 'WXBATCH2026052301', '账户校验失败', 'wd-10004-2026052301', '2026-05-23 13:10:00', '2026-05-23 13:30:00');

INSERT IGNORE INTO orders
(id, order_no, user_id, sku_id, order_type, status, amount_minor, deposit_minor, shipping_minor, discount_minor, payable_minor, created_at, updated_at) VALUES
(10003, 'XQ202605180001', 10003, 1, 'RENT', 'COMPLETED', 490000, 100000, 0, 0, 590000, '2026-05-18 10:30:00', '2026-05-19 17:20:00'),
(10004, 'XQ202605210001', 10004, 2, 'RENT', 'FULFILLING', 199900, 50000, 0, 0, 249900, '2026-05-21 09:30:00', '2026-05-22 10:10:00');

INSERT IGNORE INTO order_lines (id, order_id, sku_id, sku_name, quantity, unit_price_minor) VALUES
(10003, 10003, 1, 'G1-u2 人形机器人', 1, 490000),
(10004, 10004, 2, 'Go2 四足机器人', 1, 199900);

INSERT IGNORE INTO commission_entries
(id, order_id, beneficiary_user_id, source_user_id, level, amount_minor, rate_percent, status, protect_until, settled_at, created_at) VALUES
(10002, 10003, 10004, 10003, 1, 24500, 5, 'PENDING_PROTECT', '2026-05-31 23:59:59', NULL, '2026-05-18 11:00:00'),
(10003, 10004, 10001, 10004, 2, 5997, 3, 'SETTLED', '2026-05-28 23:59:59', '2026-05-29 12:00:00', '2026-05-21 10:00:00');

INSERT IGNORE INTO commission_settlement_batches
(id, batch_no, settled_at, total_entries, total_amount_minor) VALUES
(10001, 'CSB202605310001', '2026-05-31 12:00:00', 2, 15992);

INSERT IGNORE INTO devices
(id, device_no, name, location, area, qr_code, responsible_technician_id, type, model, manufacturer, status, created_at, updated_at) VALUES
(10002, 'GO2-B-10002', 'Go2 巡检设备 B', '深圳南山工厂 B1', '深圳南山', 'DEVICE-10002', 10004, 'ROBOT', 'Go2', '宇树科技', 'NORMAL', '2026-05-21 16:00:00', NOW()),
(10003, 'G1-C-10003', 'G1 演示设备 C', '广州展厅 2 号馆', '广州', 'DEVICE-10003', 10004, 'ROBOT', 'G1-u2', '宇树科技', 'NORMAL', '2026-05-22 16:00:00', NOW());

INSERT IGNORE INTO work_orders
(id, order_no, device_id, device_name, reporter_user_id, reporter_name, fault_type, fault_description, priority, status, assigned_technician_id, created_at, updated_at) VALUES
(10002, 'WO202605240002', 10002, 'Go2 巡检设备 B', 10003, '实验室陈老师', '网络异常', '巡检过程中遥测连接偶发中断。', 'MEDIUM', 'IN_PROGRESS', 10004, '2026-05-24 11:00:00', '2026-05-24 11:30:00'),
(10003, 'WO202605230001', 10003, 'G1 演示设备 C', 10004, '工厂运维李工', '电池异常', '演示后电池健康度低于预期。', 'LOW', 'COMPLETED', 10004, '2026-05-23 15:00:00', '2026-05-23 18:00:00');

INSERT IGNORE INTO inspection_templates
(id, name, description, check_items, created_by, created_at, updated_at) VALUES
(10001, '机器人每日安全巡检清单', '租赁机器人设备每日巡检清单。', '["电池","关节","网络","外壳"]', 1, '2026-05-20 09:00:00', NOW());

INSERT IGNORE INTO inspection_plans
(id, template_id, name, area, frequency, assigned_technician_id, status, next_run_at, created_at, updated_at) VALUES
(10001, 10001, '南山巡检机器人每日巡检', '深圳南山', '每日', 10004, 'PENDING', '2026-05-25 09:00:00', '2026-05-20 09:00:00', NOW()),
(10002, 10001, '展会设备每周巡检', '广州', '每周', 10004, 'IN_PROGRESS', '2026-05-27 10:00:00', '2026-05-21 09:00:00', NOW());

INSERT IGNORE INTO inspection_tasks
(id, plan_id, template_id, template_name, assigned_technician_id, status, deadline, started_at, completed_at, created_at) VALUES
(10001, 10001, 10001, '机器人每日安全巡检清单', 10004, 'PENDING', '2026-05-25 18:00:00', NULL, NULL, '2026-05-24 08:00:00'),
(10002, 10002, 10001, '机器人每日安全巡检清单', 10004, 'IN_PROGRESS', '2026-05-27 18:00:00', '2026-05-24 10:00:00', NULL, '2026-05-24 08:30:00');

-- 社区管理演示数据：话题、成员、帖子、评论、审核记录保持同一批用户和商品上下文。
INSERT IGNORE INTO circles
(id, name, description, icon_url, member_count, post_count, created_at) VALUES
(10001, '科研教育交流圈', '高校、实验室和开发者围绕机器人教学、算法验证、课程实践交流。', '/static/images/scene-education.png', 186, 7, '2026-05-01 09:00:00'),
(10002, '工业巡检实践圈', '园区巡检、工厂运维、设备部署和现场问题复盘。', '/static/images/scene-inspection.png', 124, 5, '2026-05-03 10:00:00'),
(10003, '租赁体验反馈圈', '用户租赁体验、交付验收、售后维护和续租建议。', '/static/images/robot-dog.png', 98, 4, '2026-05-06 14:00:00'),
(10004, '开发调试答疑圈', '控制程序、传感器、网络连接和应用集成答疑。', '/static/images/robot-humanoid.png', 76, 3, '2026-05-08 16:00:00');

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
(10001, 10003, 10001, 10001, '高校课程准备用 G1 做运动控制实验', '本周会把 G1-u2 接入课程实验，主要验证步态切换、姿态保持和急停保护，欢迎有同类课程经验的老师补充验收清单。', 'APPROVED', 96, 18, 1, '2026-05-24 10:20:00', '2026-05-24 10:20:00'),
(10002, 10004, 10002, 10002, '南山工厂夜间巡检网络抖动复盘', 'Go2 在 B1 区域夜间巡检时出现两次遥测延迟，初步判断与弱覆盖和漫游切换有关，已经准备补点测试。', 'MANUAL_REVIEW', 42, 9, 0, '2026-05-24 11:10:00', '2026-05-24 11:20:00'),
(10003, 10001, 10003, 10003, '租赁交付时建议增加电池健康报告', '最近验收设备时用户很关心续航和电池健康度，建议交付单里增加健康报告和最近一次巡检记录。', 'APPROVED', 73, 12, 0, '2026-05-23 17:40:00', '2026-05-23 17:40:00'),
(10004, 10002, 10004, 10004, '机械臂末端工具标定参数求助', '远征 A1 更换夹具后末端偏差比较明显，想确认标定流程里是否需要重新写入工具坐标。', 'AUDITING', 15, 4, 0, '2026-05-23 13:30:00', '2026-05-23 13:30:00'),
(10005, 10005, 10003, 10003, '重复发布的无效体验帖', '这条内容用于后台撤销流程演示，不在小程序信息流展示。', 'REVOKED', 3, 1, 0, '2026-05-22 20:00:00', '2026-05-23 09:00:00'),
(10006, 10003, 10002, 10002, '巡检设备从仓库到现场的交付清单', '建议把二维码绑定、远程诊断、备件检查和责任技师确认放在同一张交付清单里，减少跨部门沟通成本。', 'APPROVED', 64, 11, 0, '2026-05-22 09:30:00', '2026-05-22 09:30:00');

INSERT IGNORE INTO comments
(id, post_id, user_id, parent_id, content, status, created_at) VALUES
(10001, 10001, 10004, NULL, '建议把急停和人工接管放到第一节课，学生先熟悉安全边界。', 'VISIBLE', '2026-05-24 10:35:00'),
(10002, 10001, 10001, 10001, '这个建议很好，我会放进课前说明和验收表。', 'VISIBLE', '2026-05-24 10:50:00'),
(10003, 10002, 10003, NULL, '我们在园区也遇到过类似情况，补点后延迟明显下降。', 'VISIBLE', '2026-05-24 11:40:00'),
(10004, 10003, 10002, NULL, '支持增加报告，售后沟通会更清楚。', 'VISIBLE', '2026-05-23 18:05:00'),
(10005, 10006, 10004, NULL, '清单里可以再加一项备份网络测试。', 'VISIBLE', '2026-05-22 10:20:00');

INSERT IGNORE INTO post_audit_logs
(id, post_id, audit_source, result, raw_response, created_at) VALUES
(10001, 10001, '系统审核', '通过', '内容正常，自动发布。', '2026-05-24 10:21:00'),
(10002, 10002, '系统审核', '人工复核', '包含现场故障复盘，等待运营确认。', '2026-05-24 11:21:00'),
(10003, 10005, '人工处理', '已撤销', '重复内容，后台撤销。', '2026-05-23 09:00:00'),
(10004, 10006, '系统审核', '通过', '内容正常，自动发布。', '2026-05-22 09:31:00');
