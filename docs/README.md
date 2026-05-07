# 星球出机小程序 - 设计文档索引

**技术栈（已定）**：UniApp（微信小程序） + **Java 17 + Spring Boot 3.x** 自建后端；不采用微信云开发；社区机审使用腾讯云内容安全。

| 目录 | 说明 |
|------|------|
| [技术架构与开发准备.md](./技术架构与开发准备.md) | 总览、需求摘要、已定决策、指向各子文档 |
| [architecture/](./architecture/) | 系统总览、技术选型与部署、安全与非功能 |
| [frontend/](./frontend/) | 前端按功能模块：页面结构、状态、与 API 对应关系 |
| [backend/](./backend/) | 后端按功能模块：领域边界、Spring 分层、集成点 |
| [api/](./api/) | **OpenAPI YAML（真源）** + 接口通则 + `API-*.md` 补充说明 |

开发时请在 Cursor 中启用项目 **Rules**（`.cursor/rules/`）与 **Skills**（`.cursor/skills/`），并与 `api/` 契约保持一致。
