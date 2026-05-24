# 商品目录与活动后台管理交付

## 目标

将小程序商品页左侧菜单、商品详情内容和活动详情从静态前端内容改为数据库驱动，并提供后台管理网站编辑入口。

## 范围

- 小程序：商品分类菜单、商品详情、首页活动入口、活动详情页。
- 后端：公共查询 API 与后台管理 API。
- 数据库：商品详情扩展关系、目录筛选菜单、活动详情实体。
- 契约：`docs/api/API-01-认证与用户-运营配置.md`、`docs/api/API-02-商品场景与搜索.md`、OpenAPI。

## 关键决策

- 左侧菜单采用 `catalog_filter_groups` 与 `catalog_filter_options` 关系表，选项直接映射为查询字段，避免前端维护品牌/价格/型号/场景常量。
- 商品媒体使用 `sku_media.type=IMAGE|VIDEO` 返回结构化 `mediaItems`，不依赖 URL 后缀判断视频。
- 商品标签、服务项和详情说明使用独立子表；参数保留 JSON 对象以支持不同型号的非固定技术参数。
- 活动详情使用 `marketing_activities`，只有已发布且处于生效期的数据会提供给小程序。
- 开发种子数据用于本地真实数据库数据填充；现有生产数据库必须使用一次性迁移脚本受控变更。

## 改动

- 增加目录筛选、SKU 标签/服务/详情段落、营销活动领域实体与 mapper。
- 增加 `/catalog/filters`、`/config/activities`、`/admin/catalog-filters`、`/admin/activities` 接口。
- 扩展 `/catalog/skus` 与 `/catalog/skus/{id}` 返回结构，支持媒体、价格方案、服务和说明区块。
- 后台增加品牌主数据、商品详情完整维护表单、筛选菜单管理和活动详情管理页面。
- 小程序新增活动详情页，并将商品分类/详情和首页活动全部接入后端数据。
- 增加 `backend/sql/migrations/V20260524__catalog_activity_management.sql` 与开发种子记录。
- 清除后台运营位/专题内置样例回退，并将后台用户会员等级切换为会员表查询、缺失手机号保持空值。

## 验证

- `backend`: `mvn.cmd -q -DskipTests compile` 通过。
- `frontend`: `npm.cmd run build:mp-weixin` 通过；构建仍输出 Sass legacy API 弃用警告。
- `admin-web`: `npm.cmd run build` 通过；构建输出 Element Plus 相关大 chunk 提示，不阻断构建。
- `docs`: 使用 `yaml` 解析 `docs/api/openapi/openapi.yaml` 通过。
- `workspace`: `npm.cmd run harness:ci` 通过，结果为 `SUCCESS`，`5/5` 测试通过。

## 风险与回退

- 上线前必须先执行数据库迁移脚本，再发布依赖新增表/列的后端；反序发布会导致查询新增字段失败。
- 回退应用版本前应保留新增表与列，属于向后兼容的数据结构；不要直接删除活动或商品详情数据。
