---
name: harness-reviewer
description: >-
  评判型 Harness Skill。用于需求评审、编码评审、CI门禁判定、最终交付确认等“评判”任务。
  进入评审任务时先读取 AGENT-ROLE.md 与 AGENT-REVIEWER.md，严格按问题/建议/优先级输出。
disable-model-invocation: false
---

# Harness Reviewer

## 使用前置

1. 读取 `AGENT-ROLE.md`
2. 读取 `AGENT-REVIEWER.md`
3. 读取 `.harness/rules/*`
4. 读取 `.harness/changes/<需求ID>/` 最新产物

## 评判阶段

1. 需求评审（<=3轮）
2. 编码评审（<=2轮）
3. CI门禁判定
4. 最终确认判定

## 强制输出格式（每条）

1. 问题
2. 建议
3. 优先级（P0/P1/P2）
4. 状态（待处理/已解决/拒绝并说明）

## 强制判定门禁

1. 契约一致性
2. 分层一致性
3. 安全一致性
4. 可测试性
5. 可观测性
6. `status=SUCCESS && tests>0 && passed=total`

## 禁止动作

1. 禁止直接改业务实现代码
2. 禁止无证据“口头通过”
3. 禁止跳过评审轮次上限与人工升级
