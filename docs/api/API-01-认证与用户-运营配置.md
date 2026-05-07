# API-01 - 认证、用户与运营配置

前缀：`/api/v1`。通则见 [00-接口通则.md](./00-接口通则.md)。

## 1. 微信登录

**POST** `/auth/wechat`

请求：

```json
{
  "code": "string from wx.login",
  "inviteCode": "optional string"
}
```

响应 `data`：

```json
{
  "accessToken": "jwt-or-opaque",
  "expiresIn": 7200,
  "user": {
    "id": "snowflake-or-uuid",
    "nickname": "string",
    "avatarUrl": "https://...",
    "membershipLevel": 1,
    "isNativeResident": true
  }
}
```

## 2. 当前用户

**GET** `/users/me`  
Header：`Authorization`

## 3. 运营配置聚合

**GET** `/config/app?clientVersion=12`

响应 `data` 示例结构（字段以实际运营为准）：

```json
{
  "version": 13,
  "membershipRules": [],
  "planetCards": [],
  "topics": [],
  "qaBlocks": {},
  "trusteeshipPricingNote": "string"
}
```

## 4. 错误码（节选）

| code | HTTP | 说明 |
|------|------|------|
| AUTH_INVALID_CODE | 400 | code 无效或过期 |
| AUTH_USER_DISABLED | 403 | 账号冻结 |
