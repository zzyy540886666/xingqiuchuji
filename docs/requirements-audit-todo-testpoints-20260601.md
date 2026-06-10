# 星球出机小程序需求核对与后续任务（2026-06-01）

## 1. 核对范围

- 需求基线：`docs/星球出机小程序需求文档 V1.docx`（正文标题为 V1.1 更新版）
- 核对代码范围：
  - `backend/src/main/java`
  - `backend/src/main/resources/application.yml`
  - `frontend/src`
  - `admin-web/src`
  - `docs/api/openapi/openapi.yaml`

## 2. 当前实现结论（按“三流”）

### 2.1 交易流（租/买/软件）

- 已有能力
  - 商品列表、详情、场景与筛选基础能力存在。
  - 订单预览、创建、列表、详情、取消、微信支付参数获取与回调基础流程存在。
- 主要缺口
  - 计价仍按“租赁模型”统一计算，未按 RENT/BUY/SOFTWARE 分流。
  - 优惠券/光年币未进入计价与应付金额。
  - 支付成功后仅改订单状态，未触发合同、会员、资产、分佣联动。
  - 订单履约能力缺失（续租、物流、收货、售后、软件激活/下载）。

### 2.2 身份流（原住民/会员/资产/钱包）

- 已有能力
  - 会员、星球卡、钱包、提现、资产托管基础模型与接口存在。
- 主要缺口
  - 会员阈值与文档不一致，且前后端均有硬编码。
  - 星球卡开通走钱包扣款，不是微信支付闭环。
  - 购买机器人后未自动生成用户资产。
  - 托管价格由用户输入，未实现平台统一定价。

### 2.3 社群流（社区/IM/分销）

- 已有能力
  - 社区 Feed、发帖、圈子、分销关系绑定与佣金结算任务框架存在。
- 主要缺口
  - 社区点赞/收藏/关注主要为前端本地状态，评论与激励闭环未打通。
  - IM 页面当前读取通知数据，未接入真实 IM 会话收发。
  - 佣金生成方法存在，但未由支付成功事件触发。
  - 推广海报、异常佣金冻结、领主奖励等未落地。

## 3. 已核对的关键证据

- 统一租赁计价（未按订单类型分流）：`backend/src/main/java/com/xingqiu/server/order/service/PreviewService.java:41`
- 订单创建固定 `discountMinor=0`：`backend/src/main/java/com/xingqiu/server/order/service/OrderService.java:79`
- 支付回调仅更新为 PAID：`backend/src/main/java/com/xingqiu/server/payment/service/PaymentService.java:145`
- 商品详情下单仅 BUY/RENT，未走 SOFTWARE：`frontend/src/pages/product-detail/index.vue:178`
- 订单详情操作仅取消/支付：`frontend/src/pages/order/detail.vue:43`
- 合同下载未校验订单归属：`backend/src/main/java/com/xingqiu/server/contract/controller/ContractController.java:20`
- 合同服务仅按 orderId 查合同：`backend/src/main/java/com/xingqiu/server/contract/service/ContractService.java:109`
- 会员阈值硬编码为 100/500/2000：`backend/src/main/java/com/xingqiu/server/member/service/MemberService.java:35`
- 星球卡走钱包扣款：`backend/src/main/java/com/xingqiu/server/member/service/MemberService.java:144`
- 托管日价由用户输入并入库：`frontend/src/pages/asset/trusteeship.vue:13`、`backend/src/main/java/com/xingqiu/server/asset/service/TrusteeshipService.java:106`
- IM 页面读取通知接口：`frontend/src/pages/im/conversations.vue:28`、`frontend/src/pages/im/chat.vue:17`
- TMS 失败默认 PASS：`backend/src/main/java/com/xingqiu/server/community/adapter/TmsModerationClient.java:89`
- 托管巡检任务为占位：`backend/src/main/java/com/xingqiu/server/job/scheduler/ScheduledTasks.java:107`
- 微信支付回调地址默认 localhost：`backend/src/main/resources/application.yml:75`
- OpenAPI 仍是契约快照，订单核心接口未完整覆盖：`docs/api/openapi/openapi.yaml:1`
- 自动化测试覆盖基本为空：
  - `backend/src/test => 0`
  - `frontend/src => 0`
  - `admin-web/src => 0`

## 4. 后续任务清单（建议优先级）

## P0（先打通可交易闭环）

1. `T-P0-01` 三类订单计价分流（RENT/BUY/SOFTWARE）
   - 后端：重构 `PreviewService` 与 `OrderService.createOrder`，引入按订单类型的计价策略。
   - 前端：下单参数补齐软件授权类型；商品详情下单映射修正 SOFTWARE。
   - 验收：三类订单各 1 笔预览与下单金额正确。
2. `T-P0-02` 优惠券/光年币接入计价
   - 后端：新增优惠计算与抵扣明细，落订单字段。
   - 前端：确认页支持选择并透传。
   - 验收：应付金额=原价-抵扣，支付金额与账单一致。
3. `T-P0-03` 支付成功编排
   - 在回调后增加统一编排：合同生成、原住民标记、会员累计消费/升级、资产入账、分佣生成。
   - 验收：同一笔支付完整触发，重复回调保持幂等。
4. `T-P0-04` 合同下载鉴权修复
   - 下载前校验 `order.userId == currentUserId`。
   - 验收：越权访问返回 403。
5. `T-P0-05` 支付配置与联调验收
   - 完成真实商户配置、回调公网化、联调记录。
   - 验收：微信沙箱/实测链路跑通下单-支付-回调。
6. `T-P0-06` OpenAPI 与实现对齐
   - 补齐订单、支付、合同、会员、钱包、资产、分销关键接口。
   - 验收：前后端按契约联调通过。

## P1（补齐身份流与履约能力）

1. `T-P1-01` 订单履约与售后能力
   - 增加续租、物流、确认收货、售后、软件激活码/下载链接。
2. `T-P1-02` 会员规则动态化
   - 统一读取运营配置，移除前后端硬编码阈值与文案。
3. `T-P1-03` 星球卡接入微信支付
   - 改为订单+支付成功后发权益，废弃直接钱包扣款路径。
4. `T-P1-04` 购买后自动入资产
   - BUY 类订单完成后写入 `UserAsset`。
5. `T-P1-05` 托管平台定价
   - 前端去掉用户输入价格；后端按平台规则返回并强校验。
6. `T-P1-06` 托管巡检与收益闭环
   - 完成 `inspectTrusteeship` 实际逻辑与异常处理。

## P2（社群增长与治理完善）

1. `T-P2-01` 社区真实互动
   - 点赞/收藏/评论改为服务端落库与计数回写。
2. `T-P2-02` IM 真正落地
   - 接入融云或 ZEGO，会话、私信、群聊、已读、重连。
3. `T-P2-03` 分销运营能力补齐
   - 邀请海报、异常佣金冻结、领主奖励、风控规则。
4. `T-P2-04` 内容审核降级策略
   - TMS 异常改为待人工审核，不可默认放行。

## 5. 测试点清单（按任务直接可执行）

## P0 测试点

1. `TC-P0-01` 计价正确性
   - RENT：不同租期阶梯价、押金、运费、总价。
   - BUY：无租期、无租金、仅买断价与应付。
   - SOFTWARE：授权类型对应价格与权益字段。
2. `TC-P0-02` 抵扣计算
   - 优惠券单独使用、光年币单独使用、叠加使用、超额抵扣边界。
3. `TC-P0-03` 支付回调幂等
   - 同一 `out_trade_no` 重放回调不重复发权益、不重复分佣。
4. `TC-P0-04` 支付后联动
   - 校验合同记录、会员等级/原住民、资产记录、佣金记录同时产生。
5. `TC-P0-05` 合同安全
   - 非订单所属用户下载合同应失败（403）。
6. `TC-P0-06` 契约一致性
   - OpenAPI 与实际接口字段、错误码、鉴权要求一致。

## P1 测试点

1. `TC-P1-01` 履约状态机
   - 状态流转：已支付->待履约->履约中/已发货->已完成；非法流转拒绝。
2. `TC-P1-02` 软件交付
   - 支付成功后可见激活码/下载链接，重复查询一致。
3. `TC-P1-03` 会员规则
   - 动态配置更新后，升级阈值和展示文案一致生效。
4. `TC-P1-04` 星球卡支付
   - 下单前不发权益，支付成功后即时生效，失败不生效。
5. `TC-P1-05` 资产与托管
   - BUY 完成后资产生成；托管价格不可前端篡改；时段冲突阻断。

## P2 测试点

1. `TC-P2-01` 社区互动一致性
   - 点赞/收藏/评论跨端可见，计数准确，撤销正确回滚。
2. `TC-P2-02` IM 能力
   - 单聊、群聊、图片消息、离线重连、未读数一致。
3. `TC-P2-03` 分销闭环
   - 邀请绑定后下单，佣金生成->保护期->可结算->已结算->钱包入账。
4. `TC-P2-04` 内容审核可靠性
   - TMS 异常时帖子进入待审，不可直接公开。

## 6. 建议执行顺序

1. 先完成 `T-P0-01 ~ T-P0-06`，确保交易闭环可验收。
2. 再做 `T-P1-*`，补齐会员/资产/履约核心价值。
3. 最后推进 `T-P2-*`，完善增长与治理能力。
