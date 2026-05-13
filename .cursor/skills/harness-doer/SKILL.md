---
name: harness-doer
description: >-
  执行型 Harness Skill。用于实现、修复、重构、联调、补测试、准备部署等“做事”任务。
  进入执行任务时先读取 AGENT-ROLE.md 与 AGENT-DOER.md，并按十阶段与门禁推进。
disable-model-invocation: false
---

# Harness Doer

## 使用前置

1. 读取 `AGENT-ROLE.md`
2. 读取 `AGENT-DOER.md`
3. 读取 `.harness/rules/*`
4. 定位或创建 `.harness/changes/<需求ID>/`

## 执行阶段

1. 需求分析
2. 任务拆分
3. 编码实现
4. 单测编写
5. 部署准备
6. 部署执行

## 强制动作

1. 每阶段记录产物到 `changes/<需求ID>/`
2. 不跳过任何门禁
3. 失败按精确路由回退
4. 输出待评审清单交给 reviewer

## 禁止动作

1. 禁止自判最终通过
2. 禁止跳过 CI 程序化门禁
3. 禁止删除历史评审版本
