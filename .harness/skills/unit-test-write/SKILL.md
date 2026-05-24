---
name: unit-test-write
description: >-
  受控交付中的测试与门禁 SOP。仅在高风险变更需要正式测试证据或交付门禁时使用。
disable-model-invocation: false
---

# Unit Test Write Skill

## 使用时机

- 受控交付涉及运行时代码，需设计或记录正式测试证据。
- 需要执行 `npm run harness:ci` 形成交付门禁结论。
- 普通任务可直接运行聚焦测试，不加载本 Skill、不生成 Harness 文件。

## 执行步骤

1. 按实际改变的行为和风险触点选择最小充分测试集。
2. 对高风险业务覆盖成功、关键失败和重要边界路径。
3. 运行测试或门禁，保留真实命令与结果。
4. 将验证结果直接写入 `delivery.md`。

## 门禁

高风险运行时代码或部署交付的仓库门禁要求：

- `status=SUCCESS`
- `tests>0`
- `passed=total`

失败时修复实现或测试根因，不创建用于掩盖失败的追加产物。

## 记录要求

- `delivery.md` 写明命令、结果、覆盖风险和未验证事项。
- 仅在外部审计或工具消费要求单独报告时，另存测试结果附件。
