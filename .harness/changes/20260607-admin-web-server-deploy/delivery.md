# 管理网站与后端 Docker 部署交付

## 目标

将后台管理网站与后端服务部署到 `43.138.148.183`，采用 Docker 运行，保证网站与后端都可以在服务器上启动并对外提供服务。

## 范围

- 后台管理网站 `admin-web`
- 后端服务 `backend`
- Docker Compose 生产部署方案
- 服务器侧 Nginx 反向代理方案
- 运行参数、健康检查与回滚路径

## 关键决策

- 管理站点采用 `admin-web` 现有 Vite + Nginx 镜像，不再单独手工拷贝静态文件。
- 后端服务与网站在同一台服务器上运行，但仅将管理站点暴露到公网，后端默认只做本机回环暴露或 Docker 内网暴露，避免和现有端口冲突。
- MySQL 与 Redis 继续使用 Docker 容器，但生产环境不建议直接公网映射端口。
- 后端生产配置使用 `spring.profiles.active=prod`，并关闭 `springdoc` 在线文档。
- 后端 Dockerfile 不是多阶段构建，发布前必须先生成 `backend/target/*.jar`，再执行镜像构建。

## 实际改动

- 新增本次部署交付记录。
- 新增生产部署说明，覆盖：
  - 服务器准备
  - Docker Compose 覆盖配置
  - 后端 JAR 构建
  - Nginx 反代
  - 健康检查
  - 回滚步骤

## 验证

- `npm run harness:ci` 执行过，结果为 `FAILURE`，`tests=5`，`passed=3`，`failed=2`。
- 失败项来自现有前端单测：
  - `pages.json registers all translated pages`
  - `translated pages preserve core original labels`
- 这些失败与本次部署文档本身无关，但意味着仓库整体门禁未完全通过，实际发布前仍应按文档中的业务烟雾测试再做一次现场确认。

## 风险

- 如果服务器上已有服务占用 `80/8080/3306/6379/3000/8327` 等端口，不能直接沿用默认 Compose 端口映射，必须使用回环端口或改成内部网络。
- 后端 JAR 未预先构建时，`backend` 镜像无法成功创建。
- Nginx 反代错误会导致站点可访问但 API 失败，或站点本身不可用，需要分别检查静态站点与 `/api/v1/admin/**`。

