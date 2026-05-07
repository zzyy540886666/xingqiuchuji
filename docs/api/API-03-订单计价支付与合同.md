# API-03 - 订单、计价、支付与合同

## 1. 试算

**POST** `/orders/preview`

请求体含：`skuId`、`orderType`、租期/地址/软件授权类型等。  
响应：`priceBreakdown`、`payableAmount`、`depositAmount`、`allowedCoupons`…

## 2. 创建订单

**POST** `/orders`  
Header：`Idempotency-Key`

请求体与试算结构对齐；响应 `orderId`、`status: PENDING_PAY`。

## 3. 发起支付

**POST** `/orders/{orderId}/payments/wechat-jsapi`

响应 `data`：`timeStamp`、`nonceStr`、`package`、`signType`、`paySign`（微信字段名保持与官方一致可 snake_case 由前端映射，或统一 camelCase 在文档注明）。

## 4. 订单详情与列表

**GET** `/orders/{orderId}`  
**GET** `/orders?type=RENT&status=PAID&page=1&pageSize=20`

## 5. 合同

**GET** `/orders/{orderId}/contract/download-url`  
响应：`url`（短期签名）、`expiresAt`。

## 6. 微信回调（服务端）

**POST** `/internal/pay/wechat/notify`（路径可配置，**不在小程序调用**）

## 7. 错误码（节选）

| code | 说明 |
|------|------|
| ORDER_PRICE_CHANGED | 409 价格变更需重新试算 |
| ORDER_NOT_PAYABLE | 409 |
| PAY_SIGN_FAILED | 502 下游微信错误 |
