# API-04 - 会员、钱包与托管资产

## 1. 会员摘要

**GET** `/users/me/membership`

含：`level`、`benefits`、`nativeResident`、`planetCard` 等。

## 2. 购买星球卡

**POST** `/membership/planet-cards/{skuId}/purchase`  
返回支付参数或订单 id（与支付模块对齐）。

## 3. 钱包

**GET** `/wallet` — 余额、币种说明  
**GET** `/wallet/ledger?cursor=&limit=`  
**POST** `/wallet/withdraw` — body：`amountMinor`、`Idempotency-Key`

## 4. 分身资产

**GET** `/assets/robots`  
**GET** `/assets/robots/{assetId}`  
**POST** `/assets/robots/{assetId}/trusteeship` — 上架时段  
**GET** `/assets/robots/{assetId}/dashboard`

## 5. 错误码（节选）

| code | 说明 |
|------|------|
| WITHDRAW_AMOUNT_INVALID | 400 |
| ASSET_SLOT_CONFLICT | 409 |
