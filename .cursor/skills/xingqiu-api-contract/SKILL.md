---
name: xingqiu-api-contract
description: >-
  Keeps REST-style API paths, JSON fields, enums, and HTTP status semantics
  aligned between the WeChat miniprogram client and the self-hosted server.
  Use when defining or changing endpoints, DTOs, error payloads, pagination,
  or when the user mentions 接口、字段、状态码、前后端一致、契约、OpenAPI.
  Schema truth: docs/api/openapi/openapi.yaml (springdoc-generated). Global
  rules: docs/api/00-接口通则.md. API-*.md are narrative supplements only.
disable-model-invocation: false
---

# 星球出机 - 前后端接口契约

## 原则

- **单一事实来源**：路径、Schema、枚举以 **`docs/api/openapi/openapi.yaml`** 为准（Spring 注解生成后提交）；全局 envelope 与分页等见 **`docs/api/00-接口通则.md`**。
- 破坏性变更走版本化（如 `/v1` → `/v2`）或显式弃用周期，避免静默改字段含义。

## 建议约定（可按项目已有规范微调）

- **成功**：HTTP 2xx；业务成功与失败若共存于 200，则响应体内必须有统一 `code` 或 `success` 字段，且全站一致。
- **客户端错误**：4xx 带稳定 `error`/`message`/`code` 结构；401/403 与登录态处理与小程序侧统一拦截。
- **服务端错误**：5xx 不泄露堆栈与内部路径；日志只在服务端记录 traceId。
- **分页**：`page`/`pageSize` 或 `cursor` 二选一并全接口一致；返回体包含 `items` 与 `total` 或 `nextCursor` 之一致结构。
- **时间**：ISO 8601 字符串或 Unix 毫秒，全站一致并写明时区策略。

## 变更流程

1. 先改 **后端 Controller/DTO 与注解**，导出并提交 **openapi.yaml**，再改小程序 `services/` 与调用方；必要时同步修订 `API-*.md` 说明。
2. 对可选字段明确「缺省含义」；对枚举使用字符串枚举并与 DB/后端校验一致。
3. 新增状态值必须同时更新：后端校验、数据库约束（若适用）、小程序展示与分支。

## 禁止

- 仅改一端导致「字段存在但语义不同」。
- 用 HTTP 200 包裹未文档化的任意 JSON 而不给稳定错误码。
