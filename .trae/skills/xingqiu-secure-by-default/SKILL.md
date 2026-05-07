---
name: "xingqiu-secure-by-default"
description: "Threat-oriented hardening for APIs and miniprogram usage—treat every request parameter as hostile, prevent SQL injection and XSS, reduce IDOR/abuse risk. Use when implementing auth, queries, HTML..."
---
# 星球出机 - 安全默认项

## 立场

默认所有入参（query、body、路径 id、header）均可被伪造或篡改；默认客户端可被逆向。授权与敏感逻辑必须在服务端强制执行。

## 服务端（自建后端）

- **注入**：使用参数化查询或 ORM 绑定参数；禁止字符串拼接 SQL。对排序字段、表名等若必须动态，使用严格白名单映射。
- **鉴权与对象访问**：每个受保护资源校验「当前用户是否有权操作该 id」；禁止仅靠自增 id 且相信前端传的 userId。
- **输入校验**：类型、长度、枚举、数值范围；拒绝未知字段或记录审计（按项目策略）。对 id 使用服务端解析后的 UUID/整型，再查库。
- **速率与滥用**：登录、短信、下单、上传等敏感接口限流或人机校验（按业务需要）。
- **错误与日志**：对外错误信息通用化；详细原因进日志并关联 traceId，不含密钥与完整 SQL。
- **文件上传**：限制类型与大小；存储桶私有；下载走签名 URL 或经权限校验的代理。

## 小程序端（配合面）

- 不在代码或存储中放置长期有效的管理员密钥；`wx.setStorage` 仅存必要令牌并考虑加密存储能力（按基础库与方案）。
- 展示用户生成内容时避免不安全的富文本组件滥用；需要富文本时白名单标签与属性。
- URL 与深度链接参数同样视为不可信，提交前仍以后端校验为准。

## 快速审查问题（改接口时自问）

1. 删除「信任前端 userId」的路径了吗？
2. 所有 DB 访问都是绑定参数吗？
3. 资源 id 是否都经过「归属校验」？
4. 新输出到页面的字符串是否评估 XSS 面？
5. 管理类操作是否独立鉴权与审计？