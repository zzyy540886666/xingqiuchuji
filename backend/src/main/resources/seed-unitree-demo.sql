-- Clean Unitree demo data for product purchase, software services, user center,
-- admin management, community feed and mini-program profile pages.
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- Old local databases may predate the current community model. These ALTERs are
-- intentionally idempotent via spring.sql.init.continue-on-error=true.
ALTER TABLE posts ADD COLUMN collect_count INT DEFAULT 0 AFTER like_count;
ALTER TABLE posts ADD COLUMN deleted INT DEFAULT 0;
ALTER TABLE posts ADD COLUMN circle_id BIGINT;
ALTER TABLE posts ADD COLUMN topic_id BIGINT;
ALTER TABLE comments ALTER COLUMN status SET DEFAULT 'ACTIVE';

CREATE TABLE IF NOT EXISTS post_likes (
    id BIGINT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_user (post_id, user_id),
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS post_collects (
    id BIGINT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_user (post_id, user_id),
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_follows (
    id BIGINT PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    followee_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_follower_followee (follower_id, followee_id),
    INDEX idx_follower_id (follower_id),
    INDEX idx_followee_id (followee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO brands (id, name, logo_url, sort_order, created_at, updated_at) VALUES
(1, '瀹囨爲绉戞妧', '', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), logo_url = VALUES(logo_url), updated_at = NOW();

INSERT INTO scenes (id, name, description, image_url, sort_order) VALUES
(10, '涔版満鍣ㄤ汉', '瀹囨爲鍥涜冻鏈哄櫒浜恒€佷汉褰㈡満鍣ㄤ汉鏁存満閲囪喘涓庝氦浠樸€?, '', 10),
(11, '杞欢绋嬪簭', '鏈哄櫒浜烘帶鍒剁▼搴忋€佷簯绔皟搴︺€佷豢鐪熻缁冨拰杩滅▼杩愮淮杞欢銆?, '', 11),
(12, '鎵樼鏀剁泭', '璐拱鍚庣殑鏈哄櫒浜鸿祫浜ф墭绠°€佸嚭绉熷拰鏀剁泭缁撶畻銆?, '', 12)
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), image_url = VALUES(image_url), sort_order = VALUES(sort_order);

INSERT INTO catalog_filter_options (id, group_id, label, value, min_price_minor, max_price_minor, sort_order, enabled) VALUES
(90, 4, '涔版満鍣ㄤ汉', '涔版満鍣ㄤ汉', NULL, NULL, 90, 1),
(91, 4, '杞欢绋嬪簭', '杞欢绋嬪簭', NULL, NULL, 91, 1)
ON DUPLICATE KEY UPDATE label = VALUES(label), value = VALUES(value), enabled = VALUES(enabled);

INSERT INTO skus
(id, name, type, brand_id, model_id, description, specs_json, subtitle, original_price_minor, adapted_scenes_text, stock_status_text, delivery_text, status, stock, created_at, updated_at) VALUES
(9001, 'Unitree Go2 Edu 鍥涜冻鏈哄櫒浜洪噰璐瑁?, 'BUY', 1, 2, '闈㈠悜楂樻牎瀹為獙瀹ゃ€佸睍鍘呭拰浼佷笟宸℃楠岃瘉鐨勫畤鏍?Go2 Edu 閲囪喘濂楄锛屽惈鏈綋銆侀仴鎺у櫒銆佺數姹犵粍銆佸厖鐢靛櫒鍜屽紑绠卞煿璁€?, '{"閲嶉噺":"绾?5kg","缁埅":"2-4灏忔椂","鏈€楂橀€熷害":"绾?.5m/s","浼犳劅鍣?:"4D 婵€鍏夐浄杈俱€侀奔鐪肩浉鏈恒€両MU","寮€鏀捐兘鍔?:"鏀寔 SDK 涓庝簩娆″紑鍙?}', '涔版満鍣ㄤ汉 路 鐜拌揣閲囪喘 路 鏁欒偛绉戠爺棣栭€?, 599000, '绉戠爺鏁欒偛銆佸睍鍘呮紨绀恒€佸贰妫€绠楁硶楠岃瘉銆佹満鍣ㄤ汉璇剧▼', '鐜拌揣 18 鍙?, '鍏ㄥ浗鍙彂锛? 涓伐浣滄棩鍐呬氦浠橈紝鏀寔鐜板満楠屾敹', 'ONLINE', 18, '2026-06-01 09:00:00', NOW()),
(9002, 'Unitree B2 宸ヤ笟鍥涜冻鏈哄櫒浜洪噰璐瑁?, 'BUY', 1, 12, '闈㈠悜鍥尯銆佺數鍔涖€佸伐鍘傚拰澶嶆潅鍦板舰宸℃鐨勫畤鏍?B2 宸ヤ笟绾ч噰璐瑁咃紝寮鸿皟璐熻浇銆佺画鑸拰鍏ㄥぉ鍊欑ǔ瀹氳繍琛屻€?, '{"閲嶉噺":"绾?0kg","璐熻浇":"鏈€楂?0kg","缁埅":"4-6灏忔椂","闃叉姢":"宸ヤ笟绾ч槻鎶?,"閫傞厤":"鐑垚鍍忋€佹皵浣撲紶鎰熷櫒銆丷TK 妯″潡"}', '涔版満鍣ㄤ汉 路 宸ヤ笟宸℃ 路 楂樿礋杞介暱缁埅', 1599900, '鐢靛姏宸℃銆佸伐鍘傚贰妫€銆佸洯鍖哄畨闃层€佸簲鎬ュ嫎瀵?, '鍙璁?6 鍙?, '15 涓伐浣滄棩鍐呬氦浠橈紝鍚幇鍦洪儴缃茶皟璇?, 'ONLINE', 6, '2026-06-01 09:10:00', NOW()),
(9003, 'Unitree G1 浜哄舰鏈哄櫒浜洪噰璐瑁?, 'BUY', 1, 1, '閫傚悎绉戠爺銆佸睍绀哄拰鍏疯韩鏅鸿兘搴旂敤楠岃瘉鐨勫畤鏍?G1 浜哄舰鏈哄櫒浜洪噰璐瑁咃紝鏀寔杩愬姩鎺у埗銆佷氦浜掓紨绀哄拰浜屾寮€鍙戙€?, '{"韬珮":"绾?27cm","閲嶉噺":"绾?5kg","鑷敱搴?:"23+","缁埅":"绾?灏忔椂","寮€鏀捐兘鍔?:"寮€鍙戞帴鍙ｃ€佸姩浣滃簱銆佽皟璇曞伐鍏?}', '涔版満鍣ㄤ汉 路 浜哄舰鏈哄櫒浜?路 鍏疯韩鏅鸿兘鐮斿彂骞冲彴', 999900, '鍏疯韩鏅鸿兘鐮斿彂銆佸睍鍘呰瑙ｃ€佸姩浣滄紨绀恒€佺畻娉曢獙璇?, '鍙璁?10 鍙?, '25 涓伐浣滄棩鍐呬氦浠橈紝鍚繙绋嬫妧鏈敮鎸?, 'ONLINE', 10, '2026-06-01 09:20:00', NOW()),
(9010, 'Unitree Robot SDK 涓撲笟鐗?, 'SOFTWARE', 1, NULL, '闈㈠悜瀹囨爲鏈哄櫒浜轰簩娆″紑鍙戠殑杞欢绋嬪簭鍖咃紝鎻愪緵 C++/Python API銆丷OS/ROS2 鎺ュ彛銆佷紶鎰熷櫒鏁版嵁璇诲彇鍜岃繍鍔ㄦ帶鍒剁ず渚嬨€?, '{"鎺堟潈":"3骞?,"璇█":"C++ / Python","骞冲彴":"Linux / Windows","鎺ュ彛":"ROS銆丷OS2銆乄ebSocket","甯綅":"5鍙拌澶?}', '杞欢绋嬪簭 路 SDK 涓撲笟鐗?路 浜屾寮€鍙戝繀澶?, 89900, '鏈哄櫒浜哄紑鍙戙€佽绋嬪疄楠屻€佽繍鍔ㄦ帶鍒躲€佷紶鎰熷櫒铻嶅悎', '鍗虫椂寮€閫?, '浠樻鍚?1 涓伐浣滄棩鍐呭紑閫氭巿鏉?, 'ONLINE', 999, '2026-06-01 10:00:00', NOW()),
(9011, 'Unitree CloudFleet 浜戠璋冨害绯荤粺', 'SOFTWARE', 1, NULL, '閫傜敤浜庡鍙板畤鏍戞満鍣ㄤ汉闆嗙兢浣滀笟鐨勮蒋浠剁▼搴忥紝鏀寔浠诲姟娲惧彂銆佽矾绾块厤缃€佽澶囩姸鎬佺洃鎺с€佹棩蹇楀洖鏀惧拰寮傚父鍛婅銆?, '{"鎺堟潈":"1骞?,"閮ㄧ讲":"SaaS 鎴栫鏈夊寲","瀹归噺":"鏈€楂?00鍙版満鍣ㄤ汉","鍗忚":"MQTT銆丠TTP銆乄ebSocket","鏁版嵁鐣欏瓨":"90澶?}', '杞欢绋嬪簭 路 浜戠璋冨害 路 澶氭満闆嗙兢绠＄悊', 599900, '鍥尯宸℃銆佸伐鍘傝繍缁淬€佸鏈轰换鍔¤皟搴︺€佽繍钀ョ湅鏉?, '鏂规鍒朵氦浠?, '7 涓伐浣滄棩鍐呭畬鎴愮鎴峰紑閫氾紝绉佹湁鍖栧彟琛屾帓鏈?, 'ONLINE', 100, '2026-06-01 10:10:00', NOW()),
(9012, 'Unitree SimLab 浠跨湡璁粌骞冲彴', 'SOFTWARE', 1, NULL, '鎻愪緵瀹囨爲鏈哄櫒浜洪珮淇濈湡妯″瀷銆佽缁冨満鏅€佽建杩瑰洖鏀惧拰绠楁硶璇勪及鑳藉姏锛岄€傚悎鏁欏銆佷豢鐪熼楠岃瘉鍜屽己鍖栧涔犺缁冦€?, '{"鎺堟潈":"1骞?,"寮曟搸":"Isaac Sim 鍏煎","鍦烘櫙":"15涓缃満鏅?,"妯″瀷":"Go2銆丅2銆丟1","鑳藉姏":"杞ㄨ抗鍥炴斁銆佹寚鏍囪瘎浼般€佸満鏅紪杈?}', '杞欢绋嬪簭 路 浠跨湡璁粌 路 绠楁硶棰勯獙璇?, 149900, '浠跨湡璁粌銆佺畻娉曠珵璧涖€佽绋嬪疄楠屻€佹柟妗堟紨绀?, '鍗虫椂寮€閫?, '3 涓伐浣滄棩鍐呭畬鎴愰儴缃叉寚瀵?, 'ONLINE', 999, '2026-06-01 10:20:00', NOW())
ON DUPLICATE KEY UPDATE
name = VALUES(name), type = VALUES(type), description = VALUES(description), specs_json = VALUES(specs_json),
subtitle = VALUES(subtitle), original_price_minor = VALUES(original_price_minor),
adapted_scenes_text = VALUES(adapted_scenes_text), stock_status_text = VALUES(stock_status_text),
delivery_text = VALUES(delivery_text), status = VALUES(status), stock = VALUES(stock), updated_at = NOW();

INSERT IGNORE INTO sku_media (id, sku_id, url, type, sort_order) VALUES
(9001, 9001, '', 'IMAGE', 0),
(9002, 9002, '', 'IMAGE', 0),
(9003, 9003, '', 'IMAGE', 0),
(9010, 9010, '', 'IMAGE', 0),
(9011, 9011, '', 'IMAGE', 0),
(9012, 9012, '', 'IMAGE', 0);

INSERT INTO sku_prices (id, sku_id, price_type, price_minor, min_duration, max_duration, daily_rate_minor) VALUES
(9001, 9001, 'BUY', 599000, 1, 1, 0),
(9002, 9002, 'BUY', 1599900, 1, 1, 0),
(9003, 9003, 'BUY', 999900, 1, 1, 0),
(9010, 9010, 'SUBSCRIPTION', 89900, 365, 1095, 0),
(9011, 9011, 'SUBSCRIPTION', 599900, 365, 1095, 0),
(9012, 9012, 'SUBSCRIPTION', 149900, 365, 1095, 0)
ON DUPLICATE KEY UPDATE price_type = VALUES(price_type), price_minor = VALUES(price_minor), min_duration = VALUES(min_duration), max_duration = VALUES(max_duration), daily_rate_minor = VALUES(daily_rate_minor);

INSERT IGNORE INTO sku_tags (id, sku_id, name, sort_order) VALUES
(9001, 9001, '涔版満鍣ㄤ汉', 1), (9002, 9001, 'Go2 Edu', 2), (9003, 9001, '绉戠爺鏁欒偛', 3),
(9004, 9002, '涔版満鍣ㄤ汉', 1), (9005, 9002, '宸ヤ笟宸℃', 2), (9006, 9002, 'B2', 3),
(9007, 9003, '涔版満鍣ㄤ汉', 1), (9008, 9003, '浜哄舰鏈哄櫒浜?, 2), (9009, 9003, 'G1', 3),
(9010, 9010, '杞欢绋嬪簭', 1), (9011, 9010, 'SDK', 2), (9012, 9010, 'ROS2', 3),
(9013, 9011, '杞欢绋嬪簭', 1), (9014, 9011, '浜戠璋冨害', 2), (9015, 9011, '澶氭満绠＄悊', 3),
(9016, 9012, '杞欢绋嬪簭', 1), (9017, 9012, '浠跨湡璁粌', 2), (9018, 9012, '绠楁硶楠岃瘉', 3);

INSERT IGNORE INTO sku_services (id, sku_id, name, price_label, sort_order) VALUES
(9001, 9001, '寮€绠遍獙鏀朵笌鍩虹鍩硅', '鍏嶈垂', 1),
(9002, 9001, '涓€骞村師鍘傝川淇?, '鍚湪鍞环鍐?, 2),
(9003, 9002, '鐜板満閮ㄧ讲涓庤矾绾胯皟璇?, '鍏嶈垂', 1),
(9004, 9002, '宸ヤ笟浼犳劅鍣ㄩ€傞厤鍜ㄨ', '鎸夋柟妗堟姤浠?, 2),
(9005, 9003, '鍔ㄤ綔搴撲笌寮€鍙戠幆澧冨垵濮嬪寲', '鍏嶈垂', 1),
(9006, 9003, '杩滅▼鎶€鏈敮鎸?, '涓€骞村厤璐?, 2),
(9010, 9010, 'API 鏂囨。涓庣ず渚嬪伐绋?, '鍏嶈垂', 1),
(9011, 9010, '寮€鍙戣€呭伐鍗曟敮鎸?, '涓€骞村厤璐?, 2),
(9012, 9011, '璋冨害骞冲彴绉熸埛寮€閫?, '鍏嶈垂', 1),
(9013, 9011, '绉佹湁鍖栭儴缃茶瘎浼?, '鎸夋柟妗堟姤浠?, 2),
(9014, 9012, '瀹夎閮ㄧ讲鎸囧', '鍏嶈垂', 1),
(9015, 9012, '浠跨湡鍦烘櫙瀹氬埗', '鎸夋柟妗堟姤浠?, 2);

INSERT IGNORE INTO sku_detail_sections (id, sku_id, title, content, sort_order) VALUES
(9001, 9001, '閲囪喘璇存槑', '閫傚悎楂樻牎銆佸疄楠屽鍜屽睍鍘呭揩閫熼噰璐畤鏍?Go2 Edu锛屽钩鍙版彁渚涘悎鍚屻€佸彂绁ㄣ€佺墿娴佸拰楠屾敹鍗忓姪銆?, 1),
(9002, 9002, '宸ヤ笟浜や粯璇存槑', 'B2 宸ヤ笟濂楄鎸夐」鐩氦浠橈紝寤鸿鍦ㄩ噰璐墠纭宸℃璺嚎銆佷紶鎰熷櫒銆佺綉缁滆鐩栧拰鐜板満瀹夊叏杈圭晫銆?, 1),
(9003, 9003, '浜哄舰鏈哄櫒浜虹爺鍙戣鏄?, 'G1 閫傚悎鍏疯韩鏅鸿兘鐮斿彂鍜屽睍绀猴紝浜や粯鍚庡彲閫氳繃 SDK銆佸姩浣滃簱鍜岃繙绋嬫妧鏈敮鎸佸紑灞曚簩娆″紑鍙戙€?, 1),
(9010, 9010, '杞欢绋嬪簭璇存槑', 'SDK 涓撲笟鐗堥€傚悎浜屾寮€鍙戙€佽绋嬪疄楠屽拰鎺у埗绠楁硶楠岃瘉锛屾巿鏉冧俊鎭互鍚庡彴璁㈠崟涓庤蒋浠跺紑閫氳褰曚负鍑嗐€?, 1),
(9011, 9011, '璋冨害骞冲彴璇存槑', 'CloudFleet 鏀寔澶氭満鍦ㄧ嚎銆佷换鍔℃淳鍙戙€佹棩蹇楀洖鏀惧拰寮傚父鍛婅锛屽彲涓庢姤淇拰鎵樼鏀剁泭鍦烘櫙鑱斿姩銆?, 1),
(9012, 9012, '浠跨湡骞冲彴璇存槑', 'SimLab 鐢ㄤ簬绠楁硶涓婄嚎鍓嶇殑浠跨湡棰勯獙璇侊紝闄嶄綆鐪熷疄璁惧璋冭瘯椋庨櫓銆?, 1);

INSERT IGNORE INTO scene_sku_rel (id, scene_id, sku_id) VALUES
(9001, 10, 9001), (9002, 10, 9002), (9003, 10, 9003),
(9010, 11, 9010), (9011, 11, 9011), (9012, 11, 9012);

INSERT INTO app_configs (id, config_key, value_json, version, description) VALUES
(9030, 'recommended_skus', '[9001,9002,9003,9010,9011,9012]', 2, '瀹囨爲涔版満鍣ㄤ汉鍜岃蒋浠剁▼搴忔帹鑽愬晢鍝?),
(9031, 'hot_keywords', '["涔版満鍣ㄤ汉","杞欢绋嬪簭","Unitree Go2","Unitree G1","鎵樼鏀剁泭","宸ヤ笟宸℃"]', 2, '瀹囨爲涓氬姟鎼滅储鐑瘝')
ON DUPLICATE KEY UPDATE value_json = VALUES(value_json), version = VALUES(version), description = VALUES(description);

INSERT INTO users (id, openid, unionid, nickname, avatar_url, role, created_at, updated_at) VALUES
(91001, 'unitree-demo-owner-openid', 'unitree-demo-owner-unionid', '瀹囨爲璧勪骇鎵樼鐢ㄦ埛', '/static/images/default-avatar.svg', 'USER', '2026-06-01 09:00:00', NOW()),
(91002, 'unitree-demo-school-openid', 'unitree-demo-school-unionid', '鏉窞瀹為獙瀹ら檲鑰佸笀', '/static/images/default-avatar.svg', 'USER', '2026-06-01 10:00:00', NOW()),
(91003, 'unitree-demo-ops-openid', 'unitree-demo-ops-unionid', '鍗楀北宸ュ巶杩愮淮鏉庡伐', '/static/images/default-avatar.svg', 'TECHNICIAN', '2026-06-01 11:00:00', NOW())
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), avatar_url = VALUES(avatar_url), role = VALUES(role), updated_at = NOW();

INSERT INTO user_membership (id, user_id, level, is_native, lifetime_spend_minor, planet_card_expires_at, created_at, updated_at) VALUES
(91001, 91001, 3, 1, 2689700, '2027-06-01 23:59:59', '2026-06-01 09:10:00', NOW()),
(91002, 91002, 2, 0, 688900, '2027-03-01 23:59:59', '2026-06-01 10:20:00', NOW()),
(91003, 91003, 1, 0, 199900, '2026-12-01 23:59:59', '2026-06-01 11:20:00', NOW())
ON DUPLICATE KEY UPDATE level = VALUES(level), is_native = VALUES(is_native), lifetime_spend_minor = VALUES(lifetime_spend_minor), planet_card_expires_at = VALUES(planet_card_expires_at), updated_at = NOW();

INSERT INTO membership_benefit_grants (id, user_id, benefit_type, total_count, used_count, expires_at) VALUES
(91001, 91001, 'FREE_DEPOSIT', 3, 1, '2027-06-01 23:59:59'),
(91002, 91001, 'ONSITE_TRAINING', 2, 0, '2027-06-01 23:59:59'),
(91003, 91001, 'SOFTWARE_SUPPORT', 6, 1, '2027-06-01 23:59:59'),
(91004, 91002, 'FREE_DEPOSIT', 2, 0, '2027-03-01 23:59:59')
ON DUPLICATE KEY UPDATE total_count = VALUES(total_count), used_count = VALUES(used_count), expires_at = VALUES(expires_at);

INSERT INTO wallets (id, user_id, balance_minor, frozen_minor, created_at, updated_at) VALUES
(91001, 91001, 2368800, 120000, '2026-06-01 09:15:00', NOW()),
(91002, 91002, 386000, 0, '2026-06-01 10:15:00', NOW()),
(91003, 91003, 126800, 0, '2026-06-01 11:15:00', NOW())
ON DUPLICATE KEY UPDATE balance_minor = VALUES(balance_minor), frozen_minor = VALUES(frozen_minor), updated_at = NOW();

INSERT INTO wallet_ledger (id, user_id, type, amount_minor, ref_type, ref_id, balance_after_minor, description, created_at) VALUES
(91001, 91001, 'CREDIT', 1680000, 'ASSET_REVENUE', '91001', 1680000, 'Unitree Go2 鎵樼鍑虹鏀剁泭缁撶畻', '2026-06-02 12:00:00'),
(91002, 91001, 'CREDIT', 588800, 'COMMISSION', '91001', 2268800, '鎺ㄨ崘鏉窞瀹為獙瀹よ喘涔?G1 鐨勫垎閿€浣ｉ噾', '2026-06-03 12:00:00'),
(91003, 91001, 'CREDIT', 220000, 'REFUND', '91003', 2488800, 'G1 杞欢璋冭瘯淇濊瘉閲戦噴鏀?, '2026-06-04 16:00:00'),
(91004, 91001, 'DEBIT', 100000, 'WITHDRAW', 'WD91001', 2388800, '鎻愮幇鍒板井淇￠浂閽?, '2026-06-05 09:00:00'),
(91005, 91002, 'CREDIT', 386000, 'RECHARGE', 'RC91002', 386000, '瀹為獙瀹ら」鐩挶鍖呭厖鍊?, '2026-06-02 09:00:00'),
(91006, 91003, 'CREDIT', 126800, 'COMMISSION', '91002', 126800, '宸ュ巶宸℃椤圭洰浜岀骇鍒嗛攢浣ｉ噾', '2026-06-04 12:00:00')
ON DUPLICATE KEY UPDATE amount_minor = VALUES(amount_minor), balance_after_minor = VALUES(balance_after_minor), description = VALUES(description);

INSERT INTO withdraw_requests (id, user_id, amount_minor, status, wx_batch_id, fail_reason, idempotency_key, created_at, updated_at) VALUES
(91001, 91001, 100000, 'PROCESSING', 'WXBATCH91001', NULL, 'wd-unitree-91001-20260605', '2026-06-05 09:00:00', NOW()),
(91002, 91003, 68000, 'SUCCESS', 'WXBATCH91002', NULL, 'wd-unitree-91003-20260604', '2026-06-04 09:30:00', '2026-06-04 10:00:00')
ON DUPLICATE KEY UPDATE amount_minor = VALUES(amount_minor), status = VALUES(status), wx_batch_id = VALUES(wx_batch_id), updated_at = NOW();

INSERT INTO orders
(id, order_no, user_id, sku_id, order_type, status, amount_minor, deposit_minor, shipping_minor, discount_minor, payable_minor, rent_start_date, rent_end_date, address_json, idempotency_key, created_at, updated_at) VALUES
(91001, 'XQ202606010001', 91001, 9001, 'BUY', 'COMPLETED', 599000, 0, 0, 0, 599000, NULL, NULL, '{"receiver":"瀹囨爲璧勪骇鎵樼鐢ㄦ埛","city":"鏉窞","address":"鏈潵绉戞妧鍩庢満鍣ㄤ汉瀹為獙瀹?}', 'seed-order-91001', '2026-06-01 14:00:00', '2026-06-01 16:00:00'),
(91002, 'XQ202606020001', 91002, 9003, 'BUY', 'FULFILLING', 999900, 0, 0, 20000, 979900, NULL, NULL, '{"receiver":"鏉窞瀹為獙瀹ら檲鑰佸笀","city":"鏉窞","address":"瑗挎箹鍖洪珮鏍¤仈鍚堝疄楠屾ゼ"}', 'seed-order-91002', '2026-06-02 10:30:00', '2026-06-03 09:20:00'),
(91003, 'XQ202606030001', 91001, 9011, 'SOFTWARE', 'COMPLETED', 599900, 0, 0, 0, 599900, NULL, NULL, '{"receiver":"瀹囨爲璧勪骇鎵樼鐢ㄦ埛","city":"娣卞湷","address":"鍗楀北宸ュ巶宸℃椤圭洰缁?}', 'seed-order-91003', '2026-06-03 11:00:00', '2026-06-03 12:30:00'),
(91004, 'XQ202606040001', 91003, 9002, 'BUY', 'FULFILLING', 1599900, 0, 0, 0, 1599900, NULL, NULL, '{"receiver":"鍗楀北宸ュ巶杩愮淮鏉庡伐","city":"娣卞湷","address":"鍗楀北鏅洪€犲洯 B1 宸ュ巶"}', 'seed-order-91004', '2026-06-04 15:00:00', '2026-06-04 15:00:00')
ON DUPLICATE KEY UPDATE status = VALUES(status), amount_minor = VALUES(amount_minor), payable_minor = VALUES(payable_minor), updated_at = NOW();

INSERT INTO order_lines (id, order_id, sku_id, sku_name, quantity, unit_price_minor) VALUES
(91001, 91001, 9001, 'Unitree Go2 Edu 鍥涜冻鏈哄櫒浜洪噰璐瑁?, 1, 599000),
(91002, 91002, 9003, 'Unitree G1 浜哄舰鏈哄櫒浜洪噰璐瑁?, 1, 999900),
(91003, 91003, 9011, 'Unitree CloudFleet 浜戠璋冨害绯荤粺', 1, 599900),
(91004, 91004, 9002, 'Unitree B2 宸ヤ笟鍥涜冻鏈哄櫒浜洪噰璐瑁?, 1, 1599900)
ON DUPLICATE KEY UPDATE sku_name = VALUES(sku_name), quantity = VALUES(quantity), unit_price_minor = VALUES(unit_price_minor);

INSERT IGNORE INTO order_events (id, order_id, from_status, to_status, operator, reason, created_at) VALUES
(91001, 91001, NULL, 'PENDING_PAY', 'USER:91001', '鍒涘缓涔版満鍣ㄤ汉璁㈠崟', '2026-06-01 14:00:00'),
(91002, 91001, 'PENDING_PAY', 'COMPLETED', 'SYSTEM', '鏀粯瀹屾垚骞剁‘璁ゆ墭绠?, '2026-06-01 16:00:00'),
(91003, 91003, NULL, 'PENDING_PAY', 'USER:91001', '鍒涘缓杞欢绋嬪簭璁㈠崟', '2026-06-03 11:00:00'),
(91004, 91003, 'PENDING_PAY', 'COMPLETED', 'SYSTEM', '杞欢鎺堟潈寮€閫?, '2026-06-03 12:30:00');

INSERT INTO user_assets (id, user_id, name, type, model_info, image_url, status, purchase_order_id, acquired_at, created_at) VALUES
(91001, 91001, 'Unitree Go2 Edu 鎵樼璧勪骇 A', 'ROBOT', 'Go2 Edu / SN-GO2-91001', '', 'TRUSTEED', '91001', '2026-06-01 16:00:00', '2026-06-01 16:00:00'),
(91002, 91001, 'Unitree G1 灞曠ず璧勪骇 B', 'ROBOT', 'G1 / SN-G1-91002', '', 'IDLE', '91002', '2026-06-03 09:20:00', '2026-06-03 09:20:00'),
(91003, 91003, 'Unitree B2 宸ュ巶宸℃璧勪骇 C', 'ROBOT', 'B2 / SN-B2-91003', '', 'MAINTENANCE', '91004', '2026-06-04 15:30:00', '2026-06-04 15:30:00')
ON DUPLICATE KEY UPDATE name = VALUES(name), model_info = VALUES(model_info), image_url = VALUES(image_url), status = VALUES(status);

INSERT INTO trusteeship_slots (id, asset_id, user_id, start_time, end_time, status, daily_rate_minor, total_revenue_minor, created_at) VALUES
(91001, 91001, 91001, '2026-06-02 00:00:00', '2026-07-02 23:59:59', 'ACTIVE', 68000, 1680000, '2026-06-01 17:00:00'),
(91002, 91002, 91001, '2026-06-05 00:00:00', '2026-06-20 23:59:59', 'ACTIVE', 98000, 0, '2026-06-04 18:00:00')
ON DUPLICATE KEY UPDATE status = VALUES(status), daily_rate_minor = VALUES(daily_rate_minor), total_revenue_minor = VALUES(total_revenue_minor);

INSERT INTO asset_revenue_ledger (id, asset_id, user_id, order_id, amount_minor, status, created_at) VALUES
(91001, 91001, 91001, 91001, 680000, 'SETTLED', '2026-06-02 12:00:00'),
(91002, 91001, 91001, 91003, 980000, 'SETTLED', '2026-06-03 12:00:00'),
(91003, 91002, 91001, 91002, 360000, 'PENDING_SETTLE', '2026-06-05 12:00:00')
ON DUPLICATE KEY UPDATE amount_minor = VALUES(amount_minor), status = VALUES(status);

INSERT INTO invite_relations (id, inviter_user_id, invitee_user_id, level, bound_at, source) VALUES
(91001, 91001, 91002, 1, '2026-06-01 10:05:00', 'UNITREE_SHARE_POSTER'),
(91002, 91002, 91003, 1, '2026-06-01 11:05:00', 'UNITREE_SOFTWARE_DEMO')
ON DUPLICATE KEY UPDATE source = VALUES(source), bound_at = VALUES(bound_at);

INSERT INTO commission_entries (id, order_id, beneficiary_user_id, source_user_id, level, amount_minor, rate_percent, status, protect_until, settled_at, created_at) VALUES
(91001, 91002, 91001, 91002, 1, 49995, 5, 'SETTLED', '2026-06-05 23:59:59', '2026-06-06 12:00:00', '2026-06-02 12:00:00'),
(91002, 91004, 91002, 91003, 1, 79995, 5, 'PENDING_PROTECT', '2026-06-11 23:59:59', NULL, '2026-06-04 15:30:00'),
(91003, 91004, 91001, 91003, 2, 47997, 3, 'PENDING_PROTECT', '2026-06-11 23:59:59', NULL, '2026-06-04 15:30:00')
ON DUPLICATE KEY UPDATE amount_minor = VALUES(amount_minor), status = VALUES(status), protect_until = VALUES(protect_until), settled_at = VALUES(settled_at);

INSERT INTO commission_settlement_batches (id, batch_no, settled_at, total_entries, total_amount_minor) VALUES
(91001, 'CSB202606060001', '2026-06-06 12:00:00', 1, 49995)
ON DUPLICATE KEY UPDATE settled_at = VALUES(settled_at), total_entries = VALUES(total_entries), total_amount_minor = VALUES(total_amount_minor);

INSERT INTO devices (id, device_no, name, location, area, qr_code, responsible_technician_id, type, model, manufacturer, install_date, status, created_at, updated_at) VALUES
(91001, 'UT-GO2-91001', 'Unitree Go2 Edu 鎵樼璧勪骇 A', '鏉窞鏈潵绉戞妧鍩庢満鍣ㄤ汉瀹為獙瀹?, '鏉窞', 'DEVICE-UT-GO2-91001', 91003, 'ROBOT', 'Go2 Edu', '瀹囨爲绉戞妧', '2026-06-01 16:00:00', 'NORMAL', '2026-06-01 16:00:00', NOW()),
(91002, 'UT-G1-91002', 'Unitree G1 灞曠ず璧勪骇 B', '鏉窞楂樻牎鑱斿悎瀹為獙妤?, '鏉窞', 'DEVICE-UT-G1-91002', 91003, 'ROBOT', 'G1', '瀹囨爲绉戞妧', '2026-06-03 09:20:00', 'NORMAL', '2026-06-03 09:20:00', NOW()),
(91003, 'UT-B2-91003', 'Unitree B2 宸ュ巶宸℃璧勪骇 C', '娣卞湷鍗楀北鏅洪€犲洯 B1 宸ュ巶', '娣卞湷鍗楀北', 'DEVICE-UT-B2-91003', 91003, 'ROBOT', 'B2', '瀹囨爲绉戞妧', '2026-06-04 15:30:00', 'FAULT', '2026-06-04 15:30:00', NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name), location = VALUES(location), status = VALUES(status), updated_at = NOW();

INSERT INTO work_orders (id, order_no, device_id, device_name, reporter_user_id, reporter_name, reporter_phone, fault_type, fault_description, priority, status, assigned_technician_id, images, solution_description, completed_at, created_at, updated_at) VALUES
(91001, 'WO202606050001', 91003, 'Unitree B2 宸ュ巶宸℃璧勪骇 C', 91003, '鍗楀北宸ュ巶杩愮淮鏉庡伐', '138****9103', '鍏宠妭寮傚搷', 'B2 鍦ㄥ闂村贰妫€杞集鏃跺彸鍚庤吙鍏宠妭鍑虹幇寮傚搷锛屽凡鏆傚仠鎵樼宸℃浠诲姟骞剁瓑寰呭伐绋嬪笀鎺掓煡銆?, 'HIGH', 'IN_PROGRESS', 91003, '[""]', NULL, NULL, '2026-06-05 09:30:00', NOW()),
(91002, 'WO202606040001', 91001, 'Unitree Go2 Edu 鎵樼璧勪骇 A', 91001, '瀹囨爲璧勪骇鎵樼鐢ㄦ埛', '139****9101', '缃戠粶寮傚父', 'Go2 鎵樼浠诲姟涓伓鍙戜簯绔皟搴﹀績璺冲欢杩燂紝闇€瑕佺‘璁よ矾鐢卞櫒鍜?CloudFleet 閰嶇疆銆?, 'MEDIUM', 'DONE', 91003, '[""]', '宸叉洿鎹㈢幇鍦虹綉鍏冲苟璋冩暣蹇冭烦閲嶈瘯绛栫暐銆?, '2026-06-04 18:30:00', '2026-06-04 11:00:00', NOW())
ON DUPLICATE KEY UPDATE fault_description = VALUES(fault_description), priority = VALUES(priority), status = VALUES(status), solution_description = VALUES(solution_description), updated_at = NOW();

INSERT IGNORE INTO work_order_logs (id, work_order_id, action, operator_id, operator_name, from_status, to_status, remark, created_at) VALUES
(91001, 91001, 'CREATED', 91003, '鍗楀北宸ュ巶杩愮淮鏉庡伐', NULL, 'NEW', '鐢ㄦ埛鎻愪氦 B2 鍏宠妭寮傚搷鎶ヤ慨', '2026-06-05 09:30:00'),
(91002, 91001, 'ASSIGNED', 1, '鍚庡彴绠＄悊鍛?, 'NEW', 'IN_PROGRESS', '娲惧崟缁欐妧鏈憳鏉庡伐', '2026-06-05 09:45:00'),
(91003, 91002, 'COMPLETED', 91003, '鍗楀北宸ュ巶杩愮淮鏉庡伐', 'IN_PROGRESS', 'DONE', '缃戝叧涓庤皟搴﹂厤缃慨澶嶅畬鎴?, '2026-06-04 18:30:00');

INSERT IGNORE INTO inspection_templates (id, name, description, check_items, created_by, created_at, updated_at) VALUES
(91001, '瀹囨爲鏈哄櫒浜烘瘡鏃ユ墭绠″贰妫€娓呭崟', '鐢ㄤ簬 Go2銆丅2銆丟1 鎵樼璁惧鐨勫熀纭€宸℃銆?, '["鐢垫睜鍋ュ悍","鍏宠妭娓╁害","缃戠粶蹇冭烦","浜戠璋冨害鐘舵€?,"澶栧３涓庝紶鎰熷櫒"]', 1, '2026-06-01 09:00:00', NOW());

INSERT INTO inspection_plans (id, template_id, name, area, frequency, assigned_technician_id, status, next_run_at, created_at, updated_at) VALUES
(91001, 91001, '鏉窞 Go2 鎵樼璧勪骇姣忔棩宸℃', '鏉窞', '姣忔棩', 91003, 'PENDING', '2026-06-07 09:00:00', '2026-06-01 09:00:00', NOW()),
(91002, 91001, '娣卞湷 B2 宸ュ巶宸℃璁惧姣忔棩宸℃', '娣卞湷鍗楀北', '姣忔棩', 91003, 'IN_PROGRESS', '2026-06-07 10:00:00', '2026-06-04 16:00:00', NOW())
ON DUPLICATE KEY UPDATE status = VALUES(status), next_run_at = VALUES(next_run_at), updated_at = NOW();

INSERT INTO inspection_tasks (id, plan_id, template_id, template_name, assigned_technician_id, status, deadline, started_at, completed_at, created_at) VALUES
(91001, 91001, 91001, '瀹囨爲鏈哄櫒浜烘瘡鏃ユ墭绠″贰妫€娓呭崟', 91003, 'PENDING', '2026-06-07 18:00:00', NULL, NULL, '2026-06-06 08:30:00'),
(91002, 91002, 91001, '瀹囨爲鏈哄櫒浜烘瘡鏃ユ墭绠″贰妫€娓呭崟', 91003, 'IN_PROGRESS', '2026-06-07 18:00:00', '2026-06-06 10:00:00', NULL, '2026-06-06 08:40:00')
ON DUPLICATE KEY UPDATE status = VALUES(status), deadline = VALUES(deadline), started_at = VALUES(started_at), completed_at = VALUES(completed_at);

INSERT INTO notifications (id, user_id, type, title, content, ref_id, is_read, created_at) VALUES
(91001, 91001, 'ORDER', '涔版満鍣ㄤ汉璁㈠崟宸插畬鎴?, 'Unitree Go2 Edu 閲囪喘璁㈠崟 XQ202606010001 宸插畬鎴愶紝璧勪骇宸茶繘鍏ユ墭绠′腑蹇冦€?, '91001', 0, '2026-06-01 16:05:00'),
(91002, 91001, 'ASSET_REVENUE', '鎵樼鏀剁泭鍒拌处', 'Go2 鎵樼浠诲姟宸茬粨绠?16800.00 鍏冿紝浣欓鍙湪閽卞寘鏌ョ湅銆?, '91001', 0, '2026-06-03 12:05:00'),
(91003, 91001, 'DISTRIBUTION', '鍒嗛攢浣ｉ噾寰呯粨绠?, '浣犳帹鑽愮殑 B2 宸ヤ笟閲囪喘椤圭洰浜х敓浜岀骇浣ｉ噾锛屼繚鎶ゆ湡鍚庤嚜鍔ㄧ粨绠椼€?, '91003', 0, '2026-06-04 16:00:00'),
(91004, 91003, 'REPAIR', '鎶ヤ慨宸ュ崟宸叉淳鍗?, 'B2 鍏宠妭寮傚搷宸ュ崟宸叉淳缁欐妧鏈憳澶勭悊锛岃淇濇寔璁惧鍋滄満寰呮銆?, '91001', 0, '2026-06-05 09:50:00'),
(91005, 91002, 'SOFTWARE', '杞欢绋嬪簭宸插紑閫?, 'Unitree CloudFleet 浜戠璋冨害绯荤粺绉熸埛宸插紑閫氾紝鍙繘鍏ュ悗鍙伴厤缃澶囥€?, '91003', 1, '2026-06-03 12:40:00')
ON DUPLICATE KEY UPDATE title = VALUES(title), content = VALUES(content), is_read = VALUES(is_read);

INSERT INTO circles (id, name, description, icon_url, member_count, post_count, created_at) VALUES
(91001, '瀹囨爲涔版満鍣ㄤ汉浜ゆ祦鍦?, '鏁存満閲囪喘銆佷氦浠橀獙鏀躲€佸悎鍚屽彂绁ㄥ拰鍦烘櫙閫夊瀷浜ゆ祦銆?, '', 368, 18, '2026-06-01 09:00:00'),
(91002, '瀹囨爲杞欢绋嬪簭寮€鍙戝湀', 'SDK銆丷OS2銆丆loudFleet銆丼imLab 鍜岃繙绋嬭繍缁撮厤缃氦娴併€?, '', 246, 14, '2026-06-01 09:10:00'),
(91003, '鏈哄櫒浜烘墭绠℃敹鐩婂湀', '璐拱鍚庢墭绠″嚭绉熴€佹敹鐩婄粨绠椼€佹姤淇淮鎶ゅ拰璧勪骇杩愯惀浜ゆ祦銆?, '', 198, 11, '2026-06-01 09:20:00')
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), member_count = VALUES(member_count), post_count = VALUES(post_count);

INSERT IGNORE INTO circle_members (id, circle_id, user_id, role, joined_at) VALUES
(91001, 91001, 91001, 'OWNER', '2026-06-01 09:30:00'),
(91002, 91001, 91002, 'MEMBER', '2026-06-01 10:30:00'),
(91003, 91002, 91002, 'OWNER', '2026-06-01 10:40:00'),
(91004, 91002, 91003, 'MEMBER', '2026-06-01 11:40:00'),
(91005, 91003, 91001, 'OWNER', '2026-06-01 12:00:00'),
(91006, 91003, 91003, 'MEMBER', '2026-06-01 12:20:00');

INSERT INTO posts (id, user_id, circle_id, topic_id, title, content, status, like_count, collect_count, comment_count, is_pinned, created_at, updated_at, deleted) VALUES
(91001, 91001, 91003, 91003, 'Go2 閲囪喘鍚庢墭绠?3 澶╋紝绗竴绗旀敹鐩婂凡缁忓埌璐?, '鎴戣繖鍙?Unitree Go2 Edu 閲囪喘鍚庣洿鎺ユ斁鍒板钩鍙版墭绠★紝涓昏璺戦珮鏍′綋楠岃鍜屽洯鍖烘紨绀轰换鍔°€備粖澶╅挶鍖呴噷鐪嬪埌绗竴绗旀墭绠℃敹鐩婂埌璐︼紝鍚庡彴杩樿兘鐪嬪埌璧勪骇銆佸伐鍗曞拰鏀剁泭鏄庣粏锛屾瘮杈冮€傚悎鎯充拱鏈哄櫒浜轰絾鏆傛椂娌℃湁鍥哄畾鍦哄湴鐨勭敤鎴枫€?, 'APPROVED', 328, 64, 38, 1, '2026-06-06 09:20:00', '2026-06-06 09:20:00', 0),
(91002, 91002, 91001, 91001, '瀹為獙瀹ら噰璐?G1 鍓嶏紝鎴戜滑鏁寸悊浜嗕竴浠介獙鏀舵竻鍗?, '鍑嗗涔?Unitree G1 鍋氬叿韬櫤鑳借绋嬪疄楠岋紝閲嶇偣妫€鏌ヨ嚜鐢卞害銆佹€ュ仠銆佸姩浣滃簱銆丼DK 鎺堟潈銆佽繍杈撳寘瑁呭拰鐜板満鍩硅銆傚悗鍙板晢鍝侀噷鑳介厤缃拱鏈哄櫒浜?SKU銆佷环鏍煎拰鏈嶅姟椤癸紝鍓嶇璇︽儏椤靛悓姝ュ睍绀猴紝閲囪喘娌熼€氶『浜嗗緢澶氥€?, 'APPROVED', 216, 42, 24, 0, '2026-06-06 08:45:00', '2026-06-06 08:45:00', 0),
(91003, 91003, 91002, 91002, 'CloudFleet 鎺ュ叆 B2 宸℃椤圭洰鐨勯厤缃粡楠?, '杞欢绋嬪簭杩欏潡寤鸿鍏堝缓鏈哄櫒浜哄垎缁勶紝鍐嶉厤缃矾绾裤€佸績璺冲拰寮傚父鍛婅銆傛垜浠敤 Unitree CloudFleet 绠?B2 宸ュ巶宸℃锛屽嚭鐜扮綉缁滄姈鍔ㄦ椂娑堟伅涓績浼氭帹閫侊紝鎶ヤ慨宸ュ崟涔熻兘鎺ヤ笂銆?, 'APPROVED', 184, 31, 19, 0, '2026-06-05 19:10:00', '2026-06-05 19:10:00', 0),
(91004, 91001, 91002, 91002, 'Robot SDK 涓撲笟鐗堥€傚悎鍝簺浜轰拱锛?, '濡傛灉鍙槸浣撻獙鏈哄櫒浜哄姩浣滐紝鍩虹閰嶇疆灏卞锛涘鏋滆鍋?ROS2銆佷紶鎰熷櫒铻嶅悎銆佹鎬佽皟鍙傚拰璇剧▼瀹為獙锛屽缓璁洿鎺ヤ笂 Unitree Robot SDK 涓撲笟鐗堛€傛垜浠幇鍦ㄦ妸杞欢绋嬪簭璁㈠崟鍜屾満鍣ㄤ汉璧勪骇鍒嗗紑绠＄悊锛屽悗缁画璐规洿娓呮銆?, 'APPROVED', 156, 27, 12, 0, '2026-06-05 14:30:00', '2026-06-05 14:30:00', 0),
(91005, 91002, 91003, 91003, '鎵樼鏀剁泭鍜屾姤淇仈鍔ㄥ緢鏈夊繀瑕?, '鎵樼涓殑鏈哄櫒浜轰竴鏃﹀嚭鐜版晠闅滐紝鏀剁泭鍙兘浼氭殏鍋滐紝鎵€浠ユ垜鐨勫缓璁槸璧勪骇璇︽儏閲屽悓鏃剁湅鎵樼鏀剁泭銆佺淮淇伐鍗曞拰娑堟伅鎻愰啋銆侭2 杩欑被宸ヤ笟鏈哄櫒浜哄挨鍏堕渶瑕佸浐瀹氬贰妫€璁″垝銆?, 'APPROVED', 142, 22, 15, 0, '2026-06-04 18:00:00', '2026-06-04 18:00:00', 0)
ON DUPLICATE KEY UPDATE content = VALUES(content), status = VALUES(status), like_count = VALUES(like_count), collect_count = VALUES(collect_count), comment_count = VALUES(comment_count), is_pinned = VALUES(is_pinned), deleted = 0;

INSERT INTO post_media (id, post_id, url, type, sort_order) VALUES
(91001, 91001, '', 'IMAGE', 0),
(91002, 91002, '', 'IMAGE', 0),
(91003, 91003, '', 'IMAGE', 0),
(91004, 91004, '', 'IMAGE', 0),
(91005, 91005, '', 'IMAGE', 0)
ON DUPLICATE KEY UPDATE url = VALUES(url), type = VALUES(type), sort_order = VALUES(sort_order);

INSERT INTO comments (id, post_id, user_id, parent_id, content, status, created_at) VALUES
(91001, 91001, 91002, NULL, '杩欑被鎵樼鏀剁泭鏁版嵁濡傛灉鑳芥寜璧勪骇鍜屾湀浠芥媶寮€鐪嬶紝浼氭洿閫傚悎缁欒储鍔″仛鎶ヨ〃銆?, 'ACTIVE', '2026-06-06 10:00:00'),
(91002, 91002, 91001, NULL, '楠屾敹娓呭崟閲屽缓璁姞涓?SDK 鎺堟潈璐﹀彿鍜岃蒋浠剁増鏈彿锛屽悗缁淮鎶や細鐪佷簨銆?, 'ACTIVE', '2026-06-06 09:30:00'),
(91003, 91003, 91001, NULL, 'CloudFleet 鐨勫績璺抽棿闅旀垜浠篃璋冭繃锛屽伐鍘?Wi-Fi 瑕嗙洊宸椂寰堝叧閿€?, 'ACTIVE', '2026-06-05 20:00:00'),
(91004, 91004, 91003, NULL, 'SDK 涓撲笟鐗堝宸℃浜屾寮€鍙戝緢鏈夌敤锛屽挨鍏舵槸浼犳劅鍣ㄦ暟鎹鍙栥€?, 'ACTIVE', '2026-06-05 15:10:00'),
(91005, 91005, 91003, NULL, 'B2 宸ヤ笟宸℃纭疄瑕佹妸缁翠慨鍝嶅簲鍐欒繘鎵樼瑙勫垯銆?, 'ACTIVE', '2026-06-04 19:00:00')
ON DUPLICATE KEY UPDATE content = VALUES(content), status = VALUES(status);

INSERT IGNORE INTO post_likes (id, post_id, user_id, created_at) VALUES
(91001, 91001, 91002, '2026-06-06 10:01:00'),
(91002, 91001, 91003, '2026-06-06 10:02:00'),
(91003, 91002, 91001, '2026-06-06 09:31:00'),
(91004, 91003, 91001, '2026-06-05 20:01:00');

INSERT IGNORE INTO post_collects (id, post_id, user_id, created_at) VALUES
(91001, 91001, 91002, '2026-06-06 10:03:00'),
(91002, 91002, 91001, '2026-06-06 09:32:00'),
(91003, 91003, 91001, '2026-06-05 20:02:00');

INSERT IGNORE INTO user_follows (id, follower_id, followee_id, created_at) VALUES
(91001, 91002, 91001, '2026-06-06 10:04:00'),
(91002, 91001, 91003, '2026-06-05 20:03:00');

