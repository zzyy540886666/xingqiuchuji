# OpenAPI 契约（唯一真源）

本目录存放 **与线上行为一致** 的 OpenAPI 描述文件，由 **Spring Boot + springdoc-openapi** 从 Controller/DTO **生成后提交**（或 CI 中导出再提交），前后端联调与类型生成均以此为准。

| 文件 | 说明 |
|------|------|
| [openapi.yaml](./openapi.yaml) | 当前契约快照；接口演进时随发版更新 |

## 工作流程（推荐）

1. 在后端改接口：Controller 路径、DTO 字段、校验注解、`@Schema` 说明。
2. 本地启动应用，执行导出（见 `docs/architecture/02-技术选型与部署拓扑.md` 第 6 节 `curl` 示例）。
3. 将生成的 `openapi.yaml` 覆盖本目录，走 Code Review。
4. 前端 UniApp 可按需用 `openapi-typescript` 等工具从 YAML 生成 TS 类型（可选）。

## 与 Markdown 的关系

- `docs/api/00-接口通则.md`：**永久保留**，描述鉴权头、错误信封、分页、金额单位等**全局约定**（生成器难以表达的规范）。
- `docs/api/API-*.md`：**补充说明**（业务流程、示例场景）；若与 `openapi.yaml` 冲突，**以 openapi.yaml 为准** 并修正 md。
