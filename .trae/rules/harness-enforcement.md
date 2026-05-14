---
alwaysApply: true
description: Harness 流程强制门禁规则——确保每次编码任务 100% 经过 Skill 切换、产物产出、质量门禁
---

# Harness 流程强制门禁

本规则为 `.harness/` 体系的执行层。每次收到开发任务时，Agent 必须逐项自检。

---

## A. 强制 Skill 加载协议（确保目标 1）

在执行任何编码/评审/测试操作前，Agent 必须先加载对应 Skill：

| 当前阶段 | 必须加载的 Skill | 加载路径 |
|---------|-----------------|---------|
| 编码实现 | coding-skill | `.harness/skills/coding-skill/SKILL.md` + 8 份 specs |
| 需求评审 / 编码评审 | expert-reviewer | `.harness/skills/expert-reviewer/SKILL.md` |
| 单测编写 | unit-test-write | `.harness/skills/unit-test-write/SKILL.md` |

**自检清单（每次收到任务后立即执行）：**

```
□ 当前处于哪个阶段？（需求分析/需求评审/任务拆分/编码/编码评审/单测/CI/部署）
□ 对应 Skill 是否已加载？
□ 如未加载，必须先用 Read 工具加载 SKILL.md 全文，再继续
```

> ⚠️ 禁止行为：未加载对应 Skill 就直接写代码。

---

## B. 强制产物产出协议（确保目标 2）

每个需求在 `changes/<YYYYMMDD-需求简称>/` 下必须产出完整文档链：

| 序号 | 产物文件名 | 产出阶段 | 必含内容 |
|------|-----------|---------|---------|
| 01 | `01-需求分析文档.md` | 阶段 1 | 需求来源、目标范围、非目标范围、风险 |
| 02 | `02-任务拆分清单.md` | 阶段 3 | 任务列表 + 验证方式 |
| 03 | `03-编码报告.md` | 阶段 4 | 已实现内容、规则对齐、待评审项、未验证项 |
| 04 | `04-评审记录-vN.md` | 阶段 2/5 | 问题/建议/优先级（P0/P1/P2），版本递增不删旧 |
| 05 | `05-CI结果.md` | 阶段 6/7 | status、tests 数量、passed=total |
| 06 | `06-部署验证.md` | 阶段 9 | 部署参数、验证结果 |

**自检清单（阶段切换前必须执行）：**

```
□ 当前阶段产物是否已写入 changes/<需求ID>/ 目录？
□ 产物是否包含必含内容的所有章节？
□ 评审文件是否为递增版本（v1→v2→v3），旧版本未删除？
□ 如产物缺失，禁止进入下一阶段，必须先补齐
```

---

## C. 强制质量门禁协议（确保目标 3）

### C1. CI 门禁（可程序化校验）

进入"部署准备"阶段前，必须逐项确认：

```
□ CI status = SUCCESS
□ tests > 0
□ passed = total
```

任意不满足 → 按回退规则回退：
- `tests=0` → 回退到单测编写
- 有测试但未全通过 → 回退到编码实现

### C2. 安全门禁（每次编码完成自检）

```
□ 所有外部输入是否经过类型/范围/长度/枚举校验？
□ 是否有字符串拼接 SQL？（必须参数化）
□ 是否有未消毒的用户输入直接展示？（防 XSS）
□ 金额字段是否使用分（整数）？
□ 支付/回调/关单流程是否幂等？
□ 敏感配置是否在代码中硬编码？
```

### C3. 架构门禁（每次编码完成自检）

```
□ 是否有越层调用？（Controller→DAO、Domain→SDK）
□ API 路径前缀是否为 /api/v1？
□ 字段/枚举是否与 docs/api/openapi/openapi.yaml 一致？
□ 外部调用是否设了超时？
□ 异常是否被记录（有 traceId）而非吞掉？
```

---

## D. 回退协议（精确路由，不从头来）

| 触发条件 | 回退目标阶段 | 回退后动作 |
|---------|------------|-----------|
| 需求不符 | 需求分析 | 重写 01-需求分析 |
| 编译错误 | 编码实现 | 修复后重新产出 03-编码报告 |
| CI 失败 + tests=0 | 单测编写 | 补写测试，重新产出 05-CI结果 |
| CI 失败 + 有测试未全通过 | 编码实现 | 修复代码 |
| 部署参数错误 | 部署准备 | 修正参数 |
| 验收失败 | 编码评审 | 重评审 |

---

## E. 每次对话启动时的默认动作

1. 用 LS 检查 `changes/` 下是否有活跃需求目录
2. 如有活跃需求，识别当前所处阶段
3. 根据阶段加载对应 Skill
4. 检查当前阶段产物是否存在且完整
5. 以上确认后，方可开始执行任务

---

## F. 与 .trae/rules/ 其他规则的协作

- 本规则为 **流程门禁层**（alwaysApply: true），覆盖"该做什么、何时做"
- `karpathy-guidelines.md` 为 **行为约束层**（alwaysApply: false），覆盖"怎么做才对"
- `xingqiu-dev-principles.md` 为 **安全与契约层**（alwaysApply: true），覆盖"做什么才安全"
- 三者 + Wiki 层在每个阶段同时生效，不互相替代

---

## G. Wiki 查阅协议（确保查阅 docs/ 知识库）

`.harness/wiki/README.md` 是 `docs/` 下 40+ 篇文档的索引。Agent 不能凭记忆编码，必须查阅对应文档。

### G1. 阶段 → 必查文档映射

| 阶段 | 必须查阅的 Wiki 文档 |
|------|---------------------|
| **需求分析** | `docs/architecture/01-系统总览与领域边界.md`、`docs/architecture/02-技术选型与部署拓扑.md`、`docs/api/00-接口通则.md` |
| **需求评审** | `docs/architecture/01-系统总览与领域边界.md`、`docs/architecture/03-安全合规与观测性.md` |
| **任务拆分** | `.harness/wiki/README.md`（索引全览，避免遗漏模块） |
| **编码实现** | 必查：`docs/api/openapi/openapi.yaml`（接口真源）+ 按业务域匹配的 `docs/backend/BE-XX.md` + `docs/frontend/FE-XX.md` + `docs/api/API-XX.md` |
| **编码评审** | 必查：`docs/api/openapi/openapi.yaml` + `docs/architecture/03-安全合规与观测性.md` + 相关业务域的 BE/FE/API 文档 |
| **单测编写** | 按需查相关 API 文档和 BE 文档（确保测试数据贴近线上真实数据特征） |

### G2. 编码阶段 Wiki 查阅自检清单

```
□ 是否已查阅 docs/api/openapi/openapi.yaml 确认字段/枚举/状态码？
□ 是否已查阅对应后端模块文档（docs/backend/BE-XX.md）？
□ 是否已查阅对应前端模块文档（docs/frontend/FE-XX.md）？
□ 是否已查阅对应 API 契约文档（docs/api/API-XX.md）？
□ 查阅后的关键约束是否已写入 changes/<需求ID>/01-需求分析文档.md？
```

> ⚠️ 禁止行为：凭记忆或猜测字段名/枚举值/状态码，不查 openapi.yaml 直接编码。发现时视为违规，必须回退补查。

### G3. 三层加载策略（对齐 .harness 设计文档）

```
会话常驻层（always）  → .trae/rules/harness-enforcement.md + xingqiu-dev-principles.md
阶段触发层（按阶段）  → .harness/skills/coding-skill | expert-reviewer | unit-test-write
按需查阅层（按主题）  → .harness/wiki/README.md → docs/architecture|backend|frontend|api
```
