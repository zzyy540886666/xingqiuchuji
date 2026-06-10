-- Unitree-focused, relational demo data for user 2063223234097950700.
-- Target schema: xingqiu_dev (MySQL 8.0)
-- Amounts are stored in integer minor units (fen).
-- The script is idempotent for the fixed IDs below.

USE xingqiu_dev;

START TRANSACTION;

SET @target_user_id = 2063223234097950700;
SET @invitee_a_id = 2063223234097961001;
SET @invitee_b_id = 2063223234097961002;
SET @invitee_c_id = 2063223234097961003;

-- ---------------------------------------------------------------------------
-- 1. Users required by ownership and distribution foreign keys
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO users
    (id, openid, unionid, nickname, avatar_url, role, created_at, updated_at)
VALUES
    (@target_user_id, 'demo-unitree-2063223234097950700', NULL, '瀹囨爲鏈哄櫒浜轰綋楠屽畼',
     '/static/images/default-avatar.svg', 'USER', '2026-01-08 09:30:00', '2026-06-07 10:00:00'),
    (@invitee_a_id, 'demo-unitree-team-a-2063223234097961001', NULL, '鏉窞鍥涜冻鏈哄櫒浜哄伐浣滃',
     '/static/images/default-avatar.svg', 'USER', '2026-03-12 10:20:00', '2026-06-07 10:00:00'),
    (@invitee_b_id, 'demo-unitree-team-b-2063223234097961002', NULL, '鏅哄贰绉戞妧',
     '/static/images/default-avatar.svg', 'USER', '2026-04-03 14:10:00', '2026-06-07 10:00:00'),
    (@invitee_c_id, 'demo-unitree-team-c-2063223234097961003', NULL, '鏈哄櫒鐙楄缁冭惀',
     '/static/images/default-avatar.svg', 'USER', '2026-05-16 11:40:00', '2026-06-07 10:00:00');

-- ---------------------------------------------------------------------------
-- 2. Target user's Unitree orders, lines, status history and payments
-- ---------------------------------------------------------------------------
INSERT INTO orders
    (id, order_no, user_id, sku_id, order_type, status, amount_minor, deposit_minor,
     shipping_minor, discount_minor, payable_minor, rent_start_date, rent_end_date,
     address_json, idempotency_key, created_at, updated_at)
VALUES
    (2063223234097970001, 'XQ202601150001UNITREE', @target_user_id, 9001, 'BUY', 'COMPLETED',
     599000, 0, 0, 0, 599000, NULL, NULL,
     '{"receiverName":"鏋楀厛鐢?,"phone":"138****5208","province":"娴欐睙鐪?,"city":"鏉窞甯?,"district":"浣欐澀鍖?,"detail":"鏈潵绉戞妧鍩庢満鍣ㄤ汉鍒涙柊涓績"}',
     'seed-2063223234097950700-order-1', '2026-01-15 10:18:00', '2026-01-23 16:30:00'),
    (2063223234097970002, 'XQ202603180002UNITREE', @target_user_id, 9002, 'BUY', 'COMPLETED',
     1599900, 0, 0, 100000, 1499900, NULL, NULL,
     '{"receiverName":"鏋楀厛鐢?,"phone":"138****5208","province":"娴欐睙鐪?,"city":"鏉窞甯?,"district":"浣欐澀鍖?,"detail":"鏈潵绉戞妧鍩庢満鍣ㄤ汉鍒涙柊涓績"}',
     'seed-2063223234097950700-order-2', '2026-03-18 09:42:00', '2026-04-02 14:20:00'),
    (2063223234097970003, 'XQ202605080003UNITREE', @target_user_id, 9003, 'BUY', 'FULFILLING',
     999900, 0, 0, 0, 999900, NULL, NULL,
     '{"receiverName":"鏋楀厛鐢?,"phone":"138****5208","province":"娴欐睙鐪?,"city":"鏉窞甯?,"district":"浣欐澀鍖?,"detail":"鏈潵绉戞妧鍩庢満鍣ㄤ汉鍒涙柊涓績"}',
     'seed-2063223234097950700-order-3', '2026-05-08 15:26:00', '2026-06-05 11:00:00'),
    (2063223234097970004, 'XQ202606060004UNITREE', @target_user_id, 9011, 'SOFTWARE', 'PENDING_PAY',
     599900, 0, 0, 0, 599900, NULL, NULL, NULL,
     'seed-2063223234097950700-order-4', '2026-06-06 20:16:00', '2026-06-06 20:16:00'),
    (2063223234097970005, 'XQ202604120005UNITREE', @target_user_id, 2, 'RENT', 'CANCELLED',
     1399300, 200000, 0, 0, 1599300, '2026-04-20', '2026-04-27', NULL,
     'seed-2063223234097950700-order-5', '2026-04-12 18:05:00', '2026-04-13 09:12:00')
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    amount_minor = VALUES(amount_minor),
    deposit_minor = VALUES(deposit_minor),
    discount_minor = VALUES(discount_minor),
    payable_minor = VALUES(payable_minor),
    updated_at = VALUES(updated_at);

INSERT INTO order_lines
    (id, order_id, sku_id, sku_name, quantity, unit_price_minor)
VALUES
    (2063223234097971001, 2063223234097970001, 9001, 'Unitree Go2 Edu 鍥涜冻鏈哄櫒浜洪噰璐瑁?, 1, 599000),
    (2063223234097971002, 2063223234097970002, 9002, 'Unitree B2 宸ヤ笟鍥涜冻鏈哄櫒浜洪噰璐瑁?, 1, 1599900),
    (2063223234097971003, 2063223234097970003, 9003, 'Unitree G1 浜哄舰鏈哄櫒浜洪噰璐瑁?, 1, 999900),
    (2063223234097971004, 2063223234097970004, 9011, 'Unitree CloudFleet 浜戠璋冨害绯荤粺', 1, 599900),
    (2063223234097971005, 2063223234097970005, 2, 'Go2 鍥涜冻鏈哄櫒浜?, 7, 199900)
ON DUPLICATE KEY UPDATE
    sku_name = VALUES(sku_name),
    quantity = VALUES(quantity),
    unit_price_minor = VALUES(unit_price_minor);

INSERT INTO order_events
    (id, order_id, from_status, to_status, operator, reason, created_at)
VALUES
    (2063223234097972001, 2063223234097970001, NULL, 'PENDING_PAY', 'USER', '鎻愪氦 Go2 Edu 閲囪喘璁㈠崟', '2026-01-15 10:18:00'),
    (2063223234097972002, 2063223234097970001, 'PENDING_PAY', 'PAID', 'SYSTEM', '寰俊鏀粯鎴愬姛', '2026-01-15 10:21:00'),
    (2063223234097972003, 2063223234097970001, 'PAID', 'FULFILLING', 'ADMIN', '璁惧鍑哄簱骞跺畬鎴愬簭鍒楀彿鐧昏', '2026-01-17 09:30:00'),
    (2063223234097972004, 2063223234097970001, 'FULFILLING', 'COMPLETED', 'USER', '璁惧绛炬敹骞跺畬鎴愭縺娲?, '2026-01-23 16:30:00'),
    (2063223234097972005, 2063223234097970002, NULL, 'PENDING_PAY', 'USER', '鎻愪氦 B2 宸ヤ笟宸℃濂楄璁㈠崟', '2026-03-18 09:42:00'),
    (2063223234097972006, 2063223234097970002, 'PENDING_PAY', 'PAID', 'SYSTEM', '瀵瑰叕杞处鍒拌处', '2026-03-19 11:00:00'),
    (2063223234097972007, 2063223234097970002, 'PAID', 'FULFILLING', 'ADMIN', '瀹屾垚宸ヤ笟宸℃閰嶇疆鍜屽嚭搴撴娴?, '2026-03-25 15:10:00'),
    (2063223234097972008, 2063223234097970002, 'FULFILLING', 'COMPLETED', 'USER', 'B2 鍒板満骞堕€氳繃楠屾敹', '2026-04-02 14:20:00'),
    (2063223234097972009, 2063223234097970003, NULL, 'PENDING_PAY', 'USER', '鎻愪氦 G1 浜哄舰鏈哄櫒浜鸿鍗?, '2026-05-08 15:26:00'),
    (2063223234097972010, 2063223234097970003, 'PENDING_PAY', 'PAID', 'SYSTEM', '寰俊鏀粯鎴愬姛', '2026-05-08 15:29:00'),
    (2063223234097972011, 2063223234097970003, 'PAID', 'FULFILLING', 'ADMIN', 'G1 鐢熶骇鎺掓湡瀹屾垚锛岀瓑寰呬氦浠?, '2026-06-05 11:00:00'),
    (2063223234097972012, 2063223234097970004, NULL, 'PENDING_PAY', 'USER', '鎻愪氦 CloudFleet 骞村害璁㈤槄璁㈠崟', '2026-06-06 20:16:00'),
    (2063223234097972013, 2063223234097970005, NULL, 'PENDING_PAY', 'USER', '鎻愪氦 Go2 涓冩棩绉熻祦璁㈠崟', '2026-04-12 18:05:00'),
    (2063223234097972014, 2063223234097970005, 'PENDING_PAY', 'CANCELLED', 'USER', '娲诲姩妗ｆ湡璋冩暣锛岀敤鎴峰彇娑堣鍗?, '2026-04-13 09:12:00')
ON DUPLICATE KEY UPDATE
    reason = VALUES(reason),
    created_at = VALUES(created_at);

INSERT INTO payment_orders
    (id, order_id, out_trade_no, transaction_id, amount_minor, channel, status,
     prepay_id, pay_sign, created_at, updated_at)
VALUES
    (2063223234097973001, 2063223234097970001, 'PAY202601150001UNITREE',
     'WX420000202601150001', 599000, 'WECHAT_JSAPI', 'SUCCESS', NULL, NULL,
     '2026-01-15 10:19:00', '2026-01-15 10:21:00'),
    (2063223234097973002, 2063223234097970002, 'PAY202603180002UNITREE',
     'BANK202603190002', 1499900, 'BANK_TRANSFER', 'SUCCESS', NULL, NULL,
     '2026-03-18 09:45:00', '2026-03-19 11:00:00'),
    (2063223234097973003, 2063223234097970003, 'PAY202605080003UNITREE',
     'WX420000202605080003', 999900, 'WECHAT_JSAPI', 'SUCCESS', NULL, NULL,
     '2026-05-08 15:27:00', '2026-05-08 15:29:00'),
    (2063223234097973004, 2063223234097970004, 'PAY202606060004UNITREE',
     NULL, 599900, 'WECHAT_JSAPI', 'PENDING', 'wx-prepay-unitree-demo-0004', NULL,
     '2026-06-06 20:16:00', '2026-06-06 20:16:00')
ON DUPLICATE KEY UPDATE
    transaction_id = VALUES(transaction_id),
    amount_minor = VALUES(amount_minor),
    status = VALUES(status),
    updated_at = VALUES(updated_at);

INSERT INTO contracts
    (id, order_id, template_version, cos_key, pdf_hash, sign_status, created_at)
VALUES
    (2063223234097973101, 2063223234097970001, 'v1',
     'contracts/XQ202601150001UNITREE.pdf',
     '8e4f2b7ea3b7059ccb2d6bf5089a47368fcde1154bc74bdc58c6fa5a9a32c101',
     'SIGNED', '2026-01-15 10:22:00'),
    (2063223234097973102, 2063223234097970002, 'v1',
     'contracts/XQ202603180002UNITREE.pdf',
     '141954b643d0fa2df378a0ba9f1f951e941d10d80fb85fcde50cd03e31ce8102',
     'SIGNED', '2026-03-19 11:01:00'),
    (2063223234097973103, 2063223234097970003, 'v1',
     'contracts/XQ202605080003UNITREE.pdf',
     'ecfb4b7f5c2c74a2814aa36ef12d3c5d222cfafbf351815f26c84754115ac903',
     'PENDING', '2026-05-08 15:30:00')
ON DUPLICATE KEY UPDATE
    template_version = VALUES(template_version),
    cos_key = VALUES(cos_key),
    pdf_hash = VALUES(pdf_hash),
    sign_status = VALUES(sign_status),
    created_at = VALUES(created_at);

-- ---------------------------------------------------------------------------
-- 3. Membership and benefits, aligned with paid target-user orders
-- ---------------------------------------------------------------------------
INSERT INTO user_membership
    (id, user_id, level, is_native, lifetime_spend_minor, planet_card_expires_at, created_at, updated_at)
VALUES
    (2063223234097974001, @target_user_id, 3, 1, 3098800, '2027-05-31 23:59:59',
     '2026-01-15 10:21:00', '2026-06-01 09:00:00')
ON DUPLICATE KEY UPDATE
    level = VALUES(level),
    is_native = VALUES(is_native),
    lifetime_spend_minor = VALUES(lifetime_spend_minor),
    planet_card_expires_at = VALUES(planet_card_expires_at),
    updated_at = VALUES(updated_at);

INSERT INTO membership_benefit_grants
    (id, user_id, benefit_type, total_count, used_count, expires_at)
VALUES
    (2063223234097974101, @target_user_id, 'FREE_DEPOSIT', 12, 3, '2027-05-31 23:59:59'),
    (2063223234097974102, @target_user_id, 'MAINTENANCE', 6, 1, '2027-05-31 23:59:59'),
    (2063223234097974103, @target_user_id, 'DISCOUNT', 8, 2, '2027-05-31 23:59:59')
ON DUPLICATE KEY UPDATE
    total_count = VALUES(total_count),
    used_count = VALUES(used_count),
    expires_at = VALUES(expires_at);

INSERT INTO membership_events
    (id, user_id, event_type, description, created_at)
VALUES
    (2063223234097974201, @target_user_id, 'PAID_ORDER', 'Paid order: 2063223234097970001', '2026-01-15 10:21:00'),
    (2063223234097974202, @target_user_id, 'NATIVE_TAGGED', '棣栨璐拱瀹囨爲璁惧锛屾爣璁颁负鍘熶綇姘戜細鍛?, '2026-01-15 10:21:00'),
    (2063223234097974203, @target_user_id, 'LEVEL_UP', '浼氬憳绛夌骇鍗囩骇鑷?L3', '2026-03-19 11:00:00'),
    (2063223234097974204, @target_user_id, 'PLANET_CARD_PURCHASE', '缁垂瀹囨爲璁惧灏婁韩鏄熺悆鍗?, '2026-06-01 09:00:00'),
    (2063223234097974205, @target_user_id, 'PAID_ORDER', 'Paid order: 2063223234097970002', '2026-03-19 11:00:00'),
    (2063223234097974206, @target_user_id, 'PAID_ORDER', 'Paid order: 2063223234097970003', '2026-05-08 15:29:00')
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    created_at = VALUES(created_at);

-- ---------------------------------------------------------------------------
-- 4. Assets, trusteeship slots and revenue records
-- ---------------------------------------------------------------------------
INSERT INTO user_assets
    (id, user_id, name, type, model_info, image_url, status, purchase_order_id, acquired_at, created_at)
VALUES
    (2063223234097975001, @target_user_id, 'Go2 Edu 鏁欑爺鍥涜冻鏈哄櫒浜?U2-0701', 'ROBOT',
     'Unitree Go2 Edu / SN: GO2E-U20701', '', 'IDLE',
     '2063223234097970001', '2026-01-23 16:30:00', '2026-01-23 16:30:00'),
    (2063223234097975002, @target_user_id, 'B2 宸ヤ笟宸℃鏈哄櫒浜?B2-0318', 'ROBOT',
     'Unitree B2 / SN: B2-XQ0318', '', 'TRUSTEED',
     '2063223234097970002', '2026-04-02 14:20:00', '2026-04-02 14:20:00'),
    (2063223234097975003, @target_user_id, 'G1 浜哄舰鏈哄櫒浜?G1-0508', 'ROBOT',
     'Unitree G1 / SN: G1-XQ0508', '', 'MAINTENANCE',
     '2063223234097970003', '2026-06-05 11:00:00', '2026-06-05 11:00:00')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    model_info = VALUES(model_info),
    image_url = VALUES(image_url),
    status = VALUES(status),
    purchase_order_id = VALUES(purchase_order_id),
    acquired_at = VALUES(acquired_at);

INSERT INTO trusteeship_slots
    (id, asset_id, user_id, start_time, end_time, status, daily_rate_minor,
     total_revenue_minor, created_at)
VALUES
    (2063223234097975101, 2063223234097975001, @target_user_id,
     '2026-02-01 09:00:00', '2026-02-11 09:00:00', 'COMPLETED', 7600, 76000, '2026-01-28 13:20:00'),
    (2063223234097975102, 2063223234097975002, @target_user_id,
     '2026-05-20 09:00:00', '2026-06-20 09:00:00', 'ACTIVE', 12800, 0, '2026-05-18 10:10:00'),
    (2063223234097975103, 2063223234097975002, @target_user_id,
     '2026-04-10 09:00:00', '2026-04-25 09:00:00', 'COMPLETED', 12800, 192000, '2026-04-08 16:00:00')
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    daily_rate_minor = VALUES(daily_rate_minor),
    total_revenue_minor = VALUES(total_revenue_minor);

INSERT INTO asset_revenue_ledger
    (id, asset_id, user_id, order_id, amount_minor, status, created_at)
VALUES
    (2063223234097975201, 2063223234097975001, @target_user_id, NULL, 76000, 'SETTLED', '2026-02-11 09:10:00'),
    (2063223234097975202, 2063223234097975002, @target_user_id, NULL, 64000, 'SETTLED', '2026-04-15 09:10:00'),
    (2063223234097975203, 2063223234097975002, @target_user_id, NULL, 128000, 'SETTLED', '2026-04-25 09:10:00'),
    (2063223234097975204, 2063223234097975002, @target_user_id, NULL, 28800, 'PENDING_SETTLE', '2026-06-05 09:10:00')
ON DUPLICATE KEY UPDATE
    amount_minor = VALUES(amount_minor),
    status = VALUES(status),
    created_at = VALUES(created_at);

-- ---------------------------------------------------------------------------
-- 5. Distribution team and commissions; source orders belong to invitees
-- ---------------------------------------------------------------------------
INSERT INTO invite_relations
    (id, inviter_user_id, invitee_user_id, level, bound_at, source)
VALUES
    (2063223234097976001, @target_user_id, @invitee_a_id, 1, '2026-03-12 10:20:00', 'REGISTER'),
    (2063223234097976002, @target_user_id, @invitee_b_id, 1, '2026-04-03 14:10:00', 'REGISTER'),
    (2063223234097976003, @target_user_id, @invitee_c_id, 2, '2026-05-16 11:40:00', 'REGISTER'),
    (2063223234097976004, @invitee_a_id, @invitee_c_id, 1, '2026-05-16 11:40:00', 'REGISTER')
ON DUPLICATE KEY UPDATE
    inviter_user_id = VALUES(inviter_user_id),
    invitee_user_id = VALUES(invitee_user_id),
    level = VALUES(level),
    bound_at = VALUES(bound_at),
    source = VALUES(source);

INSERT INTO orders
    (id, order_no, user_id, sku_id, order_type, status, amount_minor, deposit_minor,
     shipping_minor, discount_minor, payable_minor, address_json, idempotency_key, created_at, updated_at)
VALUES
    (2063223234097976101, 'XQ202603250101TEAM', @invitee_a_id, 9001, 'BUY', 'COMPLETED',
     599000, 0, 0, 0, 599000, NULL, 'seed-team-order-a', '2026-03-25 11:00:00', '2026-04-01 15:00:00'),
    (2063223234097976102, 'XQ202604180102TEAM', @invitee_b_id, 9002, 'BUY', 'COMPLETED',
     1199000, 0, 0, 0, 1199000, NULL, 'seed-team-order-b', '2026-04-18 09:30:00', '2026-04-29 16:00:00'),
    (2063223234097976103, 'XQ202606030103TEAM', @invitee_a_id, 9003, 'BUY', 'PAID',
     999900, 0, 0, 0, 999900, NULL, 'seed-team-order-c', '2026-06-03 14:20:00', '2026-06-03 14:25:00'),
    (2063223234097976104, 'XQ202605200104TEAM', @invitee_c_id, 9001, 'BUY', 'COMPLETED',
     560000, 0, 0, 0, 560000, NULL, 'seed-team-order-d', '2026-05-20 10:12:00', '2026-05-27 18:30:00'),
    (2063223234097976105, 'XQ202605250105TEAM', @invitee_b_id, 9012, 'BUY', 'COMPLETED',
     252000, 0, 0, 0, 252000, NULL, 'seed-team-order-e', '2026-05-25 15:18:00', '2026-05-25 18:00:00'),
    (2063223234097976106, 'XQ202606040106TEAM', @invitee_a_id, 9004, 'BUY', 'COMPLETED',
     176000, 0, 0, 0, 176000, NULL, 'seed-team-order-f', '2026-06-04 13:05:00', '2026-06-05 09:10:00')
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    payable_minor = VALUES(payable_minor),
    updated_at = VALUES(updated_at);

INSERT INTO order_lines
    (id, order_id, sku_id, sku_name, quantity, unit_price_minor)
VALUES
    (2063223234097976201, 2063223234097976101, 9001, 'Unitree Go2 Edu 鍥涜冻鏈哄櫒浜洪噰璐瑁?, 1, 599000),
    (2063223234097976202, 2063223234097976102, 9002, 'Unitree B2 宸ヤ笟鍥涜冻鏈哄櫒浜洪噰璐瑁?, 1, 1199000),
    (2063223234097976203, 2063223234097976103, 9003, 'Unitree G1 浜哄舰鏈哄櫒浜洪噰璐瑁?, 1, 999900),
    (2063223234097976204, 2063223234097976104, 9001, 'Unitree Go2 Edu 场馆共创套装', 1, 560000),
    (2063223234097976205, 2063223234097976105, 9012, 'Unitree 官方配件包', 1, 252000),
    (2063223234097976206, 2063223234097976106, 9004, 'Unitree 机器人课程训练营', 1, 176000)
ON DUPLICATE KEY UPDATE
    sku_name = VALUES(sku_name),
    quantity = VALUES(quantity),
    unit_price_minor = VALUES(unit_price_minor);

INSERT INTO commission_entries
    (id, order_id, beneficiary_user_id, source_user_id, level, amount_minor,
     rate_percent, status, protect_until, settled_at, created_at)
VALUES
    (2063223234097976301, 2063223234097976101, @target_user_id, @invitee_a_id,
     1, 29950, 5, 'SETTLED', '2026-04-01 11:00:00', '2026-04-02 09:00:00', '2026-03-25 11:05:00'),
    (2063223234097976302, 2063223234097976102, @target_user_id, @invitee_b_id,
     1, 59950, 5, 'SETTLED', '2026-04-25 09:30:00', '2026-04-26 09:00:00', '2026-04-18 09:35:00'),
    (2063223234097976303, 2063223234097976103, @target_user_id, @invitee_a_id,
     1, 49995, 5, 'PENDING_PROTECT', '2026-06-10 14:25:00', NULL, '2026-06-03 14:25:00'),
    (2063223234097976304, 2063223234097976104, @target_user_id, @invitee_c_id,
     2, 16800, 3, 'SETTLED', '2026-05-27 10:12:00', '2026-05-28 09:00:00', '2026-05-20 10:15:00'),
    (2063223234097976305, 2063223234097976105, @target_user_id, @invitee_b_id,
     1, 12600, 5, 'SETTLEABLE', '2026-06-01 15:18:00', NULL, '2026-05-25 15:20:00'),
    (2063223234097976306, 2063223234097976106, @target_user_id, @invitee_a_id,
     1, 8800, 5, 'FROZEN', '2026-06-11 13:05:00', NULL, '2026-06-04 13:08:00')
ON DUPLICATE KEY UPDATE
    amount_minor = VALUES(amount_minor),
    rate_percent = VALUES(rate_percent),
    status = VALUES(status),
    protect_until = VALUES(protect_until),
    settled_at = VALUES(settled_at);

INSERT INTO commission_settlement_batches
    (id, batch_no, settled_at, total_entries, total_amount_minor)
VALUES
    (2063223234097976401, 'SETTLE-20260402-UNITREE', '2026-04-02 09:00:00', 1, 29950),
    (2063223234097976402, 'SETTLE-20260426-UNITREE', '2026-04-26 09:00:00', 1, 59950)
ON DUPLICATE KEY UPDATE
    settled_at = VALUES(settled_at),
    total_entries = VALUES(total_entries),
    total_amount_minor = VALUES(total_amount_minor);

-- ---------------------------------------------------------------------------
-- 6. Wallet and auditable ledger; final balance = 169600, frozen = 26000
-- ---------------------------------------------------------------------------
INSERT INTO wallets
    (id, user_id, balance_minor, frozen_minor, created_at, updated_at)
VALUES
    (2063223234097977001, @target_user_id, 169600, 26000, '2026-02-11 09:10:00', '2026-06-06 10:00:00')
ON DUPLICATE KEY UPDATE
    balance_minor = VALUES(balance_minor),
    frozen_minor = VALUES(frozen_minor),
    updated_at = VALUES(updated_at);

INSERT INTO wallet_ledger
    (id, user_id, type, amount_minor, ref_type, ref_id, balance_after_minor, description, created_at)
VALUES
    (2063223234097977101, @target_user_id, 'CREDIT', 76000, 'TRUSTEESHIP',
     '2063223234097975201', 76000, 'Go2 Edu 棣栨湡鎵樼鏀剁泭缁撶畻', '2026-02-11 09:10:00'),
    (2063223234097977102, @target_user_id, 'CREDIT', 29950, 'COMMISSION',
     '2063223234097976301', 105950, 'Go2 Edu 鎺ㄥ箍浣ｉ噾缁撶畻', '2026-04-02 09:00:00'),
    (2063223234097977103, @target_user_id, 'CREDIT', 50000, 'MEMBER_REWARD',
     'LEVEL_3', 155950, 'Lv.3 浼氬憳鎴愰暱濂栧姳', '2026-04-03 10:00:00'),
    (2063223234097977104, @target_user_id, 'DEBIT', 29900, 'PLANET_CARD',
     'UNITREE_YEAR_CARD', 126050, '缁垂瀹囨爲璁惧灏婁韩鏄熺悆鍗?, '2026-04-05 12:00:00'),
    (2063223234097977106, @target_user_id, 'CREDIT', 64000, 'TRUSTEESHIP',
     '2063223234097975202', 190050, 'B2 宸ヤ笟宸℃鎵樼鏀剁泭涓€鏈熺粨绠?, '2026-04-15 09:10:00'),
    (2063223234097977108, @target_user_id, 'CREDIT', 128000, 'TRUSTEESHIP',
     '2063223234097975203', 318050, 'B2 宸ヤ笟宸℃鎵樼鏀剁泭浜屾湡缁撶畻', '2026-04-25 09:10:00'),
    (2063223234097977105, @target_user_id, 'CREDIT', 59950, 'COMMISSION',
     '2063223234097976302', 378000, 'B2 宸ヤ笟宸℃濂楄鎺ㄥ箍浣ｉ噾缁撶畻', '2026-04-26 09:00:00'),
    (2063223234097977110, @target_user_id, 'FREEZE', 12000, 'WITHDRAW',
     '2063223234097977202', 366000, '佣金提现失败前冻结资金', '2026-04-28 15:30:00'),
    (2063223234097977111, @target_user_id, 'UNFREEZE', 12000, 'WITHDRAW',
     '2063223234097977202', 378000, '佣金提现失败退回余额', '2026-04-28 15:42:00'),
    (2063223234097977112, @target_user_id, 'CREDIT', 16800, 'COMMISSION',
     '2063223234097976304', 231600, 'Go2 Edu 场馆共创推广二级佣金结算', '2026-05-28 09:00:00'),
    (2063223234097977113, @target_user_id, 'FREEZE', 36000, 'WITHDRAW',
     '2063223234097977203', 195600, '佣金提现申请资金冻结', '2026-05-30 11:05:00'),
    (2063223234097977114, @target_user_id, 'DEBIT', 36000, 'WITHDRAW',
     '2063223234097977203', 195600, '佣金提现成功', '2026-05-30 11:18:00'),
    (2063223234097977109, @target_user_id, 'DEBIT', 163200, 'ASSET_SERVICE',
     'B2-ANNUAL-SERVICE-2026', 214800, 'B2 宸ヤ笟宸℃骞村害缁翠繚涓庝簯绔湇鍔¤垂', '2026-05-06 14:00:00'),
    (2063223234097977107, @target_user_id, 'FREEZE', 26000, 'WITHDRAW',
     '2063223234097977201', 169600, '鎻愮幇鐢宠璧勯噾鍐荤粨', '2026-06-06 10:00:00')
ON DUPLICATE KEY UPDATE
    type = VALUES(type),
    amount_minor = VALUES(amount_minor),
    ref_type = VALUES(ref_type),
    ref_id = VALUES(ref_id),
    balance_after_minor = VALUES(balance_after_minor),
    description = VALUES(description),
    created_at = VALUES(created_at);

INSERT INTO withdraw_requests
    (id, user_id, amount_minor, status, wx_batch_id, fail_reason, idempotency_key, created_at, updated_at)
VALUES
    (2063223234097977201, @target_user_id, 26000, 'PROCESSING', 'UNITREE-DEMO-WX-20260606',
     NULL, 'seed-withdraw-2063223234097950700-1', '2026-06-06 10:00:00', '2026-06-06 10:02:00'),
    (2063223234097977202, @target_user_id, 12000, 'FAILED', NULL,
     '银行卡实名校验未通过，已原路退回余额', 'seed-withdraw-2063223234097950700-2', '2026-04-28 15:30:00', '2026-04-28 15:42:00'),
    (2063223234097977203, @target_user_id, 36000, 'SUCCESS', 'UNITREE-DEMO-WX-20260530',
     NULL, 'seed-withdraw-2063223234097950700-3', '2026-05-30 11:05:00', '2026-05-30 11:18:00')
ON DUPLICATE KEY UPDATE
    amount_minor = VALUES(amount_minor),
    status = VALUES(status),
    wx_batch_id = VALUES(wx_batch_id),
    fail_reason = VALUES(fail_reason),
    updated_at = VALUES(updated_at);

-- ---------------------------------------------------------------------------
-- 7. Devices and repair work orders with progress history
-- ---------------------------------------------------------------------------
INSERT INTO devices
    (id, device_no, name, location, area, qr_code, responsible_technician_id,
     type, model, manufacturer, install_date, status, created_at, updated_at)
VALUES
    (2063223234097978001, 'UNITREE-GO2-U20701', 'Go2 Edu 鏁欑爺鍥涜冻鏈哄櫒浜?U2-0701',
     '鏉窞鏈潵绉戞妧鍩庢満鍣ㄤ汉鍒涙柊涓績 A3 瀹為獙鍖?, '鏉窞', 'XQ-DEV-UNITREE-GO2-U20701', NULL,
     'QUADRUPED_ROBOT', 'Go2 Edu', '瀹囨爲绉戞妧 Unitree', '2026-01-23 16:30:00',
     'NORMAL', '2026-01-23 16:30:00', '2026-06-07 10:00:00'),
    (2063223234097978002, 'UNITREE-B2-XQ0318', 'B2 宸ヤ笟宸℃鏈哄櫒浜?B2-0318',
     '鏉窞鏈潵绉戞妧鍩庢満鍣ㄤ汉鍒涙柊涓績 B1 宸℃鍖?, '鏉窞', 'XQ-DEV-UNITREE-B2-XQ0318', NULL,
     'QUADRUPED_ROBOT', 'B2', '瀹囨爲绉戞妧 Unitree', '2026-04-02 14:20:00',
     'NORMAL', '2026-04-02 14:20:00', '2026-06-07 10:00:00'),
    (2063223234097978003, 'UNITREE-G1-XQ0508', 'G1 浜哄舰鏈哄櫒浜?G1-0508',
     '鏉窞鏈潵绉戞妧鍩庢満鍣ㄤ汉鍒涙柊涓績 C2 灞曠ず鍖?, '鏉窞', 'XQ-DEV-UNITREE-G1-XQ0508', NULL,
     'HUMANOID_ROBOT', 'G1', '瀹囨爲绉戞妧 Unitree', '2026-06-05 11:00:00',
     'MAINTENANCE', '2026-06-05 11:00:00', '2026-06-07 10:00:00')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    location = VALUES(location),
    area = VALUES(area),
    qr_code = VALUES(qr_code),
    type = VALUES(type),
    model = VALUES(model),
    manufacturer = VALUES(manufacturer),
    status = VALUES(status),
    updated_at = VALUES(updated_at);

INSERT INTO work_orders
    (id, order_no, device_id, device_name, reporter_user_id, reporter_name, reporter_phone,
     fault_type, fault_description, priority, status, assigned_technician_id, images,
     solution_description, completed_at, created_at, updated_at)
VALUES
    (2063223234097978101, 'WO202602180001UNITREE', 2063223234097978001,
     'Go2 Edu 鏁欑爺鍥涜冻鏈哄櫒浜?U2-0701', @target_user_id, '鏋楀厛鐢?, '13800005208',
     '鍏宠妭寮傚搷', 'Go2 宸﹀悗鑵垮湪鎱㈤€熸鎬佷笅鍑虹幇杞诲井寮傚搷锛岃繛缁繍琛岀害 20 鍒嗛挓鍚庢洿鏄庢樉銆?,
     'MEDIUM', 'DONE', NULL, '[]',
     '娓呮磥楂嬪叧鑺傚苟閲嶆柊鏍囧畾鐢垫満闆朵綅锛屽浐浠跺崌绾ц嚦绋冲畾鐗堟湰锛岃繛缁鎬佹祴璇?2 灏忔椂閫氳繃銆?,
     '2026-02-20 16:20:00', '2026-02-18 09:15:00', '2026-02-21 10:00:00'),
    (2063223234097978102, 'WO202605280002UNITREE', 2063223234097978002,
     'B2 宸ヤ笟宸℃鏈哄櫒浜?B2-0318', @target_user_id, '鏋楀厛鐢?, '13800005208',
     '婵€鍏夐浄杈炬暟鎹姈鍔?, 'B2 鍦ㄤ粨搴撳己鍙嶅厜鍖哄煙宸℃鏃剁偣浜戝伓鍙戞姈鍔紝CloudFleet 鍦板浘鍑虹幇鐭椂婕傜Щ銆?,
     'HIGH', 'IN_PROGRESS', NULL, '[]', NULL, NULL,
     '2026-05-28 13:40:00', '2026-06-07 09:30:00'),
    (2063223234097978103, 'WO202606060003UNITREE', 2063223234097978003,
     'G1 浜哄舰鏈哄櫒浜?G1-0508', @target_user_id, '鏋楀厛鐢?, '13800005208',
     '鎵嬭噦闆朵綅鍋忓樊', 'G1 寮€鏈鸿嚜妫€鍚庡彸鑷傞浂浣嶅瓨鍦ㄧ害 3 搴﹀亸宸紝宸插仠姝㈠睍绀哄苟鐢宠鐜板満鏍″噯銆?,
     'MEDIUM', 'PENDING_ACCEPT', NULL, '[]',
     '瀹屾垚鍙宠噦缂栫爜鍣ㄦ鏌ュ拰闆朵綅閲嶆爣瀹氾紝閲嶅鍔ㄤ綔娴嬭瘯 50 娆℃棤鍋忓樊锛岀瓑寰呯敤鎴烽獙鏀躲€?,
     '2026-06-07 08:50:00', '2026-06-06 08:30:00', '2026-06-07 08:50:00')
ON DUPLICATE KEY UPDATE
    device_name = VALUES(device_name),
    fault_type = VALUES(fault_type),
    fault_description = VALUES(fault_description),
    priority = VALUES(priority),
    status = VALUES(status),
    solution_description = VALUES(solution_description),
    completed_at = VALUES(completed_at),
    updated_at = VALUES(updated_at);

INSERT INTO work_order_logs
    (id, work_order_id, action, operator_id, operator_name, from_status, to_status, remark, created_at)
VALUES
    (2063223234097978201, 2063223234097978101, 'CREATED', @target_user_id, '鏋楀厛鐢?, NULL, 'NEW', NULL, '2026-02-18 09:15:00'),
    (2063223234097978202, 2063223234097978101, 'ASSIGNED', NULL, '瀹囨爲鍞悗涓績', 'NEW', 'ASSIGNED', '瀹夋帓鍥涜冻鏈哄櫒浜烘妧鏈伐绋嬪笀', '2026-02-18 10:00:00'),
    (2063223234097978203, 2063223234097978101, 'ACCEPTED', NULL, '瀹囨爲鎶€鏈伐绋嬪笀鍛ㄥ伐', 'ASSIGNED', 'IN_PROGRESS', NULL, '2026-02-18 10:20:00'),
    (2063223234097978204, 2063223234097978101, 'COMPLETED', NULL, '瀹囨爲鎶€鏈伐绋嬪笀鍛ㄥ伐', 'IN_PROGRESS', 'PENDING_ACCEPT', '瀹屾垚鍏宠妭娓呮磥銆佹爣瀹氬拰鍥轰欢鍗囩骇', '2026-02-20 16:20:00'),
    (2063223234097978205, 2063223234097978101, 'VERIFIED', @target_user_id, '鏋楀厛鐢?, 'PENDING_ACCEPT', 'DONE', '鐜板満楠屾敹閫氳繃', '2026-02-21 10:00:00'),
    (2063223234097978206, 2063223234097978102, 'CREATED', @target_user_id, '鏋楀厛鐢?, NULL, 'NEW', NULL, '2026-05-28 13:40:00'),
    (2063223234097978207, 2063223234097978102, 'ASSIGNED', NULL, '瀹囨爲宸ヤ笟鏀寔缁?, 'NEW', 'ASSIGNED', '瀹夋帓 B2 瀵艰埅绠楁硶宸ョ▼甯堣繙绋嬭瘖鏂?, '2026-05-28 14:10:00'),
    (2063223234097978208, 2063223234097978102, 'ACCEPTED', NULL, '瀹囨爲鎶€鏈伐绋嬪笀闄堝伐', 'ASSIGNED', 'IN_PROGRESS', NULL, '2026-05-28 14:30:00'),
    (2063223234097978209, 2063223234097978102, 'PROGRESS', NULL, '瀹囨爲鎶€鏈伐绋嬪笀闄堝伐', 'IN_PROGRESS', 'IN_PROGRESS', '宸叉洿鏂伴浄杈炬护娉㈠弬鏁帮紝姝ｅ湪杩涜寮哄弽鍏夊満鏅娴?, '2026-06-07 09:30:00'),
    (2063223234097978210, 2063223234097978103, 'CREATED', @target_user_id, '鏋楀厛鐢?, NULL, 'NEW', NULL, '2026-06-06 08:30:00'),
    (2063223234097978211, 2063223234097978103, 'COMPLETED', NULL, '瀹囨爲浜哄舰鏈哄櫒浜烘敮鎸佺粍', 'IN_PROGRESS', 'PENDING_ACCEPT', '鍙宠噦闆朵綅閲嶆爣瀹氬畬鎴愶紝绛夊緟楠屾敹', '2026-06-07 08:50:00')
ON DUPLICATE KEY UPDATE
    action = VALUES(action),
    operator_name = VALUES(operator_name),
    from_status = VALUES(from_status),
    to_status = VALUES(to_status),
    remark = VALUES(remark),
    created_at = VALUES(created_at);

-- ---------------------------------------------------------------------------
-- 8. Notifications used by both "娑堟伅" and the profile unread counter
-- ---------------------------------------------------------------------------
INSERT INTO notifications
    (id, user_id, type, title, content, ref_id, is_read, created_at)
VALUES
    (2063223234097979001, @target_user_id, 'ORDER', 'G1 璁㈠崟杩涘叆浜や粯闃舵',
     '鎮ㄧ殑 Unitree G1 浜哄舰鏈哄櫒浜哄凡瀹屾垚鐢熶骇鎺掓湡锛屽綋鍓嶆鍦ㄨ繘琛屽嚭搴撳墠鍔ㄤ綔涓庡畨鍏ㄦ娴嬨€?,
     '2063223234097970003', 0, '2026-06-07 09:40:00'),
    (2063223234097979002, @target_user_id, 'SYSTEM', 'B2 鍏湀鎵樼鏀剁泭宸茬敓鎴?,
     '鏈湡 B2 宸ヤ笟宸℃鏈哄櫒浜烘墭绠℃敹鐩?288.00 鍏冨凡鐢熸垚锛屽綋鍓嶇姸鎬佷负寰呯粨绠椼€?,
     '2063223234097975002', 0, '2026-06-05 09:12:00'),
    (2063223234097979003, @target_user_id, 'SYSTEM', '鎻愮幇鐢宠澶勭悊涓?,
     '鎮ㄦ彁浜ょ殑 260.00 鍏冩彁鐜扮敵璇峰凡鍙楃悊锛岃祫閲戝凡鍐荤粨锛岄璁?1 涓伐浣滄棩鍐呭埌璐︺€?,
     '2063223234097977201', 0, '2026-06-06 10:02:00'),
    (2063223234097979004, @target_user_id, 'ORDER', 'CloudFleet 璁㈠崟寰呮敮浠?,
     'Unitree CloudFleet 浜戠璋冨害绯荤粺骞村害璁㈤槄璁㈠崟灏氭湭鏀粯锛岃鍦ㄨ鍗曞叧闂墠瀹屾垚浠樻銆?,
     '2063223234097970004', 0, '2026-06-06 20:18:00'),
    (2063223234097979005, @target_user_id, 'SYSTEM', 'G1 鎶ヤ慨寰呮偍楠屾敹',
     'G1 鍙宠噦闆朵綅鏍″噯宸插畬鎴愶紝璇疯繘鍏ユ垜鐨勬姤淇煡鐪嬪鐞嗚褰曞苟楠屾敹銆?,
     '2063223234097978103', 0, '2026-06-07 08:52:00'),
    (2063223234097979006, @target_user_id, 'SOCIAL', '鏂扮殑鍥㈤槦鎴愬憳鍔犲叆',
     '鏈哄櫒鐙楄缁冭惀閫氳繃鎮ㄧ殑浜岀骇鎺ㄥ箍閾捐矾鍔犲叆鍥㈤槦锛屽洟闃熸垚鍛樻暟宸叉洿鏂般€?,
     CAST(@invitee_c_id AS CHAR), 1, '2026-05-16 11:42:00'),
    (2063223234097979007, @target_user_id, 'SYSTEM', 'Lv.3 浼氬憳鏉冪泭宸插埌璐?,
     '鍏嶆娂銆佺淮淇濅笌璁惧鎶樻墸鏉冪泭宸插彂鏀撅紝鍙湪浼氬憳涓績鏌ョ湅鍓╀綑娆℃暟銆?,
     '2063223234097974001', 1, '2026-04-03 10:02:00'),
    (2063223234097979008, @target_user_id, 'ORDER', 'B2 宸ヤ笟宸℃鏈哄櫒浜洪獙鏀跺畬鎴?,
     'B2 宸ヤ笟宸℃鏈哄櫒浜哄凡瀹屾垚浜や粯楠屾敹骞跺姞鍏ユ垜鐨勮祫浜с€?,
     '2063223234097970002', 1, '2026-04-02 14:22:00')
ON DUPLICATE KEY UPDATE
    type = VALUES(type),
    title = VALUES(title),
    content = VALUES(content),
    ref_id = VALUES(ref_id),
    is_read = VALUES(is_read),
    created_at = VALUES(created_at);

COMMIT;

-- ---------------------------------------------------------------------------
-- Verification summary for the profile page and its eight linked modules
-- ---------------------------------------------------------------------------
SELECT
    u.id AS user_id,
    u.nickname,
    m.level AS membership_level,
    m.lifetime_spend_minor,
    w.balance_minor,
    w.frozen_minor,
    (SELECT COUNT(*) FROM orders o WHERE o.user_id = u.id) AS order_count,
    (SELECT COUNT(*) FROM user_assets a WHERE a.user_id = u.id) AS asset_count,
    (SELECT COALESCE(SUM(r.amount_minor), 0) FROM asset_revenue_ledger r WHERE r.user_id = u.id) AS trusteeship_revenue_minor,
    (SELECT COUNT(*) FROM invite_relations ir WHERE ir.inviter_user_id = u.id AND ir.level = 1) AS level1_team_count,
    (SELECT COUNT(*) FROM invite_relations ir WHERE ir.inviter_user_id = u.id AND ir.level = 2) AS level2_team_count,
    (SELECT COALESCE(SUM(c.amount_minor), 0) FROM commission_entries c WHERE c.beneficiary_user_id = u.id AND c.status = 'SETTLED') AS settled_commission_minor,
    (SELECT COUNT(*) FROM work_orders wo WHERE wo.reporter_user_id = u.id) AS repair_count,
    (SELECT COUNT(*) FROM notifications n WHERE n.user_id = u.id AND n.is_read = 0) AS unread_message_count
FROM users u
JOIN user_membership m ON m.user_id = u.id
JOIN wallets w ON w.user_id = u.id
WHERE u.id = @target_user_id;

