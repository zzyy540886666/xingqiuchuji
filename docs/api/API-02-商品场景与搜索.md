# API-02 - 商品目录、详情与活动

前缀：`/api/v1`。金额字段一律为整数分，不接受浮点金额。

## 1. 商品页结构分析与数据来源

商品页包含三类商品标签页：`RENT`、`BUY`、`SOFTWARE`。左侧筛选菜单不得在小程序中写死，使用以下关系表维护：

| 表 | 用途 | 关键约束 |
| --- | --- | --- |
| `catalog_filter_groups` | 左侧菜单组，例如品牌、价格区间、适配机型、热门场景 | `code` 唯一；按 `sort_order` 排序；`enabled` 控制展示 |
| `catalog_filter_options` | 菜单项及查询值 | 归属 `group_id`；价格使用 `min_price_minor` / `max_price_minor` |
| `brands` | 品牌主数据 | SKU 通过 `brand_id` 关联 |
| `scenes` / `scene_sku_rel` | 场景与商品关联 | 用于场景聚合及运营推荐 |

**GET** `/catalog/filters`

返回启用的菜单组及选项。小程序基于 `filterField` 转换为 SKU 查询参数：

| `filterField` | SKU 查询参数 |
| --- | --- |
| `BRAND_ID` | `brandId` |
| `MODEL_ID` | `modelId` |
| `PRICE_RANGE` | `minPrice` / `maxPrice` |
| `KEYWORD` | `q` |

后台管理接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/admin/brands` | 获取品牌主数据 |
| POST | `/admin/brands` | 新增品牌 |
| PUT | `/admin/brands/{id}` | 修改品牌 |
| DELETE | `/admin/brands/{id}` | 删除未被商品引用的品牌 |
| GET | `/admin/catalog-filters` | 获取全部菜单组和选项 |
| POST | `/admin/catalog-filters` | 新增菜单组及选项 |
| PUT | `/admin/catalog-filters/{id}` | 修改菜单组及选项 |
| DELETE | `/admin/catalog-filters/{id}` | 删除菜单组及选项 |

## 2. SKU 列表

**GET** `/catalog/skus`

| 参数 | 说明 |
| --- | --- |
| `type` | `RENT`、`BUY`、`SOFTWARE` |
| `brandId` | 品牌 ID |
| `modelId` | 型号 ID |
| `minPrice` / `maxPrice` | 价格范围，单位分，从 `sku_prices` 过滤 |
| `q` | 名称或描述关键词 |
| `page` / `pageSize` | 分页 |

列表项直接返回卡片所需的 `title`、`image`、`priceAmount`、`stock`、`tags` 等数据库字段组合结果。

## 3. 商品详情完整可编辑结构

**GET** `/catalog/skus/{skuId}`

后台通过 **GET/POST/PUT** `/admin/skus[/{id}]` 维护以下结构：

| 界面内容 | 数据表/字段 |
| --- | --- |
| 名称、类型、型号、简介、副标题、库存 | `skus` |
| 品牌展示名和 Logo | `brands`，SKU 通过 `skus.brand_id` 关联 |
| 原价、适配场景文案、库存状态文案、配送文案 | `skus.original_price_minor`、`adapted_scenes_text`、`stock_status_text`、`delivery_text` |
| 产品参数 | `skus.specs_json`，JSON 对象 |
| 商品图片与视频 | `sku_media`，`type=IMAGE/VIDEO`、`url`、`sort_order` |
| 租赁/买断/订阅价格方案 | `sku_prices`，`price_type`、金额和时长 |
| 标签 | `sku_tags` |
| 服务条目 | `sku_services` |
| 折叠说明区块 | `sku_detail_sections` |

价格类型约定：

| `priceType` | 展示语义 |
| --- | --- |
| `DAILY_RENT` | 短期租赁 |
| `LEASE_BUY` | 租赁购买 |
| `BUY` | 购买买断 |
| `SUBSCRIPTION` | 软件订阅 |

媒体使用结构化 `mediaItems` 返回图片/视频类型，客户端不得按 URL 后缀判断媒体类型。

## 4. 活动详情

活动由 `marketing_activities` 保存，不通过前端常量或 mock 文件提供数据。内容包括标题、副标题、标签、封面图、视频、简介、详情区块 JSON、跳转链接、生效时间、排序与发布状态。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/config/activities` | 获取当前时间有效且已发布的活动 |
| GET | `/config/activities/{id}` | 获取活动详情 |
| GET | `/admin/activities` | 后台活动列表 |
| GET | `/admin/activities/{id}` | 后台活动编辑详情 |
| POST | `/admin/activities` | 创建活动 |
| PUT | `/admin/activities/{id}` | 更新活动 |
| DELETE | `/admin/activities/{id}` | 删除活动 |

首页首个发布活动作为主活动位，点击进入 `/pages/activity-detail/index?id={id}`。

## 5. 场景与搜索

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/catalog/scenes` | 场景列表 |
| GET | `/catalog/scenes/{sceneId}/bundles` | 场景关联 SKU |
| GET | `/catalog/search?q=...` | 商品搜索 |

## 6. 约束与错误码

| code | HTTP | 说明 |
| --- | --- | --- |
| `SKU_NOT_FOUND` | 404 | 商品不存在 |
| `SKU_OFFLINE` | 400 | 商品已下架 |
| `NOT_FOUND` | 404 | 筛选菜单或活动不存在 |
