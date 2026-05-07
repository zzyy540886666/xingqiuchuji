# API-02 - 商品、场景与搜索

## 1. SKU 列表

**GET** `/catalog/skus`

Query：

| 参数 | 说明 |
|------|------|
| type | `RENT` \| `BUY` \| `SOFTWARE` |
| brandId | 可选 |
| minPrice / maxPrice | 可选，单位分 |
| modelId | 适配机型，可选 |
| page / pageSize | 页码分页 |

## 2. SKU 详情

**GET** `/catalog/skus/{skuId}`

`data` 含：`media`、`specs`、`price`（租阶梯/买一口价/软件订阅）、`availability`。

## 3. 场景聚合

**GET** `/catalog/scenes/{sceneId}/bundles`

返回可租/可买/软件组合卡片列表。

## 4. 搜索

**GET** `/catalog/search?q=...&filters=...`

`filters` 可为 JSON 字符串或重复 query（实现时 OpenAPI 定稿）。

## 5. 错误码（节选）

| code | 说明 |
|------|------|
| SKU_NOT_FOUND | 404 |
| SKU_OFFLINE | 400 |
