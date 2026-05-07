---
name: xingqiu-code-review-delivery
description: >-
  Reviews 星球出机 changes before delivery for correctness, security, API contract
  drift, maintainability, documentation sync, and runnable verification. Use when
  finishing a feature, refactoring, reviewing diffs, or when the user mentions
  review、自检、检查、交付、验收、测试、质量、可维护性.
disable-model-invocation: false
---

# 星球出机 - 代码审查与交付自检

## 审查重点

- 与 `docs/` 业务设计、`docs/api/openapi/openapi.yaml`、接口通则保持一致。
- 没有引入信任前端 userId、越权访问、SQL 拼接、XSS 展示、密钥入库/入仓库等风险。
- 新增逻辑按模块分层，不把网络、鉴权、状态机、渲染细节混在单个巨型文件。
- 错误处理可恢复、可观测；服务端日志有定位信息但不泄露敏感数据。

## 交付前清单

- 后端接口：Controller/DTO/Service/Repository 边界清晰，事务、幂等、校验、鉴权覆盖关键路径。
- 小程序端：页面注册、空态/加载态/错误态、请求封装、字段枚举、SVG 图标约定完整。
- API 契约：路径、字段、枚举、状态码、错误结构已同步 OpenAPI 与必要说明文档。
- 数据变更：表结构、索引、缓存 key、TTL、迁移与回滚路径明确。
- 验证：优先运行项目已有测试、lint、类型检查或最小可复现构建；无法运行时说明原因与人工检查点。

## 输出要求

- 先列阻断性问题，再列建议优化；不要把风格偏好伪装成必须修改。
- 对每个问题说明影响范围、根因、建议修改位置。
