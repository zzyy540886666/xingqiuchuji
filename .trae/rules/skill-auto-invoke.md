---
alwaysApply: true
description: 强制 Skill 自动调用规则——根据任务类型自动触发对应的 Skill，确保 AI 编码时 100% 加载相关设计/编码/安全约束
---

# Skill 自动调用规则

收到用户任务后，必须在回答前先调用 Skill 工具加载对应的 Skill。本规则为 `alwaysApply: true`，每次对话自动生效。

---

## 技能调用映射表

### 小程序开发

| 任务类型 | 必须调用的 Skill |
|---------|-----------------|
| 小程序页面/组件开发 | `xingqiu-miniprogram-dev` |
| 小程序前端 UI 设计 | `xingqiu-miniprogram-dev` + 下方对应设计 Skill |
| 小程序生成（Taro） | `TRAE-generate-mini-app` |

### 后端开发

| 任务类型 | 必须调用的 Skill |
|---------|-----------------|
| Spring Boot 后端开发 | `xingqiu-springboot-backend` |
| API 接口定义/修改 | `xingqiu-api-contract` |
| Redis / 缓存开发 | `redis-development` |
| 数据库 Schema 修改 | `xingqiu-data-cache-consistency` |

### 前端 UI / 设计

| 任务类型 | 必须调用的 Skill |
|---------|-----------------|
| 新页面 / Landing Page | `design-taste-frontend` |
| 极简风格页面 | `minimalist-ui` |
| 高端视觉设计（Apple 风） | `high-end-visual-design` |
| 工业风 / 数据大屏 | `industrial-brutalist-ui` |
| Web 动画 / GSAP | `gpt-taste` |
| 设计图 → 代码 | `image-to-code` |
| 生成设计参考图 | `imagegen-frontend-web` / `imagegen-frontend-mobile` |
| 品牌 / Logo 设计 | `brandkit` |
| 旧项目 UI 改造 | `redesign-existing-projects` |
| 确保输出完整代码 | `full-output-enforcement` |

### 质量与安全

| 任务类型 | 必须调用的 Skill |
|---------|-----------------|
| 代码审查 / Review | `TRAE-code-review` |
| 调试复杂 Bug | `TRAE-debugger` |
| 安全相关代码 | `xingqiu-secure-by-default` |
| 文档编写 | `xingqiu-design-docs` |
| 编码行为规范 | `karpathy-guidelines` |

### 项目元规则（常驻，无需每次调用）

以下规则已在 `alwaysApply: true` 中原生生效：

- `xingqiu-dev-principles` — 安全、契约、工程化
- `xingqiu-stack` — 技术栈与文档契约
- `skill-auto-invoke.md` — 本规则

---

## 复合任务规则

当任务涉及多个领域时，按优先级顺序调用 Skill：

1. **先加载领域 Skill**（小程序/后端）→ 确保代码结构和 API 契约正确
2. **再加载设计 Skill**（design-taste-frontend 等）→ 确保 UI 质量
3. **最后加载质量 Skill**（TRAE-code-review / xingqiu-secure-by-default）→ 确保安全合规

例：开发小程序首页 → 先调 `xingqiu-miniprogram-dev`，再调 `design-taste-frontend`

---

## 强制执行

- **禁止**在匹配上述任务类型时，不调用 Skill 就直接写代码
- 如不确定该调用哪个 Skill，默认先调 `xingqiu-dev-principles` 和 `xingqiu-stack`
- 用户明确说"不需要 skill"时，可跳过本规则
