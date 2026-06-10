USE xingqiu_dev;

START TRANSACTION;

SET @target_user_id = 2063223234097950700;
SET @invitee_a_id = 2063223234097961001;
SET @invitee_b_id = 2063223234097961002;
SET @invitee_c_id = 2063223234097961003;

INSERT INTO users
    (id, openid, unionid, nickname, avatar_url, role, created_at, updated_at)
VALUES
    (@target_user_id, 'demo-dist-target-2063223234097950700', NULL, '林启航', '/static/images/default-avatar.svg', 'USER', '2026-01-08 09:30:00', '2026-06-07 10:00:00'),
    (@invitee_a_id, 'demo-dist-invitee-a-2063223234097961001', NULL, '杭州机器人研学营', '/static/images/default-avatar.svg', 'USER', '2026-03-12 10:20:00', '2026-06-07 10:00:00'),
    (@invitee_b_id, 'demo-dist-invitee-b-2063223234097961002', NULL, '智巡科技', '/static/images/default-avatar.svg', 'USER', '2026-04-03 14:10:00', '2026-06-07 10:00:00'),
    (@invitee_c_id, 'demo-dist-invitee-c-2063223234097961003', NULL, '机器狗训练营', '/static/images/default-avatar.svg', 'USER', '2026-05-16 11:40:00', '2026-06-07 10:00:00')
ON DUPLICATE KEY UPDATE
    nickname = VALUES(nickname),
    avatar_url = VALUES(avatar_url),
    role = VALUES(role),
    updated_at = VALUES(updated_at);

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
     599000, 0, 0, 0, 599000, NULL, 'seed-dist-order-a', '2026-03-25 11:00:00', '2026-04-01 15:00:00'),
    (2063223234097976102, 'XQ202604180102TEAM', @invitee_b_id, 9002, 'BUY', 'COMPLETED',
     1199000, 0, 0, 0, 1199000, NULL, 'seed-dist-order-b', '2026-04-18 09:30:00', '2026-04-29 16:00:00'),
    (2063223234097976103, 'XQ202606030103TEAM', @invitee_a_id, 9003, 'BUY', 'PAID',
     999900, 0, 0, 0, 999900, NULL, 'seed-dist-order-c', '2026-06-03 14:20:00', '2026-06-03 14:25:00'),
    (2063223234097976104, 'XQ202605200104TEAM', @invitee_c_id, 9001, 'BUY', 'COMPLETED',
     560000, 0, 0, 0, 560000, NULL, 'seed-dist-order-d', '2026-05-20 10:12:00', '2026-05-27 18:30:00'),
    (2063223234097976105, 'XQ202605250105TEAM', @invitee_b_id, 9012, 'BUY', 'COMPLETED',
     252000, 0, 0, 0, 252000, NULL, 'seed-dist-order-e', '2026-05-25 15:18:00', '2026-05-25 18:00:00'),
    (2063223234097976106, 'XQ202606040106TEAM', @invitee_a_id, 9004, 'BUY', 'COMPLETED',
     176000, 0, 0, 0, 176000, NULL, 'seed-dist-order-f', '2026-06-04 13:05:00', '2026-06-05 09:10:00')
ON DUPLICATE KEY UPDATE
    user_id = VALUES(user_id),
    sku_id = VALUES(sku_id),
    order_type = VALUES(order_type),
    status = VALUES(status),
    amount_minor = VALUES(amount_minor),
    payable_minor = VALUES(payable_minor),
    updated_at = VALUES(updated_at);

INSERT INTO order_lines
    (id, order_id, sku_id, sku_name, quantity, unit_price_minor)
VALUES
    (2063223234097976201, 2063223234097976101, 9001, 'Unitree Go2 Edu Founder Pack', 1, 599000),
    (2063223234097976202, 2063223234097976102, 9002, 'Unitree B2 Inspection Suite', 1, 1199000),
    (2063223234097976203, 2063223234097976103, 9003, 'Unitree G1 Showcase Package', 1, 999900),
    (2063223234097976204, 2063223234097976104, 9001, 'Unitree Go2 Edu Venue Bundle', 1, 560000),
    (2063223234097976205, 2063223234097976105, 9012, 'Unitree Official Accessory Pack', 1, 252000),
    (2063223234097976206, 2063223234097976106, 9004, 'Unitree Training Camp Package', 1, 176000)
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
    settled_at = VALUES(settled_at),
    created_at = VALUES(created_at);

INSERT INTO commission_settlement_batches
    (id, batch_no, settled_at, total_entries, total_amount_minor)
VALUES
    (2063223234097976401, 'SETTLE-20260402-DIST', '2026-04-02 09:00:00', 1, 29950),
    (2063223234097976402, 'SETTLE-20260426-DIST', '2026-04-26 09:00:00', 1, 59950),
    (2063223234097976403, 'SETTLE-20260528-DIST', '2026-05-28 09:00:00', 1, 16800)
ON DUPLICATE KEY UPDATE
    settled_at = VALUES(settled_at),
    total_entries = VALUES(total_entries),
    total_amount_minor = VALUES(total_amount_minor);

INSERT INTO wallets
    (id, user_id, balance_minor, frozen_minor, created_at, updated_at)
VALUES
    (2063223234097977001, @target_user_id, 70700, 26000, '2026-04-02 09:00:00', '2026-06-06 10:00:00')
ON DUPLICATE KEY UPDATE
    balance_minor = VALUES(balance_minor),
    frozen_minor = VALUES(frozen_minor),
    updated_at = VALUES(updated_at);

INSERT INTO wallet_ledger
    (id, user_id, type, amount_minor, ref_type, ref_id, balance_after_minor, description, created_at)
VALUES
    (2063223234097977101, @target_user_id, 'CREDIT', 29950, 'COMMISSION',
     '2063223234097976301', 29950, 'Direct commission settled for XQ202603250101TEAM', '2026-04-02 09:00:00'),
    (2063223234097977102, @target_user_id, 'CREDIT', 59950, 'COMMISSION',
     '2063223234097976302', 89900, 'Direct commission settled for XQ202604180102TEAM', '2026-04-26 09:00:00'),
    (2063223234097977103, @target_user_id, 'CREDIT', 16800, 'COMMISSION',
     '2063223234097976304', 106700, 'Second-level commission settled for XQ202605200104TEAM', '2026-05-28 09:00:00'),
    (2063223234097977104, @target_user_id, 'FREEZE', 12000, 'WITHDRAW',
     '2063223234097977202', 94700, 'Withdraw request frozen before failure', '2026-05-29 09:12:00'),
    (2063223234097977105, @target_user_id, 'UNFREEZE', 12000, 'WITHDRAW',
     '2063223234097977202', 106700, 'Withdraw request failed and returned', '2026-05-29 09:25:00'),
    (2063223234097977106, @target_user_id, 'FREEZE', 36000, 'WITHDRAW',
     '2063223234097977203', 70700, 'Withdraw request frozen before transfer', '2026-05-30 11:05:00'),
    (2063223234097977107, @target_user_id, 'DEBIT', 36000, 'WITHDRAW',
     '2063223234097977203', 70700, 'Withdraw success to WeChat wallet', '2026-05-30 11:18:00'),
    (2063223234097977108, @target_user_id, 'FREEZE', 26000, 'WITHDRAW',
     '2063223234097977201', 70700, 'Withdraw request processing', '2026-06-06 10:00:00')
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
    (2063223234097977201, @target_user_id, 26000, 'PROCESSING', 'DIST-WX-20260606-01',
     NULL, 'seed-dist-withdraw-1', '2026-06-06 10:00:00', '2026-06-06 10:02:00'),
    (2063223234097977202, @target_user_id, 12000, 'FAILED', NULL,
     'Real-name verification mismatch, returned to balance', 'seed-dist-withdraw-2', '2026-05-29 09:12:00', '2026-05-29 09:25:00'),
    (2063223234097977203, @target_user_id, 36000, 'SUCCESS', 'DIST-WX-20260530-01',
     NULL, 'seed-dist-withdraw-3', '2026-05-30 11:05:00', '2026-05-30 11:18:00')
ON DUPLICATE KEY UPDATE
    amount_minor = VALUES(amount_minor),
    status = VALUES(status),
    wx_batch_id = VALUES(wx_batch_id),
    fail_reason = VALUES(fail_reason),
    updated_at = VALUES(updated_at);

COMMIT;

SELECT
    @target_user_id AS user_id,
    (SELECT COUNT(*) FROM invite_relations WHERE inviter_user_id = @target_user_id AND level = 1) AS level1_count,
    (SELECT COUNT(*) FROM invite_relations WHERE inviter_user_id = @target_user_id AND level = 2) AS level2_count,
    (SELECT COUNT(DISTINCT invitee_user_id) FROM invite_relations WHERE inviter_user_id = @target_user_id) AS total_invite_count,
    (SELECT COALESCE(SUM(amount_minor), 0) FROM commission_entries WHERE beneficiary_user_id = @target_user_id AND status IN ('PENDING_PROTECT', 'SETTLEABLE')) AS pending_commission_minor,
    (SELECT COALESCE(SUM(amount_minor), 0) FROM commission_entries WHERE beneficiary_user_id = @target_user_id AND status = 'SETTLED') AS settled_commission_minor,
    (SELECT COALESCE(SUM(amount_minor), 0) FROM withdraw_requests WHERE user_id = @target_user_id AND status = 'SUCCESS') AS withdrawn_commission_minor,
    (SELECT COALESCE(SUM(amount_minor), 0) FROM commission_entries WHERE beneficiary_user_id = @target_user_id AND status = 'FROZEN') AS dispute_commission_minor;
