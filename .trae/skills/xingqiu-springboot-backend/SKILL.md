---
name: "xingqiu-springboot-backend"
description: "Guides Java 17 + Spring Boot 3.x backend implementation for 星球出机: REST controllers, DTO validation, service/domain layering, transactional boundaries, scheduled jobs, integrations, and springdoc ..."
---
# 星球出机 - Spring Boot 后端开发

## 目标

用 Java 17 + Spring Boot 3.x 实现自建后端 REST API；业务逻辑在服务端闭环，接口与 `docs/api/openapi/openapi.yaml`、`docs/backend/` 保持一致。

## 分层

- Controller 只负责 HTTP 入参、鉴权上下文、响应转换；复杂业务放 Service/Domain 层。
- DTO 与领域对象分离；入参使用 Bean Validation 校验类型、长度、范围、枚举。
- Repository/Mapper 不拼接不可信 SQL；动态排序、状态、字段名使用白名单映射。

## 事务与状态机

- 下单、支付、钱包、提现、佣金、托管资产等写操作必须明确事务边界。
- 金额统一使用“分”的整数；支付与回调处理必须幂等、可审计、可重放保护。
- 订单、支付、合同、钱包流水等状态迁移以服务端状态机为准，禁止前端直接指定最终状态。

## 集成

- 微信登录、微信支付、腾讯云 COS、腾讯云内容安全、IM、短信等第三方调用封装在独立 client/gateway 层。
- 密钥来自环境变量或安全配置，不写入仓库；错误日志不输出密钥、token、完整身份证明材料。

## OpenAPI

- 新增或修改接口时同步 Controller 注解、DTO Schema、错误码与 `springdoc` 导出的 `openapi.yaml`。
- 若 API narrative 文档与 YAML 冲突，以 YAML 和接口通则为准，并回写说明文档。