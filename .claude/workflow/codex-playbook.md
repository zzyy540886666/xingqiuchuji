# Codex 集成手册

> Codex 插件在本项目中的具体使用方法。

---

## 前置条件

确保 Codex 已安装并登录：
```bash
npm install -g @openai/codex
codex login
```

---

## 场景一：代码审查（阶段5）

### 普通审查
```
Agent(
  description="Codex review current changes",
  prompt="Run: node \"${CLAUDE_PLUGIN_ROOT}/scripts/codex-companion.mjs\" review",
  run_in_background=true
)
```

### 基于分支的审查
```
Agent(
  description="Codex review branch diff",
  prompt="Run: node \"${CLAUDE_PLUGIN_ROOT}/scripts/codex-companion.mjs\" review --base main",
  run_in_background=true
)
```

### 挑战性审查（聚焦特定风险）
```
Agent(
  description="Codex adversarial review - security focus",
  prompt="Run: node \"${CLAUDE_PLUGIN_ROOT}/scripts/codex-companion.mjs\" review --base main. Extra focus: check for SQL injection, XSS, IDOR, and authentication bypass vulnerabilities.",
  run_in_background=true
)
```

---

## 场景二：任务委派（阶段4）

### 独立模块实现
```
Agent(
  description="Codex implement utility module",
  subagent_type="codex:codex-rescue",
  prompt="Create a utility module at src/utils/format.ts with functions for: amount formatting (cents to yuan), date formatting, phone number masking. Follow the project's TypeScript conventions."
)
```

### Bug 修复
```
Agent(
  description="Codex fix bug",
  subagent_type="codex:codex-rescue",
  prompt="Fix the date parsing issue in src/pages/repair/progress.vue line 45. The date string from API is '2026-05-15T10:30:00Z' but the display shows wrong timezone."
)
```

### 测试辅助代码
```
Agent(
  description="Codex generate test fixtures",
  subagent_type="codex:codex-rescue",
  prompt="Generate test fixtures for the repair work order module. Include: mock user data, mock work order data with various statuses, mock payment records. Data should match the shapes in docs/api/openapi/openapi.yaml."
)
```

---

## 场景三：CI 诊断（阶段7）

### CI 失败诊断
```
Agent(
  description="Codex CI failure diagnosis",
  subagent_type="codex:codex-rescue",
  prompt="CI failed with: [粘贴错误信息]. Diagnose the root cause, find the failing file and line, and suggest the minimal fix. Do not make changes - just diagnose."
)
```

---

## 场景四：后台长时间任务

### 复杂功能实现（后台）
```
Agent(
  description="Codex implement payment module",
  subagent_type="codex:codex-rescue",
  prompt="Implement the payment adapter layer for WeChat Pay. Include: prepay, query, refund, close, notify callback. Follow specs in .harness/skills/coding-skill/specs/05-adapter.md. Output code files only.",
  run_in_background=true
)
```

### 检查后台任务状态
```
/codex:status
/codex:result
```

---

## 结果处理

### Codex 审查结果
- 收到后与 Claude 内部评审结果合并
- 去重重叠问题
- 写入 `04-编码评审记录-vN.md`

### Codex 编码结果
- 收到后 Review 其产出
- 检查是否符合分层架构
- 合并到 `03-编码报告.md`
- 确保无越层调用

---

## 注意事项

1. Codex 使用独立的 API 配额，注意用量
2. Codex 后台任务需要手动检查结果
3. Codex 产出必须经过 Claude review 后才能合入
4. 简单任务不需要委派给 Codex，Claude 自己处理更快
5. `run_in_background=true` 时不会阻塞主线，适合并行场景
