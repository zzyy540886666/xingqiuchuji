# BE-12 - 定时任务、消息与 IM 集成

## 1. 功能设计

- **定时**：支付超时关单、佣金 T+7 结算、托管状态巡检、对账文件生成（可选）。
- **IM**：签发厂商用户 Token；回调同步群组变更（若需要）。

## 2. 后端架构

- Spring `@Scheduled` + 分布式锁（Redis）保证多实例单执行；或迁移到 XXL-Job / 云定时触发 HTTP。
- `ImTokenService` 调融云/即构服务端 API。

## 3. 持久化要点

- `scheduled_job_runs`（可选）：记录水位。

## 4. 接口文档

[api/API-05-社区与消息.md](../api/API-05-社区与消息.md) IM 节；埋点见 [api/API-06-分销与埋点.md](../api/API-06-分销与埋点.md)。
