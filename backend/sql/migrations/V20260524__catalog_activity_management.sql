-- Product catalog detail, filter navigation and marketing activity management.
-- Apply exactly once to an existing MySQL 8.0 database during a controlled release.
USE xingqiu_dev;

ALTER TABLE skus
    ADD COLUMN subtitle VARCHAR(256) NULL AFTER description,
    ADD COLUMN original_price_minor BIGINT NOT NULL DEFAULT 0 AFTER specs_json,
    ADD COLUMN adapted_scenes_text VARCHAR(512) NULL AFTER original_price_minor,
    ADD COLUMN stock_status_text VARCHAR(128) NULL AFTER adapted_scenes_text,
    ADD COLUMN delivery_text VARCHAR(128) NULL AFTER stock_status_text,
    ADD CONSTRAINT chk_sku_price_stock CHECK (stock >= 0 AND original_price_minor >= 0);

CREATE TABLE sku_tags (
    id BIGINT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sku_tag_name (sku_id, name),
    INDEX idx_sku_tags_sku (sku_id, sort_order),
    CONSTRAINT fk_sku_tags_sku FOREIGN KEY (sku_id) REFERENCES skus(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sku_services (
    id BIGINT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    price_label VARCHAR(64) NOT NULL DEFAULT '免费',
    sort_order INT NOT NULL DEFAULT 0,
    INDEX idx_sku_services_sku (sku_id, sort_order),
    CONSTRAINT fk_sku_services_sku FOREIGN KEY (sku_id) REFERENCES skus(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sku_detail_sections (
    id BIGINT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    content TEXT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    INDEX idx_sku_detail_sections_sku (sku_id, sort_order),
    CONSTRAINT fk_sku_detail_sections_sku FOREIGN KEY (sku_id) REFERENCES skus(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE catalog_filter_groups (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(64) NOT NULL,
    filter_field VARCHAR(32) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    INDEX idx_catalog_filter_groups_visible (enabled, sort_order),
    CONSTRAINT chk_catalog_filter_field CHECK (filter_field IN ('BRAND_ID', 'PRICE_RANGE', 'MODEL_ID', 'KEYWORD'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE catalog_filter_options (
    id BIGINT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    label VARCHAR(64) NOT NULL,
    value VARCHAR(128),
    min_price_minor BIGINT,
    max_price_minor BIGINT,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    INDEX idx_catalog_filter_options_visible (group_id, enabled, sort_order),
    CONSTRAINT fk_catalog_filter_options_group FOREIGN KEY (group_id) REFERENCES catalog_filter_groups(id),
    CONSTRAINT chk_catalog_filter_prices CHECK (
        (min_price_minor IS NULL OR min_price_minor >= 0) AND
        (max_price_minor IS NULL OR max_price_minor >= 0) AND
        (min_price_minor IS NULL OR max_price_minor IS NULL OR max_price_minor > min_price_minor)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE marketing_activities (
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

ALTER TABLE sku_media
    ADD CONSTRAINT fk_sku_media_sku FOREIGN KEY (sku_id) REFERENCES skus(id);

ALTER TABLE sku_prices
    ADD CONSTRAINT fk_sku_prices_sku FOREIGN KEY (sku_id) REFERENCES skus(id),
    ADD CONSTRAINT chk_sku_prices_amount CHECK (price_minor >= 0 AND daily_rate_minor >= 0);

ALTER TABLE scene_sku_rel
    ADD CONSTRAINT fk_scene_sku_scene FOREIGN KEY (scene_id) REFERENCES scenes(id),
    ADD CONSTRAINT fk_scene_sku_sku FOREIGN KEY (sku_id) REFERENCES skus(id),
    ADD UNIQUE KEY uk_scene_sku (scene_id, sku_id);

ALTER TABLE skus
    ADD CONSTRAINT fk_skus_brand FOREIGN KEY (brand_id) REFERENCES brands(id);
