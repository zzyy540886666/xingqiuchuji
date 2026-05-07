# API-06 - 分销与埋点

## 1. 邀请信息

**GET** `/distribution/me` — 我的邀请码、海报配置 URL  
**GET** `/distribution/team` — 一级二级统计

## 2. 佣金

**GET** `/distribution/commissions?status=PENDING_SETTLE&page=`

## 3. 分销提现（若与钱包分接口）

可与 [API-04](./API-04-会员钱包托管.md) 合并为统一提现，此处保留 **佣金明细** 只读接口。

## 4. 埋点

**POST** `/analytics/events`

请求体：

```json
{
  "events": [
    { "name": "page_view", "props": { "path": "/pages/index" }, "ts": 1710000000000 }
  ]
}
```

支持批量；服务端可采样存储。

## 5. 错误码（节选）

| code | 说明 |
|------|------|
| DISTRIBUTION_SELF_BIND | 400 不可绑定自己 |
