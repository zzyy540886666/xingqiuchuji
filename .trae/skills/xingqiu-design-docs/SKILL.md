---
name: "xingqiu-design-docs"
description: "Maps 星球出机 features to design docs under docs/frontend, docs/backend, docs/api, and docs/architecture. Use when implementing or changing miniprogram pages, Spring Boot APIs, or API contracts so be..."
---
# 星球出机设计文档索引（Agent 用）

## 何时读取

- 新增/修改小程序页面、接口、订单、支付、社区、分销、会员、钱包、托管时，先打开对应 **FE-** / **BE-** / **API-** 文档再写代码。

## 模块对照表

| 业务 | 前端 | 后端 | API |
|------|------|------|-----|
| 壳、登录、配置 | FE-01 | BE-01, BE-11 | API-01 |
| 货架、搜索、场景 | FE-02 | BE-02 | API-02 |
| 商品详情 | FE-03 | BE-02 | API-02 |
| 下单、支付、合同 | FE-04 | BE-03, BE-04, BE-05 | API-03 |
| 订单中心 | FE-05 | BE-03 | API-03 |
| 会员、星球卡 | FE-06 | BE-06 | API-04 |
| 钱包、提现 | FE-07 | BE-07 | API-04 |
| 托管、资产 | FE-08 | BE-08 | API-04 |
| 社区、审核 | FE-09 | BE-09 | API-05 |
| IM | FE-10 | BE-12 | API-05 |
| 分销 | FE-11 | BE-10 | API-06 |
| 公共、埋点 | FE-12 | BE-12 | API-06 |

## 硬性约定

- 路径与 Schema 以 `docs/api/openapi/openapi.yaml` 为准；全局约定见 `docs/api/00-接口通则.md`（camelCase、鉴权、分页、幂等）。
- 不采用微信云开发；业务逻辑在 Spring Boot。