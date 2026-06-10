# 星球出机小程序 — 启动指南（小白版）

一步一步跟着做，每完成一步打一个勾。

---

## 你需要先安装的软件

| 软件 | 下载地址 | 装好后验证 |
|------|---------|-----------|
| **Node.js (v18+)** | https://nodejs.org/ | 终端输入 `node -v` 显示版本号 |
| **微信开发者工具** | https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html | 打开能扫码登录就行 |
| **Docker Desktop** | https://www.docker.com/products/docker-desktop/ | 装好后重启电脑，桌面右下角出现 Docker 图标 |

> Docker 是什么？它像一个"软件盒子"，能把 MySQL 和 Redis 一键装好，不用手动安装配置。

---

## 第一步：启动数据库和缓存（用 Docker）

**1. 打开终端**（按 `Win+R`，输入 `powershell`，回车）

**2. 进入项目根目录**
```powershell
cd "D:\Pro\星球出机小程序 - 副本"
```

**3. 一键启动基础环境**
```powershell
docker compose -p xingqiu up -d
```
看到 `done` 或容器处于 `running` / `healthy` 状态就成功了。

> ⚠️ **重要提示**：这个命令除了启动 MySQL 和 Redis，还会默认启动一个后端的容器 (`xq-backend`)。如果你打算在本地修改和调试后端代码，**必须先把 Docker 里的后端停掉**，否则端口会冲突！
>
> 停止 Docker 中的后端服务：
> ```powershell
> docker stop xq-backend
> ```

---

## 第二步：初始化数据库

首次启动后，需要导入表结构和种子数据：

```powershell
# 导入表结构
docker exec -i xq-mysql mysql -uroot -pxingqiu123 < backend/sql/init.sql

# 导入种子数据（商品、场景、运营配置等）
docker exec -i xq-mysql mysql -uroot -pxingqiu123 xingqiu_dev < backend/src/main/resources/seed-data.sql
```

> 种子数据包含 6 个机器人商品、4 个应用场景、轮播图配置、热搜词等，导入后小程序和管理后台都能看到数据。
> 后端启动时也会自动执行 seed-data.sql（通过 Spring Boot sql.init 机制），所以如果后端正常启动过一次，这步可以跳过。

---

## 第三步：在本地启动后端服务器

**4. 设置必要的环境变量**
```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "xingqiu123"
$env:REDIS_HOST = "localhost"
$env:JWT_SECRET = "dev-secret-change-me"
```

> `DB_PASSWORD` 必须与 Docker 中 MySQL 容器的 `MYSQL_ROOT_PASSWORD` 一致（默认为 `xingqiu123`）。
> JWT_SECRET 是一个安全密钥，随便写一串长一点的英文数字就行（至少32位），但不要告诉别人。

**5. 启动后端**
```powershell
cd "D:\Pro\星球出机小程序 - 副本\backend"
mvn spring-boot:run
```
看到 `Started XqServerApplication` 就表示成功了。**不要关掉这个窗口**。

> 💡 **以后更新后端代码怎么办？**
> 因为你是在本地用 `mvn spring-boot:run` 跑的，你只需要在编辑器里修改了代码后，在这个终端里按 `Ctrl+C` 停止，然后重新运行 `mvn spring-boot:run` 就能应用最新代码。不需要重新操作 Docker！

**6. 验证后端是否正常**

打开浏览器访问以下地址，确认能看到 JSON 数据：
- 商品列表：http://localhost:8080/api/v1/catalog/skus
- 场景列表：http://localhost:8080/api/v1/catalog/scenes
- 运营配置：http://localhost:8080/api/v1/config/app

---

## 第四步：编译并打开小程序

**7. 安装前端依赖（仅第一次）**
```powershell
cd "D:\Pro\星球出机小程序 - 副本\frontend"
npm install
```

**8. 编译小程序**
```powershell
cd "D:\Pro\星球出机小程序 - 副本\frontend"
npm install
npm run build:mp-weixin
```

**9. 打开微信开发者工具**

**10. 点击「+」→「导入项目」**

| 填写项 | 值 |
|--------|-----|
| 项目名称 | 星球出机 |
| 目录 | `D:\Pro\星球出机小程序 - 副本\frontend\dist\build\mp-weixin` |
| AppID | `wxa878c0ebb0e9536a` |

**11. 点击「确定」**

**12. 最重要的一步：关掉域名校验**

点击右上角「详情」→「本地设置」→ 勾选 **「不校验合法域名」**

> 为什么？后端跑在你的电脑上（localhost），微信默认不允许连本地地址，这个选项可以跳过检查。

**13. 在左侧模拟器中，你应该能看到小程序首页了**

> 首页的商品数据、场景卡片、轮播图等内容来自后端数据库。如果看到空白，请确认第三步后端已正常启动。

---

## 第五步：启动后台管理网站

后台管理网站是一个独立的 Vue 3 + Element Plus 项目，用于管理商品、活动、订单、用户、资金、运维、数据分析等。

**1. 打开一个新的终端窗口**（刚才后端的窗口不要关）

**2. 进入后台网页项目目录**
```powershell
cd "D:\Pro\星球出机小程序 - 副本\admin-web"
```

**3. 安装依赖（仅第一次启动需要）**
```powershell
npm install
```

**4. 启动开发服务器**
```powershell
npm run dev
```

启动成功后，终端会显示 `http://localhost:3000/`。
按住 `Ctrl` 键用鼠标点击它，或者复制到浏览器中打开，即可看到后台管理系统。

> 开发模式下 API 请求会自动代理到 `http://localhost:8080`（后端），无需额外配置。

**5. 登录管理后台**

| 字段 | 值 |
|------|-----|
| 用户名 | `admin` |
| 密码 | `admin123` |

**6. 后台功能模块**

| 模块 | 功能 |
|------|------|
| 工作台 | 经营指标、待办事项、GMV 趋势图 |
| 商品信息 | SKU 列表/编辑、价格(整数分)、上下架、场景组合 |
| 活动与运营 | 场景专题、运营位(Banner/卡片)、配置版本发布/回滚 |
| 内容审核 | 审核队列(通过/驳回+原因)、审核记录 |
| 订单与履约 | 订单查询、状态流水、支付/合同详情 |
| 用户与权益 | 用户查询、会员/资产摘要、冻结/解冻(需原因) |
| 资金与分销 | 钱包流水、提现审核(幂等)、佣金与结算批次 |
| 运维管理 | 设备台账、工单(派单/加急/关闭)、巡检计划 |
| 数据分析 | 经营概览、商品分析、运营分析(ECharts 图表) |
| 系统设置 | 管理员(7种角色 RBAC)、角色权限、审计日志 |

修改后小程序端会实时生效（下次打开页面时加载最新数据）。

**7. Docker 一键部署（生产模式）**

如果不想用开发模式，可以直接用 Docker 构建并运行：
```powershell
cd "D:\Pro\星球出机小程序 - 副本"
docker compose up -d admin-web
```
构建完成后访问 `http://localhost:80` 即可使用后台管理网站。

---

## 第六步：更新前端代码

你每次改了前端代码后，运行这行命令重新编译：

```powershell
cd "D:\Pro\星球出机小程序 - 副本\frontend"
npm run build:mp-weixin
```

然后在微信开发者工具中点击「编译」按钮刷新。

---

## 架构说明

```
┌─────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│  微信小程序前端   │────▶│  Spring Boot 后端  │◀────│  后台管理网站     │
│  (UniApp/Vue3)  │     │  (localhost:8080) │     │  (localhost:3000) │
└─────────────────┘     └────────┬─────────┘     └──────────────────┘
                                 │                Vue 3 + Element Plus
                    ┌────────────┼────────────┐   + ECharts + Pinia
                    ▼                         ▼
              ┌──────────┐             ┌──────────┐
              │  MySQL   │             │  Redis   │
              │ (:3306)  │             │ (:6379)  │
              └──────────┘             └──────────┘
```

- 小程序和管理后台共用同一个后端服务（`/api/v1/admin/**` 为管理端接口）
- 商品、场景、轮播图等数据存储在 MySQL 中
- 在管理后台修改的内容，小程序端会自动获取最新数据
- Docker 部署时 admin-web 通过 Nginx 反向代理 API，网络隔离（edge + internal）

---

## 常见问题

| 问题 | 解决办法 |
|------|---------|
| `docker-compose` 找不到 | 检查 Docker Desktop 是否安装并启动（右下角图标） |
| `mvn` 找不到 | 需要安装 JDK 17 和 Maven，或者告诉我帮你用 Docker 方式启动后端 |
| `npm` 找不到 | 需要安装 Node.js（见上方软件列表） |
| 后端启动报错 `Access denied` | 检查 `$env:DB_PASSWORD` 是否与 Docker 中 MySQL 密码一致（默认 `xingqiu123`） |
| 后端启动报错 `Table already exists` | seed-data.sql 使用 INSERT IGNORE，重复执行不会报错。如果是 init.sql 报错，说明表已存在，可以忽略 |
| 小程序首页数据为空 | 确认后端已启动且 seed 数据已导入，浏览器访问 http://localhost:8080/api/v1/catalog/skus 确认有数据 |
| 小程序报 `timeout` / `Cannot read property 'errMsg'` | 1. 确认微信开发者工具已登录微信账号；2. 确认 AppID 已在微信公众平台绑定当前开发者；3. 如果只是本地预览，忽略此错误即可——登录失败不影响页面浏览 |
| 微信开发者工具白屏 | 检查「不校验合法域名」是否勾选 |
| 端口被占用 | 重启电脑，或者关闭占用 3306 / 6379 / 8080 端口的程序 |
| 管理后台登录失败 | 确认后端已启动，且 seed 数据中的 admin 用户已导入 |
| 管理后台页面空白 | 确认 `npm run dev` 正在运行且无报错；检查浏览器控制台是否有 CORS 或网络错误 |
| Docker 构建 admin-web 失败 | 确认 `admin-web/` 目录下已执行过 `npm install`，或检查 Node 版本 ≥ 18 |

---

## 你现在就可以开始做的事情

1. 启动成功后，在模拟器里点点各个页面，确认商品数据正常显示
2. 打开管理后台（`http://localhost:3000`），用 admin/admin123 登录
3. 试试在后台修改轮播图或热搜词，然后在小程序中刷新查看效果
4. 在后台「商品信息」添加一个新 SKU，然后在小程序分类页查看是否出现
5. 查看「数据分析」模块的 ECharts 图表（接入真实数据后会自动展示）
6. 在「系统设置 → 审计日志」中查看所有管理操作记录

---

## 重置数据库（可选）

如果数据库出问题需要从头来过：

```powershell
cd "D:\Pro\星球出机小程序 - 副本"

# 1. 销毁旧数据库（乱码数据无法修复，必须重建）
docker compose -p xingqiu down -v

# 2. 重新启动 MySQL 和 Redis
docker compose -p xingqiu up -d mysql redis

# 3. 等待 MySQL 就绪后重启后端
.\start-dev.ps1

$env:WX_APP_ID = "请填写你的小程序 AppID"
$env:WX_APP_SECRET = "请填写你的小程序 AppSecret"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "xingqiu123"
$env:REDIS_HOST = "localhost"
$env:REDIS_PASSWORD = "redis123"
$env:JWT_SECRET = "dev-secret-change-me"

cd "D:\Pro\星球出机小程序 - 副本\backend"
mvn spring-boot:run

cd "D:\Pro\星球出机小程序 - 副本\frontend"
npm install
npm run build:mp-weixin
