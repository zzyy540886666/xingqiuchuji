# Codex 多 Agent + Git Worktree 协作小白操作指南

本文用于在任意项目中复现“一个母 Agent 统筹，多个子 Agent 在独立目录/独立分支并行开发”的工作方式。

## 1. 适用场景

适合这些任务：

- 一个需求能拆成多个模块并行做。
- 多个 Agent 可能同时改代码。
- 涉及支付、鉴权、订单、金额、数据库、接口契约等高风险改动。
- 想让一个母 Agent 负责分发任务、审核改动、跑测试和合并结果。

不适合这些任务：

- 只改一两个文件的小修复。
- 需求还不清楚，暂时只能讨论方案。
- 没有 Git 仓库的临时目录。

## 2. 核心概念

- 母 Agent：负责拆任务、分发、检查子 Agent 的结果、合并分支、跑测试。
- 子 Agent：只负责一个明确模块，不碰其他模块。
- Git worktree：让同一个 Git 仓库拥有多个独立工作目录，每个目录绑定一个独立分支。
- 独立分支：每个子 Agent 的修改隔离在自己的分支里，后续由母 Agent 合并。

## 3. 推荐目录结构

假设主项目在：

```text
D:\Pro\my-project
```

可以创建这些并行目录：

```text
D:\Pro\my-project
D:\Pro\my-project-agent-order
D:\Pro\my-project-agent-payment
D:\Pro\my-project-agent-contract
```

主目录给母 Agent 使用，其他目录分别给子 Agent 使用。

## 4. 第一步：在主项目检查状态

在主项目目录运行：

```powershell
git status --short
git branch --show-current
git worktree list
```

注意：

- 如果有未提交改动，先判断这些改动是不是所有子 Agent 都需要。
- 如果任务文档是未提交文件，子 worktree 默认看不到，需要复制过去，或者先提交到一个基线分支。
- 不要在有大量混乱改动时直接开始多 Agent 并行，否则后续很难合并。

## 5. 第二步：创建多个 worktree

命令格式：

```powershell
git worktree add -b <新分支名> <新目录路径> HEAD
```

示例：

```powershell
git worktree add -b codex/p0-order-pricing ..\my-project-agent-order HEAD
git worktree add -b codex/p0-payment-orchestration ..\my-project-agent-payment HEAD
git worktree add -b codex/p0-contract-openapi ..\my-project-agent-contract HEAD
```

创建后检查：

```powershell
git worktree list
```

你应该看到类似结果：

```text
D:/Pro/my-project                  abc1234 [main]
D:/Pro/my-project-agent-order      abc1234 [codex/p0-order-pricing]
D:/Pro/my-project-agent-payment    abc1234 [codex/p0-payment-orchestration]
D:/Pro/my-project-agent-contract   abc1234 [codex/p0-contract-openapi]
```

## 6. 第三步：让每个子目录都有任务说明

如果任务说明文档已经提交到 Git，子 worktree 会自动拥有这些文档。

如果任务说明文档还没有提交，可以手动复制：

```powershell
Copy-Item ".\docs\your-task-plan.md" "..\my-project-agent-order\docs\your-task-plan.md" -Force
Copy-Item ".\docs\your-task-plan.md" "..\my-project-agent-payment\docs\your-task-plan.md" -Force
Copy-Item ".\docs\your-task-plan.md" "..\my-project-agent-contract\docs\your-task-plan.md" -Force
```

建议：复制过去的任务文档只给子 Agent 读取，不要让子 Agent 修改或提交。

## 7. 第四步：打开多个 Codex 对话框

建议开 4 个 Codex 对话框：

- 对话框 1：母 Agent，工作目录选择主项目目录。
- 对话框 2：子 Agent-A，工作目录选择 `my-project-agent-order`。
- 对话框 3：子 Agent-B，工作目录选择 `my-project-agent-payment`。
- 对话框 4：子 Agent-C，工作目录选择 `my-project-agent-contract`。

不要让多个 Codex 对话框同时使用同一个工作目录。

## 8. 母 Agent 提示词模板

把下面内容发给母 Agent：

```text
你是本项目的母 Agent，负责统筹多 Agent 并行实现。

职责：
1. 读取 AGENTS.md 和本轮任务文档。
2. 拆分任务，明确每个子 Agent 的边界。
3. 不直接做大范围代码修改，除非是集成修复或验证修复。
4. 审查每个子 Agent 的 git diff、测试结果、风险说明。
5. 控制合并顺序，避免互相覆盖。
6. 合并每个子分支前先运行聚焦测试。
7. 全部合并后运行项目要求的完整验证。
8. 最终输出已完成任务、验证结果、剩余风险和人工验收点。

要求：
- 不覆盖用户未提交改动。
- 涉及接口时同步检查 OpenAPI 或接口文档。
- 涉及金额时确认使用整数分。
- 涉及回调、任务、资产、分佣、支付时检查幂等。
- 如果发现子 Agent 改了无关文件，指出并要求修正。
```

## 9. 子 Agent 通用提示词模板

把下面内容发给每个子 Agent，并替换方括号里的内容：

```text
你是子 Agent，当前工作目录是独立 git worktree，分支是 [分支名]。

你只负责这些任务：
- [任务编号和任务名称]

请先读取：
- AGENTS.md
- [任务文档路径]

工作要求：
1. 只修改你负责模块相关文件。
2. 不修改、不提交任务说明文档。
3. 不改其他 Agent 负责的模块。
4. 修改前先搜索并理解现有实现方式。
5. 涉及金额统一使用整数分。
6. 涉及回调、事件、任务、资产、分佣时必须保证幂等。
7. 涉及接口变化必须同步接口文档。
8. 完成后运行最聚焦的测试、类型检查或构建。

交付时请汇总：
1. 完成的任务编号。
2. 修改的文件。
3. 运行的验证命令和结果。
4. 对应验收点。
5. 剩余风险。
6. 需要人工操作或人工验收的步骤。
```

## 10. 子 Agent 示例提示词

订单计价子 Agent：

```text
你是 Agent-A，当前工作目录是独立 git worktree，分支是 codex/p0-order-pricing。

你只负责：
- T-P0-01 三类订单计价分流
- T-P0-02 优惠券/光年币接入计价

只修改订单预览、订单创建、计价策略、优惠券/光年币抵扣、前端下单参数相关文件。不要修改支付回调编排、合同鉴权、OpenAPI 之外的模块。

完成后运行聚焦验证，并汇总修改文件、验证结果、风险和人工验收点。
```

支付编排子 Agent：

```text
你是 Agent-B，当前工作目录是独立 git worktree，分支是 codex/p0-payment-orchestration。

你只负责：
- T-P0-03 支付成功编排

只修改支付回调、幂等处理、合同生成、会员累计消费/升级、资产入账、分佣生成相关代码。不要修改订单计价策略。

完成后运行聚焦验证，并汇总修改文件、验证结果、风险和人工验收点。
```

合同与契约子 Agent：

```text
你是 Agent-C，当前工作目录是独立 git worktree，分支是 codex/p0-contract-openapi。

你只负责：
- T-P0-04 合同下载鉴权修复
- T-P0-06 OpenAPI 与实现对齐

只修改合同下载鉴权、合同服务查询边界、OpenAPI 契约文档。不要修改支付编排和订单计价。

完成后运行聚焦验证，并汇总修改文件、验证结果、风险和人工验收点。
```

## 11. 母 Agent 如何检查子 Agent 结果

在主项目目录里查看子分支 diff：

```powershell
git fetch --all
git diff main..codex/p0-order-pricing --stat
git diff main..codex/p0-order-pricing
```

如果你的主分支不是 `main`，把 `main` 换成当前基线分支名。

也可以在子 worktree 目录里检查：

```powershell
git status --short
git diff --stat
git diff
```

母 Agent 重点检查：

- 是否只改了负责范围内的文件。
- 是否有无关格式化或大范围重排。
- 是否同步了接口文档。
- 是否加了必要测试。
- 是否有金额、权限、幂等、状态机风险。

## 12. 合并子分支

建议母 Agent 按依赖顺序合并。

示例：

```powershell
git checkout main
git merge --no-ff codex/p0-contract-openapi
git merge --no-ff codex/p0-order-pricing
git merge --no-ff codex/p0-payment-orchestration
```

如果出现冲突：

1. 先停止继续合并其他分支。
2. 让母 Agent 阅读冲突文件。
3. 保留两个分支中都需要的逻辑。
4. 解决后运行聚焦测试。
5. 再继续合并下一个分支。

## 13. 合并后的验证

根据项目类型选择命令。

常见命令：

```powershell
npm test
npm run build
npm run lint
npm run typecheck
```

Java/Spring 项目常见命令：

```powershell
.\mvnw test
.\mvnw package
```

如果项目有统一 CI 命令，优先运行：

```powershell
npm run harness:ci
```

## 14. 清理 worktree

确认子分支已经合并且不再需要对应目录后，可以清理：

```powershell
git worktree remove ..\my-project-agent-order
git worktree remove ..\my-project-agent-payment
git worktree remove ..\my-project-agent-contract
```

如果分支也不需要了：

```powershell
git branch -d codex/p0-order-pricing
git branch -d codex/p0-payment-orchestration
git branch -d codex/p0-contract-openapi
```

如果 Git 提示分支未合并，不要强删，先确认是否还有需要保留的改动。

## 15. 常见问题

### 子 worktree 里看不到任务文档

原因通常是任务文档在主目录里未提交。

解决方式：

- 方式一：把任务文档提交到基线分支后再创建 worktree。
- 方式二：手动复制任务文档到每个子 worktree。

### 子 Agent 改了不该改的文件

处理方式：

1. 让子 Agent 解释为什么需要修改。
2. 如果确实无关，让它在自己的分支里移除这些改动。
3. 母 Agent 不要直接把无关改动合并进主分支。

### 多个子 Agent 都改了同一个文件

处理方式：

1. 判断是否拆分边界不合理。
2. 让后提交的 Agent 基于前一个分支重新调整。
3. 必要时由母 Agent 手动集成。

### 子分支落后主分支

在子 worktree 里运行：

```powershell
git fetch
git merge main
```

如果主分支不是 `main`，换成你的基线分支名。

## 16. 最小可复制流程

```powershell
cd D:\Pro\my-project
git status --short
git worktree add -b codex/task-a ..\my-project-agent-a HEAD
git worktree add -b codex/task-b ..\my-project-agent-b HEAD
git worktree list
```

然后：

1. 主目录打开 Codex，作为母 Agent。
2. `..\my-project-agent-a` 打开 Codex，作为子 Agent-A。
3. `..\my-project-agent-b` 打开 Codex，作为子 Agent-B。
4. 子 Agent 完成后，母 Agent 审查 diff、合并、运行测试。

