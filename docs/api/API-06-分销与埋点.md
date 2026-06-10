# API-06 - 分销与埋点

## 1. 邀请信息

**GET** `/distribution/me` — 分销中心首页信息，返回：

- `overview.pendingCommission`：待结算佣金，含保护期内与已可结算未提现部分
- `overview.withdrawableCommission`：已结算且尚未提现金额
- `overview.withdrawnCommission`：累计已提现金额
- `inviteCode` / `inviteUrl` / `invitePath`
- `teamStats.level1Count` / `teamStats.level2Count` / `teamStats.todayNew` / `teamStats.totalInvite`

**GET** `/distribution/team` — 一级、二级团队统计与成员列表

## 2. 佣金

**GET** `/distribution/commissions?status=PENDING_PROTECT&page=1&pageSize=20`

支持状态：

- `ALL`
- `PENDING_PROTECT`
- `SETTLEABLE`
- `SETTLED`
- `DISPUTE`

返回项补充：

- `orderNo`
- `productName`
- `productImage`
- `sourceType`
- `settleTime`

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
