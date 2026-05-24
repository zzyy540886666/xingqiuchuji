# 星球出机后台管理网站部署说明

## 本地开发

```bash
cd admin-web
npm install
npm run dev
```

开发服务器启动在 http://localhost:3000，API 请求代理到 http://localhost:8080。

## Docker 部署

### 构建镜像

```bash
docker compose build admin-web
```

### 启动全部服务

```bash
docker compose up -d
```

### 仅启动后台管理

```bash
docker compose up -d admin-web backend mysql redis
```

## 生产部署注意事项

1. 替换 `.env` 中所有默认密码和密钥
2. 配置 TLS 证书（在 nginx.conf 中启用 443 端口）
3. 移除 MySQL/Redis 的公网端口映射
4. 设置 `SPRINGDOC_ENABLED=false` 关闭 API 文档
5. 配置数据库定期备份
