# Harness 体系总览

本目录严格按 `docs/让ai上手企业项目的harness体系构建.md` 组织，目标是让 Agent 在企业项目里按统一流程稳定交付。

## 目录结构

- `rules/`：不随需求变化的稳定约束（工程结构、流程门禁、硬性规范）
- `skills/`：结构化 SOP（编码、评审、测试）
- `wiki/`：按需查阅知识（链路、数据模型、核心流程）
- `changes/`：全流程留痕（需求到部署验证）
- `agent/`：可选放置 agent 辅助文档（主角色定义在仓库根目录）

## 使用方式

1. 会话常驻加载：`/AGENT-ROLE.md` + `rules/*`
2. 阶段触发加载：编码阶段加载 `skills/coding-skill/`，评审阶段加载 `skills/expert-reviewer/`，测试阶段加载 `skills/unit-test-write/`
3. 按需查询：`wiki/*` 不主动加载，按任务检索

## Cursor 自动触发绑定

为提高自动触发稳定性，已添加以下绑定：

1. 全局强制规则：`.cursor/rules/harness-mandatory-execution.mdc`（alwaysApply）
2. 执行型 Skill：`.cursor/skills/harness-doer/SKILL.md`
3. 评判型 Skill：`.cursor/skills/harness-reviewer/SKILL.md`

## 质量门禁（可程序化）

- CI 状态必须 `status=SUCCESS`
- 测试数量必须 `tests>0`
- 测试通过必须 `passed=total`

不满足任一条件，按 `/AGENT-ROLE.md` 回退规则精确回退到对应阶段。
