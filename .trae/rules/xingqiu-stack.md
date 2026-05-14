---
alwaysApply: false
description: 星球出机项目技术栈与文档契约（UniApp + Java Spring Boot）
---
# 星球出机 - 项目规则

## 技术栈（已定）

- **小程序端**：UniApp，目标平台为微信小程序；不使用微信云开发。
- **后端**：**Java 17 + Spring Boot 3.x**；REST JSON API，前缀 `/api/v1`。
- **基础设施**：MySQL 8、Redis、腾讯云 COS、腾讯云内容安全（社区文本/图片机审）；IM 为融云或即构（待定）。

## 文档权威来源

实现业务与接口时，与下列文档保持一致；若有冲突以 **prd 已定范围 + api 通则** 为准，并回写文档。

- 总览与索引：`docs/README.md`、`docs/技术架构与开发准备.md`
- 架构：`docs/architecture/`
- 前端模块：`docs/frontend/FE-*.md`
- 后端模块：`docs/backend/BE-*.md`
- 接口：**`docs/api/openapi/openapi.yaml`（OpenAPI，由 springdoc 从代码生成）为 Schema 真源**；`docs/api/00-接口通则.md` 为全局约定；`docs/api/API-*.md` 为补充说明，不得与 YAML 冲突

## 代码原则

- 金额用**分**整数；支付与订单须**幂等**与可审计流水。
- 所有请求参数默认恶意：校验、鉴权、防 SQL 注入与 XSS；敏感配置不进仓库。
- 与现有 Cursor Skills 协同：`xingqiu-miniprogram-dev`、`xingqiu-springboot-backend`、`xingqiu-api-contract`、`xingqiu-data-cache-consistency`、`xingqiu-secure-by-default`、`xingqiu-code-review-delivery`。
