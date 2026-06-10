# 20260525 miniapp payment bind fix

## 目标与范围

- 处理小程序本地调试中的三个问题：
  - `POST /api/v1/distribution/bind` 因无效邀请码触发 500。
  - `POST /api/v1/orders/{orderId}/payments/wechat-jsapi` 使用创建订单返回的订单 ID 后出现 404。
  - `POST /api/v1/orders/preview` 在未登录确认订单场景被鉴权拦截为 403，并引发前端费用列表渲染异常。
  - `<wx-image>` 加载 `http://localhost:8080/uploads/images/...` 被微信基础库拒绝。
- 范围限定为分销绑定、订单创建响应 ID、前端上传图片 URL 归一化和 OpenAPI 补充。

## 风险触点与关键决策

- 支付 404 根因判断为雪花订单 ID 超过 JavaScript 安全整数。前端类型已声明 `Order.id` 为 string，但后端 JSON 仍返回 Long 数字，导致小程序运行时精度丢失后再请求支付接口。
- 只将 `OrderResponse.id` 改为字符串，金额字段继续保持 Long/number，避免影响金额计算。
- 分销绑定在写入邀请关系前校验邀请人用户存在，避免把外键错误暴露成 500。
- 本地 `http://localhost` 上传图片在微信小程序中无法作为 `<image>` 源使用；仅对 `/uploads/images/` 的本地 HTTP 图片在小程序运行时降级为包内静态图，不改变 H5 或真实 HTTPS 图片。
- 订单价格试算仅使用公开 SKU 和试算参数，不创建订单或访问用户资源；仅公开 `POST /api/v1/orders/preview`，下单及支付接口继续要求 Bearer 鉴权。
- 前端请求层必须将 HTTP 非 2xx 视为失败，不让空的错误响应进入业务响应渲染链路；页面同时对费用明细缺失做防御性处理。

## 实际改动

- `backend/src/main/java/com/xingqiu/server/order/dto/OrderResponse.java`
  - `id` 从 `Long` 调整为 `String`。
- `backend/src/main/java/com/xingqiu/server/order/service/OrderService.java`
  - 订单响应 `id` 使用 `String.valueOf(order.getId())`。
- `backend/src/main/java/com/xingqiu/server/distribution/dto/BindInviteRequest.java`
  - 增加邀请码非空和最大长度校验。
- `backend/src/main/java/com/xingqiu/server/distribution/controller/DistributionController.java`
  - 对绑定请求启用 `@Valid`。
- `backend/src/main/java/com/xingqiu/server/distribution/service/CommissionService.java`
  - 注入 `UserMapper` 并在插入邀请关系前校验邀请人存在。
- `frontend/src/utils/request.ts`
  - 对小程序本地 `/uploads/images/` HTTP URL 返回本地静态兜底图，避免微信图片组件继续请求 HTTP。
  - 对 HTTP 非 2xx 响应 reject，并优先保留后端错误消息，避免错误体被当作正常业务数据。
- `frontend/src/pages/order/confirm.vue`
  - 对试算返回缺少 `priceBreakdown` 的异常数据降级为空列表，避免页面渲染抛错。
- `backend/src/main/java/com/xingqiu/server/common/config/SecurityConfig.java`
  - 仅放行无需用户身份的订单试算 `POST /api/v1/orders/preview`。
- `docs/api/openapi/openapi.yaml`
  - 补充分销绑定接口契约，并明确订单公开试算端点无需登录。

## 验证证据与结果

- `cd backend && mvn test`
  - 结果：SUCCESS。
- `cd frontend && npm run build:mp-weixin`
  - 结果：SUCCESS。
  - 说明：构建输出包含 Dart Sass legacy JS API deprecation warning，不影响构建。
- `npm run harness:ci`
  - 结果：FAILURE，`tests=5`，`passed=4`，`failed=1`。
  - 失败项：`pages.json registers all translated pages`，原因 `expected 9 pages, got 10`。
  - 判断：失败来自当前工作区已有 `frontend/src/pages.json` 页面数量变化，非本次修复触及的支付、分销绑定或请求封装逻辑。
  - 证据文件：`.harness/ci/latest.json`。

## 未验证事项与回退

- 未在真实微信支付商户配置下验证 JSAPI 下单成功；本次修复解决的是支付接口请求使用错误订单 ID 导致的 404。
- 本地上传图在小程序中使用静态兜底图；生产环境应配置 HTTPS 图片域名后返回真实 HTTPS URL。
- 回退方式：恢复上述文件改动；若只回退图片兜底，不影响后端支付和分销修复。
