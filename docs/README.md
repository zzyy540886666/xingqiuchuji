# 星球出机小程序 - 设计文档索引

**技术栈（已定）**：UniApp（微信小程序） + **Java 17 + Spring Boot 3.x** 自建后端；不采用微信云开发；社区机审使用腾讯云内容安全。

| 目录 | 说明 |
|------|------|
| [技术架构与开发准备.md](./技术架构与开发准备.md) | 总览、需求摘要、已定决策、指向各子文档 |
| [architecture/](./architecture/) | 系统总览、技术选型与部署、安全与非功能 |
| [frontend/](./frontend/) | 前端按功能模块：页面结构、状态、与 API 对应关系 |
| [backend/](./backend/) | 后端按功能模块：领域边界、Spring 分层、集成点 |
| [api/](./api/) | **OpenAPI YAML（真源）** + 接口通则 + `API-*.md` 补充说明 |
| [admin/](./admin/) | 后台管理网站需求、技术架构与 Docker 部署方案 |

开发时请在 Cursor 中启用项目 **Rules**（`.cursor/rules/`）与 **Skills**（`.cursor/skills/`），并与 `api/` 契约保持一致。

## 本轮补充（报修工单体系）

根据 `报修工单小程序系统功能清单.xlsx`，已补充以下文档：

- `architecture/04-报修工单系统架构与领域边界.md`
- `frontend/FE-13-报修工单与巡检小程序.md`
- `backend/BE-13-报修工单巡检与运维后台.md`
- `api/API-07-报修工单巡检与运维后台.md`

## 本轮补充（后台管理网站规划）

- `admin/01-后台管理网站需求文档.md`
- `admin/02-后台管理网站技术架构与Docker部署.md`

后台管理接口仍需在实施前补入 `api/openapi/openapi.yaml` 后方可作为开发契约使用。
