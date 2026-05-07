---
name: "xingqiu-data-cache-consistency"
description: "Guides MySQL 8 schema design, Redis cache usage, consistency, idempotency, locking, pagination, audit records, and financial ledger correctness for 星球出机. Use when changing database tables, indexe..."
---
# 星球出机 - 数据与缓存一致性

## 数据建模

- MySQL 8 为交易与业务事实源；Redis 只做缓存、限流、短期状态、锁或队列辅助，不作为最终事实源。
- 表字段、枚举、索引与 `docs/backend/`、`docs/api/openapi/openapi.yaml` 语义一致。
- 金额、钱包、佣金、支付、退款、提现使用整数分与流水表；避免用浮点数表达金额。

## 一致性

- 写数据库后再处理缓存失效或异步刷新；读取缓存必须允许 miss 并能从 DB 重建。
- 对下单、支付回调、提现、佣金结算等关键路径使用幂等键、唯一约束或业务单号防重复。
- 涉及余额、库存式计数、权益次数时，使用事务、乐观锁/悲观锁或原子 SQL，避免读改写竞争。

## Redis 使用

- Redis key 命名包含业务域、版本、资源 id；设置合理 TTL，避免永久脏缓存。
- 分布式锁必须有过期时间、唯一 value、释放校验；不要用锁替代数据库约束。
- 限流、验证码、登录态、内容审核异步状态等短期数据要定义过期与降级策略。

## 查询与分页

- 列表接口必须有稳定排序；分页参数校验范围，避免无上限 `pageSize`。
- 高频查询按访问模式设计联合索引；避免在热路径做无索引模糊查询或大 offset 扫描。