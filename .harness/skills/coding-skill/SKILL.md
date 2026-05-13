---
name: coding-skill
description: >-
  分层编码 SOP。用于进入编码阶段时，按 Controller/Service/Domain/DAO/Adapter
  及扩展规范逐层实现，不允许凭感觉直接堆代码。
disable-model-invocation: false
---

# Coding Skill

## 目标

按规范一层一层实现，不是凭感觉写。

## 使用时机

- 进入“编码实现”阶段时强制加载
- 编码评审要求回退到实现阶段时重新加载

## 执行步骤

1. 先确认接口契约（OpenAPI + API 通则）
2. 从 Controller 开始建立输入/输出边界
3. 进入 Service 编排用例与事务
4. 在 Domain 落地业务规则与状态机
5. DAO 层实现参数化持久化访问
6. Adapter 层完成外部依赖调用与容错
7. 补全 DTO 校验、幂等、观测性
8. 自检通过后输出编码报告

## 分层规范索引（8 份）

1. `specs/01-controller.md`
2. `specs/02-service.md`
3. `specs/03-domain.md`
4. `specs/04-dao.md`
5. `specs/05-adapter.md`
6. `specs/06-dto-and-validation.md`
7. `specs/07-transaction-and-idempotency.md`
8. `specs/08-observability-and-error-handling.md`

## 产出物

- 代码变更
- `changes/<需求ID>/03-编码报告.md`
