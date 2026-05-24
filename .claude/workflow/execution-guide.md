# 执行指南 — Claude Code 工具调用手册

> 本文件定义每个阶段的精确工具调用序列。Claude Code 必须按此执行。

---

## 阶段1: 需求分析（Doer）

### 必读文件
```
Read("AGENT-ROLE.md")
Read(".harness/rules/01-工程结构-模块职责-五层架构.md")
Read(".harness/rules/02-开发流程规范-阶段与门禁.md")
Read(".harness/rules/03-工程硬性规范.md")
```

### 执行
```
Skill("coding-skill")  // 加载编码 SOP（分析阶段也需要）
```

### 产出
- 创建 `.harness/changes/<需求ID>/01-需求分析文档.md`
- 包含：需求理解、目标范围、非目标范围、待决议项

### 门禁
- [ ] 需求来源可查
- [ ] 目标范围明确
- [ ] 非目标范围明确
- [ ] 待决议项已列出

### 人工确认点
- **需求待决议确认** → 等待用户输入后再进入阶段2

---

## 阶段2: 需求评审（Reviewer, ≤3轮）

### 必读文件
```
Read("AGENT-ROLE.md")
Read("AGENT-REVIEWER.md")
Read(".harness/rules/*")
Skill("expert-reviewer")
```

### 执行
```
Skill("expert-reviewer")
// 逐条检查 7 个维度：需求一致性/架构一致性/契约一致性/安全性/数据一致性/可观测性/可测试性
```

### 产出
- 创建 `.harness/changes/<需求ID>/02-需求评审记录-v1.md`（首轮）
- 每条意见包含：问题 / 建议 / 优先级 (P0/P1/P2)

### 门禁
- [ ] 7个维度全部覆盖
- [ ] 每条意见有三元组
- [ ] P0 问题已标记
- [ ] 结论明确：通过/不通过

### 回退
- 不通过 → 回退阶段1
- 超过3轮 → 升级人工决策，停止

---

## 阶段3: 任务拆分（Doer）

### 执行
```
Skill("coding-skill")
// 基于需求分析文档，按五层架构拆分任务
```

### 产出
- 创建 `.harness/changes/<需求ID>/02-任务拆分清单.md`

### 人工确认点
- **计划评审后确认** → 等待用户确认任务拆分

---

## 阶段4: 编码实现（Doer, Codex 并行）

### 必读文件
```
Read("AGENT-ROLE.md")
Read("AGENT-DOER.md")
Skill("coding-skill")
// 按需读取 specs/ 下的分层规范
```

### 执行（核心业务）
```
Skill("coding-skill")
// 按 8 步 SOP 逐层实现：Controller → Service → Domain → DAO → Adapter → DTO → 事务/幂等 → 可观测性
```

### Codex 并行子任务
判断是否有可委派的独立子任务：

```
// 如果有独立模块、工具函数、测试辅助代码等可委派：
Agent(
  description="Codex implementation subtask",
  subagent_type="codex:codex-rescue",
  prompt="[具体子任务描述，包含文件路径和预期产出]",
  run_in_background=true
)
```

**委派条件清单：**
- [ ] 子任务与主线无代码依赖
- [ ] 子任务是独立模块/工具函数
- [ ] 子任务可明确定义边界

### 多 Agent 并行模式
```
// 同时启动多个独立任务
Agent(description="前端页面实现", prompt="...", run_in_background=true)
Agent(description="后端接口实现", prompt="...", run_in_background=true)
Agent(description="Codex工具类", subagent_type="codex:codex-rescue", prompt="...", run_in_background=true)
```

### 产出
- 代码变更
- `.harness/changes/<需求ID>/03-编码报告.md`

### 自检清单（编码报告必须覆盖）
- [ ] openapi.yaml 字段/枚举/状态码在代码中一致
- [ ] 调用链无越层
- [ ] 外部调用有超时+重试+降级
- [ ] 金额字段整数分
- [ ] 无字符串拼接 SQL
- [ ] 关键路径有日志+traceId
- [ ] 幂等键已实现

### 回退
- 编译错误 → 停留在阶段4修复

---

## 阶段5: 编码评审（Reviewer, ≤2轮, Codex 并行）

### 必读文件
```
Read("AGENT-ROLE.md")
Read("AGENT-REVIEWER.md")
Skill("expert-reviewer")
```

### 双流审查（核心优化）

**流1: Claude 内部评审**
```
Skill("expert-reviewer")
// 7 个维度逐项检查
```

**流2: Codex 独立审查（后台并行）**
```
// 普通审查
Agent(
  description="Codex code review",
  prompt="Run codex review for this project. Execute: node \"${CLAUDE_PLUGIN_ROOT}/scripts/codex-companion.mjs\" review --base main",
  run_in_background=true
)

// 或挑战性审查（有特定关注点时）
Agent(
  description="Codex adversarial review",
  prompt="Run codex adversarial review. Execute: node \"${CLAUDE_PLUGIN_ROOT}/scripts/codex-companion.mjs\" review --base main. Focus on: [安全/性能/数据一致性等具体关注点]",
  run_in_background=true
)
```

### 结果合并
1. 收集 Claude 评审结果
2. 收集 Codex 评审结果
3. 去重重叠问题，保留独立问题
4. 重叠问题优先级取较高者
5. 写入合并后的评审记录

### 产出
- `.harness/changes/<需求ID>/04-编码评审记录-vN.md`（版本递增，旧版不删）

### 人工确认点
- **编码评审后确认** → 等待用户确认

### 回退
- 不通过 → 回退阶段4
- 超过2轮 → 升级人工决策，停止

---

## 阶段6: 单测编写（Doer）

### 必读文件
```
Read("AGENT-ROLE.md")
Read("AGENT-DOER.md")
Skill("unit-test-write")
```

### 执行
```
Skill("unit-test-write")
// 基于变更接口列出测试清单
// 构造接近线上分布的测试样例
// 编写单测与集成测试
```

### 产出
- 测试代码
- `.harness/changes/<需求ID>/05-CI结果.md`

### 门禁
- CI 程序化输出：
  - `status = SUCCESS`
  - `tests > 0`
  - `passed = total`

---

## 阶段7: CI 门禁判定（Reviewer）

### 执行
```bash
# 运行 CI
npm run type-check
npm run build:mp-weixin
# 或后端
mvn test
```

### Codex 辅助诊断（CI 失败时）
```
Agent(
  description="Codex CI diagnosis",
  subagent_type="codex:codex-rescue",
  prompt="CI failed. Diagnose: [错误信息]. Find root cause and suggest minimal fix.",
  run_in_background=true
)
```

### 回退
- tests=0 → 回退阶段6
- 有未通过 → 回退阶段4

---

## 阶段8: 部署准备（Doer）

### 执行
- 准备部署参数
- 编写部署文档

### 产出
- `.harness/changes/<需求ID>/06-部署验证.md`

### 人工确认点
- **部署环境参数确认** → 等待用户确认

---

## 阶段9: 部署执行（Doer）

### 执行
- 按确认的参数执行部署
- 验证部署结果

---

## 阶段10: 最终确认与交付（Reviewer）

### 执行
- 检查所有产出物完整性
- 检查所有门禁通过
- 检查留痕文件齐全

### 人工确认点
- **最终交付确认** → 等待用户确认

---

## 快速决策树

```
收到需求
  → 读 CLAUDE.md + AGENT-ROLE.md + .harness/rules/*
  → 确定阶段
  → 阶段1/3/4/6/8/9 → Skill(coding-skill) 或 Skill(unit-test-write) → Doer 执行
  → 阶段2/5/7/10     → Skill(expert-reviewer) → Reviewer 执行
  → 阶段4 有独立任务 → Agent(codex-rescue) 并行
  → 阶段5           → Skill(expert-reviewer) + Agent(codex-review) 双流
  → 阶段7 CI失败    → Agent(codex-rescue) 诊断
  → 失败            → 按回退路由精确回退
  → 通过            → 进入下一阶段
```
