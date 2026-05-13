---
name: unit-test-write
description: >-
  单测编写 Skill。改了哪个接口就测哪个接口，使用线上真实数据特征构造测试样例并接入 CI 门禁。
disable-model-invocation: false
---

# Unit Test Write Skill

## 基本要求

- 改了哪个接口就测哪个
- 测试数据结构贴近线上真实数据特征
- 测试覆盖正常路径、异常路径、边界条件

## 执行步骤

1. 基于变更接口列出测试清单
2. 构造接近线上分布的测试样例（脱敏、合法）
3. 编写单测与必要集成测试
4. 本地执行并记录测试数量与通过率
5. 接入 CI 校验并产出结果

## CI 程序化门禁

- `status=SUCCESS`
- `tests>0`
- `passed=total`

任意条件不满足必须回退，不允许带病进入部署。

## 产出物

- `changes/<需求ID>/05-CI结果.md`
