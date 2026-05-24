---
name: coding-skill
description: >-
  受控交付中的实现 SOP。仅在高风险实现需要正式证据时使用，
  依据实际影响范围检查契约、分层、安全和可观测性。
disable-model-invocation: false
---

# Coding Skill

## 使用时机

- 任务已进入受控交付，且需要修改运行时代码或工程配置。
- 普通小功能和缺陷修复不因写代码而加载本 Skill。

## 执行原则

1. 从用户目标和受影响路径开始，只修改达成目标所需的模块与层级。
2. 仅当接口、状态或调用方受影响时核对 OpenAPI 与相关 API 文档。
3. 仅当后端调用链受影响时核对 Controller / Service / Domain / DAO / Adapter 边界。
4. 仅当任务触及资金、鉴权、输入、持久化或外部调用时执行对应安全/幂等/超时检查。
5. 将关键决策、实际验证和剩余风险集中写入 `delivery.md`，不拆出形式化编码报告。

## 按需规范索引

- `specs/01-controller.md` 至 `specs/05-adapter.md`：仅读取被修改的后端层。
- `specs/06-dto-and-validation.md`：输入/DTO/契约受影响时读取。
- `specs/07-transaction-and-idempotency.md`：事务、订单、支付或重复请求受影响时读取。
- `specs/08-observability-and-error-handling.md`：外部集成或关键失败路径受影响时读取。

## 验证清单

仅核对适用于本次改动的项目：

- [ ] 受影响接口与 OpenAPI/调用方保持一致。
- [ ] 受影响调用路径没有新增越层依赖。
- [ ] 涉及金额时仍为整数分；涉及 SQL 时仍为参数化访问。
- [ ] 涉及外部调用时具备合理的超时和失败处理。
- [ ] 涉及安全或关键写入时具备权限、校验、幂等或审计证据。
- [ ] 已运行与影响面匹配的验证，并将结论写入 `delivery.md`。
