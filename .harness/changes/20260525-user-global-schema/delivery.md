# 用户与全局数据库结构完善

## 目标与范围

- 为现有小程序后端补齐用户中心、全局配置、钱包账务、权限、审计、幂等和媒体资产相关的数据表结构。
- 采用新增兼容层方式，不删除现有表、不重命名现有字段，避免当前 `users`、`wallets`、`app_configs` 等运行时代码立即失效。
- 本次只交付数据库迁移脚本和结构回填基础，不切换 Java Service/Mapper 到新表。

## 风险触点和关键决策

- 数据库迁移属于高风险变更，按受控交付处理。
- `users.role` 现阶段仍保留，避免当前登录逻辑依赖 `role = DISABLED` 的禁用判断被破坏；新增 `users.status` 作为后续规范状态字段。
- 钱包新增规范化 `wallet_accounts` 和 `wallet_ledger_entries`，但保留旧 `wallets`、`wallet_ledger` 供当前服务继续读写。
- 全局配置新增 `app_config_items`、`app_config_revisions`、`app_config_publish_batches`，保留旧 `app_configs` 兼容现有后台配置接口。
- 敏感资料表只保存手机号掩码/哈希、地址密文字段，不新增明文敏感字段。

## 实际改动

- 新增迁移脚本：`backend/sql/migrations/V20260525__user_global_schema_hardening.sql`
- 新增/强化的核心结构：
  - 用户主档增强：`users.status`、`user_type`、`registered_at`、`last_login_at`、`deleted`、`version`
  - 登录身份：`user_auth_identities`
  - 用户资料与地址：`user_profiles`、`user_addresses`
  - RBAC：`roles`、`user_role_rel`、`admin_user_role_rel`
  - 钱包规范账务：`wallet_accounts`、`wallet_ledger_entries`，并增强 `wallets`、`withdraw_requests`
  - 配置发布体系：`app_config_items`、`app_config_revisions`、`app_config_publish_batches`
  - 全局基础设施：`dict_types`、`dict_items`、`media_files`、`operation_audit_logs`、`idempotency_records`
  - 定时任务记录增强：`scheduled_job_runs.trace_id`、`duration_ms`、`error_message`

## 验证证据与结果

- 已执行数据库迁移：
  - 命令：`Get-Content -Raw -Encoding UTF8 backend\sql\migrations\V20260525__user_global_schema_hardening.sql | docker exec -i xq-mysql mysql -uroot -pxingqiu123 --default-character-set=utf8mb4`
  - 结果：exitCode=0，MySQL 未返回执行错误
- 数据库结构和回填校验：
  - 新增表存在：`user_auth_identities`、`wallet_accounts`、`wallet_ledger_entries`、`app_config_items`、`operation_audit_logs`、`idempotency_records`
  - 回填计数：`users=5`、`user_auth_identities=5`、`user_profiles=5`、`wallet_accounts=5`、`wallet_ledger_entries=7`、`app_config_items=4`、`app_config_revisions=4`
  - 增强字段存在：`users.status`、`users.user_type`、`wallets.currency`、`withdraw_requests.channel`、`scheduled_job_runs.trace_id`
- 本地启动验证：
  - 已执行 `.\start-dev.ps1`
  - 后端健康检查：`GET http://localhost:8080/meta/ping` 返回 200，响应 `{"success":true,"data":"pong",...}`
  - 后台前端：`GET http://localhost:3000` 返回 200
- `npm.cmd run harness:ci`
  - status=SUCCESS
  - tests=5
  - passed=5
  - total=5
  - artifact=`D:\Pro\星球出机小程序 - 副本\.harness\ci\latest.json`
- SQL 静态风险扫描：
  - 检查 `CAST(`、`DROP TABLE`、`DROP COLUMN`、`DELETE FROM`、`TRUNCATE`
  - 结果：未命中

## 未验证事项

- 本次只在本地开发库 `xq-mysql/xingqiu_dev` 验证，未在生产或类生产数据库执行。
- 新结构尚未接入 Java Service/Mapper，运行时代码仍主要读写旧表。

## 回退与后续动作

- 回退：本迁移是新增兼容层；若需要回退，应按新增对象逐项删除新增表和新增列，并先确认没有服务切换到新表。
- 后续动作：
  - 分阶段把登录、用户状态、钱包流水、配置发布服务切换到新结构。
