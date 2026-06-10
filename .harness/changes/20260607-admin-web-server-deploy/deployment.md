# 整站 Docker 部署说明

目标：

- 服务器：`43.138.148.183`
- 登录：`ssh ubuntu@43.138.148.183`
- 部署内容：后端 `backend` + 管理网站 `admin-web`
- 方式：本地先编译并打包成 Docker 镜像，再上传到服务器运行

这份文档默认你在本地电脑上已经安装了：

- Node.js
- Maven
- Docker Desktop
- OpenSSH 客户端（Windows 一般自带 `ssh` / `scp`）

如果你本地没有 Maven，我在后面也给了备用命令。

## 1. 本地准备目录

在本地仓库根目录执行：

```powershell
Set-Location 'D:\Pro\星球出机小程序 - 副本'
New-Item -ItemType Directory -Force -Path .\releases | Out-Null
New-Item -ItemType Directory -Force -Path .\releases\certs | Out-Null
```

## 2. 在本地生成部署配置文件

先在本地根目录创建 `.env`，部署时 Docker Compose 会自动读取它。

```powershell
@'
DB_USERNAME=root
DB_PASSWORD=xingqiu123
DB_URL=jdbc:mysql://mysql:3306/xingqiu?useUnicode=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci&serverTimezone=Asia/Shanghai
REDIS_PASSWORD=redis123
JWT_SECRET=请填写强随机 JWT 密钥
WX_APP_ID=请填写小程序 AppID
WX_APP_SECRET=请填写小程序 AppSecret
WX_PAY_MCH_ID=
WX_PAY_MERCHANT_SERIAL_NO=
WX_PAY_API_V3_KEY=
WX_PAY_PRIVATE_KEY_PATH=
WX_PAY_PLATFORM_PUBLIC_KEY_PATH=
WX_PAY_NOTIFY_URL=
COS_SECRET_ID=
COS_SECRET_KEY=
COS_REGION=ap-guangzhou
COS_BUCKET=
TMS_SECRET_ID=
TMS_SECRET_KEY=
TMS_REGION=ap-guangzhou
SPRINGDOC_ENABLED=false
SPRING_PROFILES_ACTIVE=prod
'@ | Set-Content -Encoding utf8 .\.env
```

然后把 `.env` 打开，把里面的 `请替换为...` 全部改成你的真实值。

再创建生产用的 Docker Compose 文件：

```powershell
@'
services:
  mysql:
    image: mysql:8.0
    container_name: xq-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
      MYSQL_DATABASE: xingqiu
    volumes:
      - mysql_data:/var/lib/mysql
      - ./backend/sql/init.sql:/docker-entrypoint-initdb.d/01-init.sql:ro
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci --character-set-client-handshake=FALSE --init-connect='SET NAMES utf8mb4' --default-time-zone=+08:00
    networks:
      - internal
    healthcheck:
      test: ["CMD-SHELL", "mysqladmin ping -h localhost -uroot -p${DB_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: xq-redis
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis_data:/data
    networks:
      - internal
    healthcheck:
      test: ["CMD", "redis-cli", "-a", "${REDIS_PASSWORD}", "ping"]
      interval: 5s
      timeout: 3s
      retries: 5

  backend:
    image: xingqiu-backend:prod
    container_name: xq-backend
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: ${DB_URL}
      SPRING_DATASOURCE_USERNAME: ${DB_USERNAME}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_DATA_REDIS_HOST: redis
      SPRING_DATA_REDIS_PASSWORD: ${REDIS_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      WX_APP_ID: ${WX_APP_ID}
      WX_APP_SECRET: ${WX_APP_SECRET}
      WX_PAY_MCH_ID: ${WX_PAY_MCH_ID}
      WX_PAY_MERCHANT_SERIAL_NO: ${WX_PAY_MERCHANT_SERIAL_NO}
      WX_PAY_API_V3_KEY: ${WX_PAY_API_V3_KEY}
      WX_PAY_PRIVATE_KEY_PATH: ${WX_PAY_PRIVATE_KEY_PATH}
      WX_PAY_PLATFORM_PUBLIC_KEY_PATH: ${WX_PAY_PLATFORM_PUBLIC_KEY_PATH}
      WX_PAY_NOTIFY_URL: ${WX_PAY_NOTIFY_URL}
      COS_SECRET_ID: ${COS_SECRET_ID}
      COS_SECRET_KEY: ${COS_SECRET_KEY}
      COS_REGION: ${COS_REGION}
      COS_BUCKET: ${COS_BUCKET}
      TMS_SECRET_ID: ${TMS_SECRET_ID}
      TMS_SECRET_KEY: ${TMS_SECRET_KEY}
      TMS_REGION: ${TMS_REGION}
      SPRINGDOC_ENABLED: ${SPRINGDOC_ENABLED}
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE}
    ports:
      - "127.0.0.1:8328:8080"
    volumes:
      - ./certs:/app/certs:ro
    networks:
      - internal
      - edge
    restart: unless-stopped

  admin-web:
    image: xingqiu-admin-web:prod
    container_name: xq-admin-web
    ports:
      - "127.0.0.1:5137:80"
    networks:
      - edge
    restart: unless-stopped

networks:
  edge: {}
  internal:
    internal: true

volumes:
  mysql_data:
  redis_data:
'@ | Set-Content -Encoding utf8 .\docker-compose.prod.yml
```

## 3. 本地编译后台管理网站

在本地仓库根目录执行：

```powershell
Set-Location 'D:\Pro\星球出机小程序 - 副本\admin-web'
npm ci
npm run build
docker build -t xingqiu-admin-web:prod .
```

## 4. 本地编译后端

在本地仓库根目录执行：

```powershell
Set-Location 'D:\Pro\星球出机小程序 - 副本\backend'
mvn -DskipTests clean package
docker build -t xingqiu-backend:prod .
```

如果你本地没有 Maven，可以用这个替代：

```powershell
Set-Location 'D:\Pro\星球出机小程序 - 副本\backend'
docker run --rm -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-17 mvn -DskipTests clean package
docker build -t xingqiu-backend:prod .
```

## 5. 导出本地镜像

回到仓库根目录，把两个镜像导出成 tar 包：

```powershell
Set-Location 'D:\Pro\星球出机小程序 - 副本'
docker save -o .\releases\admin-web-prod.tar xingqiu-admin-web:prod
docker save -o .\releases\backend-prod.tar xingqiu-backend:prod
```

如果你有微信支付证书，也把证书准备好：

```powershell
Copy-Item .\certs\* .\releases\certs\ -Force
```

## 6. 上传到服务器

先创建服务器目录：

```powershell
ssh ubuntu@43.138.148.183 "mkdir -p /opt/xingqiu/releases /opt/xingqiu/certs /opt/xingqiu/backend/sql"
```

然后把文件上传过去：

```powershell
scp .\.env ubuntu@43.138.148.183:/opt/xingqiu/.env
scp .\docker-compose.prod.yml ubuntu@43.138.148.183:/opt/xingqiu/docker-compose.prod.yml
scp .\backend\sql\init.sql ubuntu@43.138.148.183:/opt/xingqiu/backend/sql/init.sql
scp .\releases\admin-web-prod.tar ubuntu@43.138.148.183:/opt/xingqiu/releases/
scp .\releases\backend-prod.tar ubuntu@43.138.148.183:/opt/xingqiu/releases/
scp .\releases\certs\* ubuntu@43.138.148.183:/opt/xingqiu/certs/
```

如果你没有证书文件，可以先跳过最后一行。

## 7. 在服务器上加载镜像

登录服务器：

```powershell
ssh ubuntu@43.138.148.183
```

然后在服务器上执行：

```bash
cd /opt/xingqiu
docker load -i /opt/xingqiu/releases/admin-web-prod.tar
docker load -i /opt/xingqiu/releases/backend-prod.tar
```

## 8. 在服务器上启动整套服务

还是在服务器 `/opt/xingqiu` 目录下执行：

```bash
docker compose -f docker-compose.prod.yml up -d
```

第一次启动时，Docker 会自动拉取 `mysql:8.0` 和 `redis:7-alpine`，然后启动：

- `mysql`
- `redis`
- `backend`
- `admin-web`

## 9. 检查运行状态

看容器是否都起来了：

```bash
docker compose -f docker-compose.prod.yml ps
```

看日志：

```bash
docker logs -f xq-backend
docker logs -f xq-admin-web
```

看管理网站是否正常：

```bash
curl -I http://127.0.0.1:5137/healthz
```

看后端是否能响应：

```bash
curl -I http://127.0.0.1:8328/api/v1/admin/auth/me
```

未登录时返回 `401` 或 `403` 都是正常的，说明后端路径通了。

## 10. 访问方式

先直接访问本机端口测试：

- 管理网站：`http://43.138.148.183:5137`
- 后端接口：`http://43.138.148.183:8328`

如果你想绑定域名，再把服务器上的 Nginx 配成反向代理到 `127.0.0.1:5137`。如果你需要，我可以再给你单独生成一份“域名版 Nginx 配置”。

## 11. 常见问题

### 11.1 `docker compose` 起不来

先看：

```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs --tail=100
```

### 11.2 网站能打开，但接口报错

优先检查：

```bash
docker logs -f xq-backend
```

然后确认 `.env` 里的这些值是否正确：

- `DB_URL`
- `DB_PASSWORD`
- `REDIS_PASSWORD`
- `JWT_SECRET`
- 微信支付相关配置

### 11.3 服务器端口冲突

如果 `5137` 或 `8328` 被占用，改 `docker-compose.prod.yml` 里的端口映射就行。

## 12. 回滚

如果新版本有问题，在服务器上执行：

```bash
cd /opt/xingqiu
docker compose -f docker-compose.prod.yml down
```

然后把旧版本的 tar 重新 `docker load`，再 `docker compose -f docker-compose.prod.yml up -d`。
