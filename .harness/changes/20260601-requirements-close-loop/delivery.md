# 需求审计整改闭环交付记录

## 目标与范围

- 目标：统筹三个子 Agent worktree 完成 P0 需求审计整改的审查、验证与有序合并。
- 范围：
  - `T-P0-01` 三类订单计价分流。
  - `T-P0-02` 优惠券/光年币接入计价。
  - `T-P0-03` 支付成功编排。
  - `T-P0-04` 合同下载鉴权修复。
  - `T-P0-06` OpenAPI 与实现对齐。
- 不在本轮代码闭环范围：
  - `T-P0-05` 真实支付配置与联调验收，依赖微信商户、公网回调和真机人工验收。
  - P1/P2 任务，仅记录后续验收风险。

## 任务分工与任务板

| 任务 | 子 Agent / 分支 | 状态 | 审查重点 |
| --- | --- | --- | --- |
| T-P0-04 / T-P0-06 | `D:\Pro\xq-agent-contract` / `codex/p0-contract-openapi` | 已在主工作区集成修复 | 合同下载鉴权、403、OpenAPI 契约一致性、越界文件 |
| T-P0-01 / T-P0-02 | `D:\Pro\xq-agent-order` / `codex/p0-order-pricing` | 已在主工作区集成修复 | 整数分计价、RENT/BUY/SOFTWARE 分流、抵扣边界、前端传参 |
| T-P0-03 | `D:\Pro\xq-agent-payment` / `codex/p0-payment-orchestration` | 已在主工作区集成修复 | 支付回调幂等、合同/会员/资产/分佣联动、重复回调 |

## 风险触点和关键决策

- 金额字段必须使用整数分，禁止浮点金额入库。
- 支付回调、资产入账、合同生成、会员升级、分佣生成必须幂等。
- 合同下载必须校验 `order.userId == currentUserId`，越权返回 403。
- 影响接口以 `docs/api/openapi/openapi.yaml` 为真源，合并前检查契约与实现一致。
- 主工作区当前存在大量未提交改动；合并前必须避免覆盖用户未提交改动。
- 子 worktree 当前包含未跟踪任务说明文档，默认不纳入合并。
- 合并顺序采用：合同鉴权/OpenAPI -> 订单计价 -> 支付编排。

## 当前基线状态

- 主工作区分支：`codex/xingqiu`。
- worktree：
  - `D:\Pro\xq-agent-contract`：`codex/p0-contract-openapi`。
  - `D:\Pro\xq-agent-order`：`codex/p0-order-pricing`。
  - `D:\Pro\xq-agent-payment`：`codex/p0-payment-orchestration`。
- 初始检查结论：
  - 主工作区存在未提交改动，涉及后台、前端、OpenAPI、启动脚本和未跟踪文档/资产。
  - 三个子 worktree 的任务改动均处于未提交状态，分支 HEAD 仍在共同基线 `c0d9b7e`。
  - 三个子 worktree 均包含未跟踪 `docs/requirements-audit-*` 任务文档，合并时默认排除。

## 审查记录

### 合同鉴权 / OpenAPI

- 状态：子 worktree 原 diff 审查未通过；已在主工作区绕过乱码 OpenAPI 后完成必要集成修复。
- Diff：
  - `backend/src/main/java/com/xingqiu/server/contract/controller/ContractController.java`
  - `backend/src/main/java/com/xingqiu/server/contract/service/ContractService.java`
  - `docs/api/openapi/openapi.yaml`
  - 未跟踪测试：`backend/src/test/java/com/xingqiu/server/contract/service/ContractServiceTest.java`
  - 未跟踪任务文档：`docs/requirements-audit-*`，默认排除合并。
- 越界检查：
  - 合同代码改动在任务边界内。
  - OpenAPI 属于任务边界，但当前变更将原有中文说明整体破坏为乱码，并引入 BOM，属于阻断问题。
- 验证：
  - `D:\Pro\xq-agent-contract\backend> mvn -Dtest=ContractServiceTest test`
  - 结果：SUCCESS，Tests run: 3, Failures: 0, Errors: 0, Skipped: 0。
- 风险：
  - 子 worktree 的 `openapi.yaml` 不能以原 diff 合并，否则会污染契约真源；主工作区采用干净契约追加。
  - `ContractController.currentUserId()` 与项目其他控制器的 `Long principal` 方式一致，但缺少 controller 层 403 集成测试；当前只验证了 service 层越权。

### 订单计价

- 状态：子 worktree 未完整覆盖 `T-P0-02`；已在主工作区补齐请求级优惠券/光年币抵扣闭环。
- Diff：
  - `backend/src/main/java/com/xingqiu/server/order/service/PreviewService.java`
  - `backend/src/main/java/com/xingqiu/server/order/service/OrderService.java`
  - `frontend/src/pages/product-detail/index.vue`
  - 未跟踪测试：`backend/src/test/java/com/xingqiu/server/order/service/PreviewServiceTest.java`
  - 未跟踪任务文档：`docs/requirements-audit-*`，默认排除合并。
- 越界检查：
  - 文件范围基本在订单计价与商品详情下单参数边界内。
- 验证：
  - `D:\Pro\xq-agent-order\backend> mvn -Dtest=PreviewServiceTest test`
  - 结果：SUCCESS，Tests run: 4, Failures: 0, Errors: 0, Skipped: 0。
- 风险：
  - 当前抵扣为请求级整数分抵扣并后端封顶，未接入真实优惠券库存、使用状态或光年币余额账户；真实余额校验仍需后续领域模型支持。
  - 已补 `LEASE_BUY` 兼容为租赁价，但仍需业务确认它是否应独立为租赁购买价策略。

### 支付编排

- 状态：子 worktree 幂等保障不足；已在主工作区补充支付回调条件更新抢占，降低重复回调并发双触发风险。
- Diff：
  - `backend/src/main/java/com/xingqiu/server/payment/service/PaymentService.java`
  - `backend/src/main/java/com/xingqiu/server/payment/service/PaymentSuccessOrchestrator.java`（未跟踪）
  - `backend/src/main/java/com/xingqiu/server/asset/service/AssetOrderService.java`（未跟踪）
  - `backend/src/main/java/com/xingqiu/server/distribution/service/CommissionService.java`
  - `backend/src/main/java/com/xingqiu/server/member/service/MemberService.java`
  - `backend/src/main/java/com/xingqiu/server/order/service/OrderService.java`
  - 未跟踪测试：`PaymentServiceTest`、`CommissionServiceTest`
  - 未跟踪任务文档：`docs/requirements-audit-*`，默认排除合并。
- 越界检查：
  - 支付、合同生成、会员、资产、分佣、订单状态审计均与 `T-P0-03` 相关。
  - 修改 `OrderService` 会与订单分支重叠，合并前需要人工集成。
- 验证：
  - 首次执行 `mvn -Dtest=PaymentServiceTest,CommissionServiceTest test` 在 PowerShell 中因逗号参数解析失败，未进入 Maven。
  - 修正为 `mvn '-Dtest=PaymentServiceTest,CommissionServiceTest' test`
  - 结果：SUCCESS，Tests run: 4, Failures: 0, Errors: 0, Skipped: 0。
- 风险：
  - 支付回调入口已通过 `out_trade_no + PENDING` 条件更新保证只有一个回调进入编排。
  - 会员、资产、分佣仍主要是查询去重，缺少数据库唯一键；在支付入口抢占之外的手工重复编排仍可能依赖应用层检查。
  - `PaymentSuccessOrchestrator` 内多领域联动在同一事务中调用合同 PDF/COS 相关逻辑，外部副作用与数据库事务混在一起，失败补偿仍需后续可靠事件化。

## 合并与验证记录

- 合并方式：
  - 未执行 `git merge`。原因是主工作区存在大量未提交改动，且三个子 worktree 均为未提交 diff，直接合并会覆盖或冲突。
  - 已在主工作区手工集成通过审查的必要改动，并排除子 worktree 中未跟踪任务说明文档。
- 实际集成改动：
  - 订单计价：`PreviewService` 按 `RENT/BUY/SOFTWARE` 分流；租赁必须提供合法租期；`LEASE_BUY` 兼容为租赁价；BUY/SOFTWARE 不计押金。
  - 抵扣计价：`PreviewRequest` / `CreateOrderRequest` 增加 `couponDiscountMinor`、`lightYearDiscountMinor`；后端使用整数分并按应付金额封顶；订单落 `discountMinor`。
  - 前端确认页：增加优惠券/光年币选择并透传抵扣金额；`frontend/src/services/order.ts` 同步类型。
  - 合同鉴权：下载地址获取前校验订单归属，非归属用户抛 `FORBIDDEN`。
  - 支付编排：支付回调通过 `out_trade_no + PENDING` 条件更新原子抢占成功状态；只有抢占成功的回调触发订单 PAID、合同、会员、BUY 资产、分佣编排。
  - 下游幂等：合同生成复用已有 `findByOrderId`；会员按 `PAID_ORDER + orderId` 跳过重复累计；资产按 `userId + purchaseOrderId` 跳过；分佣按 `orderId + beneficiaryUserId + level` 跳过。
  - OpenAPI：补齐订单预览/创建、微信支付参数、合同下载关键契约，并标明抵扣字段金额单位为分。
- 验证记录：
  - `D:\Pro\星球出机小程序 - 副本\backend> mvn -Dtest=PreviewServiceTest,ContractServiceTest test`
    - 结果：SUCCESS，Tests run: 7, Failures: 0, Errors: 0, Skipped: 0。
  - `D:\Pro\星球出机小程序 - 副本\backend> mvn -Dtest=PreviewServiceTest,ContractServiceTest,PaymentServiceTest test`
    - 首次结果：FAILURE，`LambdaUpdateWrapper` 在纯单测环境缺少 MyBatis-Plus lambda cache。
    - 修复：改为 `UpdateWrapper` 字符串列名条件更新。
    - 复测结果：SUCCESS，Tests run: 9, Failures: 0, Errors: 0, Skipped: 0。
  - `D:\Pro\星球出机小程序 - 副本\backend> mvn test`
    - 结果：SUCCESS，Tests run: 9, Failures: 0, Errors: 0, Skipped: 0。
  - `D:\Pro\星球出机小程序 - 副本\frontend> npm run build:mp-weixin`
    - 结果：SUCCESS，微信小程序构建完成，输出到 `frontend/dist/build/mp-weixin`。
  - `D:\Pro\星球出机小程序 - 副本> npm run harness:ci`
    - 结果：FAILURE，status=FAILURE，tests=5，passed=4，total=5，failed=1。
    - 失败原因：`pages.json registers all translated pages` 期望 9 个页面，当前主工作区已有 10 个页面。`typeCheck=SUCCESS`，失败不来自本轮订单/支付/合同代码。
- 完整验证：
  - 后端完整 Maven 测试已通过。
  - 前端静态 type-check 已通过（由 `harness:ci` 执行），微信小程序构建已通过。
  - 统一门禁因既有页面数量断言失败，未达到 SUCCESS。

## 未验证事项、人工验收与回退

- 人工验收待执行：
  - `TC-P0-01`：三类订单各 1 笔预览与下单金额核对。
  - `TC-P0-02`：优惠券、光年币、叠加、超额抵扣四组对账。
  - `TC-P0-03`：同一 `out_trade_no` 回调重放 2 次，确认不重复发权益、不重复分佣。
  - `TC-P0-04`：支付成功后核对合同、会员/原住民、资产、佣金记录。
  - `TC-P0-05`：A/B 用户交叉下载同一合同，非归属用户返回 403。
  - `TC-P0-06`：前后端按 OpenAPI 抽样联调，字段差异归零。
- 回退方式：
  - 未合并前：保留各子 worktree，不将未通过审查的改动引入主工作区。
  - 合并后：使用常规 `git revert` 回退对应 merge commit，禁止 `git reset --hard` 或破坏性覆盖用户改动。
