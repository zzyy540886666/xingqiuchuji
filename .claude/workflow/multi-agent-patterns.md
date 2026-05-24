# 多 Agent 并行协作模式

> Claude Code 多 Agent 并行执行的具体模式和触发条件。

---

## 模式总览

| 模式 | 阶段 | 主 Agent | 并行 Agent | 产出 |
|------|------|---------|-----------|------|
| 审查并行 | 5 | Claude 评审 | Codex 审查 | 合并评审记录 |
| 编码并行 | 4 | Claude 核心编码 | Codex/Agent 子任务 | 合并编码报告 |
| 诊断并行 | 7 | Claude 分析 | Codex 诊断 | 诊断结果 |
| 修复验证 | 5→4→5 | Claude 修复 | Codex 验证修复 | 修复确认 |

---

## 模式一：审查并行（阶段5 核心优化）

### 触发条件
进入编码评审阶段时自动触发。

### 执行流程

```
1. 前台：Claude 加载 expert-reviewer 执行内部评审
   Skill("expert-reviewer")
   → 产出 Claude 评审意见

2. 同时后台：启动 Codex 独立审查
   Agent(
     description="Codex branch review",
     prompt="Run: node \"${CLAUDE_PLUGIN_ROOT}/scripts/codex-companion.mjs\" review --base main",
     run_in_background=true
   )

3. 等待 Codex 结果返回

4. 合并两份评审：
   - 按 7 个维度归类
   - 去重重叠问题（保留更高优先级）
   - 各自独立问题全部保留
   - 写入 04-编码评审记录-vN.md
```

### 并行优势
- Claude 从架构/业务视角评审
- Codex 从代码质量/模式视角评审
- 双视角发现更多问题

---

## 模式二：编码并行（阶段4 加速）

### 触发条件
编码任务可分解为多个独立子任务时。

### 执行流程

```
1. 任务拆分时识别可并行的子任务

2. 主线：Claude 编码核心业务逻辑
   Skill("coding-skill")

3. 并行 Agent A：独立模块实现
   Agent(
     description="Implement [module A]",
     prompt="[具体的模块实现任务]",
     run_in_background=true
   )

4. 并行 Agent B：工具/辅助代码
   Agent(
     description="Implement [module B]",
     prompt="[具体的工具实现任务]",
     run_in_background=true
   )

5. （可选）并行 Codex：模式化代码
   Agent(
     description="Codex implement [pattern]",
     subagent_type="codex:codex-rescue",
     prompt="[重复模式的代码生成]",
     run_in_background=true
   )

6. 等待全部完成

7. 合并产出：
   - 检查各模块间接口一致
   - 检查无越层调用
   - 合并到 03-编码报告.md
```

### 委派给 Agent vs Codex 的选择

| 任务类型 | 委派给 | 原因 |
|---------|--------|------|
| 架构敏感代码 | Claude 主线 | 需要全局视角 |
| 独立工具函数 | Claude Agent | 快速、可控 |
| 重复模式代码 | Codex | 擅长模式化生成 |
| 诊断/调查 | Codex | 第二视角 |
| 需要上下文的修复 | Claude | 上下文在 Claude 中 |

---

## 模式三：修复+验证并行

### 触发条件
编码评审发现问题，修复后需要再次验证。

### 执行流程

```
1. Claude 修复评审发现的问题

2. 修复的同时，启动 Codex 验证修复
   Agent(
     description="Codex verify fixes",
     prompt="Run: node \"${CLAUDE_PLUGIN_ROOT}/scripts/codex-companion.mjs\" review --base main. Focus on verifying that the following issues were properly fixed: [问题列表]",
     run_in_background=true
   )

3. Claude 完成修复后，等待 Codex 验证结果

4. 如 Codex 发现修复不完整 → 继续修复
5. 如 Codex 确认修复正确 → 进入下一阶段
```

---

## 模式四：CI 诊断并行

### 触发条件
CI 失败，需要同时分析和诊断。

### 执行流程

```
1. Claude 分析 CI 输出，确定失败类型和回退目标
   → tests=0 → 回退阶段6
   → 有未通过 → 回退阶段4

2. 同时 Codex 诊断具体失败原因
   Agent(
     description="Codex CI diagnosis",
     subagent_type="codex:codex-rescue",
     prompt="Diagnose CI failure. Error output: [粘贴]. Find root cause.",
     run_in_background=true
   )

3. 合并分析结果：
   - Claude 确定回退阶段
   - Codex 提供具体修复建议
   - 执行回退并修复
```

---

## 并行控制规则

### 何时并行
- 子任务之间无代码依赖
- 子任务有明确的输入/输出边界
- 任务复杂度足够值得并行开销

### 何时不并行
- 子任务之间有依赖关系
- 需要共享状态
- 任务太简单（并行开销 > 收益）

### 同步点
- 每个阶段结束时必须同步
- 人工确认点必须同步
- 门禁验证必须同步

### 错误处理
- 并行 Agent 失败 → 不阻塞主线，记录失败
- Codex 任务失败 → Claude 主线继续，降级为单 Agent
- 合并时发现冲突 → 以 Claude 判断为准
