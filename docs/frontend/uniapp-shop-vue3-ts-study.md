# `uniapp-shop-vue3-ts` 架构学习与当前项目优化说明

## 1. 分析范围

- 参考仓库：`https://gitee.com/Megasu/uniapp-shop-vue3-ts`
- 拉取时间：2026-06-08
- 参考仓库最新提交日期：2025-08-27
- 当前项目：星球出机小程序，UniApp Vue 3 + TypeScript + Pinia，后端为 Spring Boot 3

参考仓库只包含小程序/H5/App 前端和 `uniCloud-aliyun` 目录，没有其业务 REST API 服务端源码。因此本文对参考项目的数据流分析止于公开的 HTTP 接口调用，不推测服务端数据库、事务或领域实现。

## 2. 参考项目架构

### 2.1 目录分层

| 层 | 目录 | 职责 |
| --- | --- | --- |
| 应用入口 | `src/main.ts`、`src/App.vue` | 创建 Vue 应用、注册 Pinia、处理应用生命周期 |
| 页面与路由 | `src/pages`、`src/pagesMember`、`src/pagesOrder`、`src/pages.json` | 主包页面、会员分包、订单分包和 TabBar |
| 页面组件 | 各页面的 `components/` | 页面私有面板、骨架屏、订单列表等 |
| 全局组件 | `src/components` | 轮播、猜你喜欢、SKU 弹窗等跨页面组件 |
| 请求服务 | `src/services` | 按业务域封装 REST API |
| 状态管理 | `src/stores` | 登录会员、临时选中地址等跨页面状态 |
| 组合逻辑 | `src/composables` | 复用滚动触底、分页组件引用等页面逻辑 |
| 类型 | `src/types` | API 参数、响应和组件实例类型 |
| 基础设施 | `src/utils/http.ts` | base URL、超时、请求头、token、401 和错误提示 |

整体调用方向是：

```text
页面/组件 -> services -> utils/http -> 外部 REST API
页面/组件 <-> Pinia store <-> 小程序本地存储
```

页面不直接拼接完整服务器地址，业务接口集中在 `services`；类型定义与请求函数分离，页面只处理展示状态和用户交互。

### 2.2 Git 历史与热点

浅克隆分析了最近 100 个提交，时间跨度为 2023-04-19 至 2025-08-27。变更最频繁的业务文件是：

1. `src/pagesOrder/list/components/OrderList.vue`
2. `src/pagesOrder/detail/detail.vue`
3. `src/pages/login/login.vue`
4. `src/pages/goods/goods.vue`
5. `src/pages/cart/components/CartMain.vue`

这些热点集中在订单、登录、商品详情和购物车，说明交易链路是该模板复杂度最高、最需要重点测试的区域。

## 3. 页面切换机制

### 3.1 路由注册

参考项目通过 `pages.json` 管理三类页面：

- 主包：主页、分类、购物车、我的、登录、商品详情、推荐页。
- `pagesMember` 分包：设置、个人资料、地址列表、地址表单。
- `pagesOrder` 分包：确认订单、订单详情、支付结果、订单列表。

Tab 页面使用原生 TabBar。普通页面主要使用 `<navigator>` 或 `uni.navigateTo`，提交订单和支付结果等不应返回到旧表单的流程使用 `uni.redirectTo`。

### 3.2 分包预下载

参考项目在进入“我的”页面后预下载会员分包：

```json
"preloadRule": {
  "pages/my/my": {
    "network": "all",
    "packages": ["pagesMember"]
  }
}
```

这种方式不会提前执行页面逻辑，只提前下载分包资源，适合“当前页之后有高概率进入某一业务分包”的路径。

当前项目已经有更多业务分包，但此前没有 `preloadRule`。本次按真实入口增加了克制的 Wi-Fi 预下载：

- 分类页预下载订单分包。
- 社区页预下载发帖和聊天分包。
- 我的页预下载订单、钱包、资产、分销和消息分包。

使用 Wi-Fi 而非 `all`，避免移动网络下主动消耗流量。

## 4. 参考项目的数据流

### 4.1 请求链路

```text
页面生命周期/用户动作
  -> services/domain.ts
  -> http<T>(UniApp.RequestOptions)
  -> uni.request
  -> 请求拦截器补 baseURL、timeout、source-client、Authorization
  -> 后端返回 { code, msg, result }
  -> 页面读取 result 并更新 ref/computed
```

`uni.addInterceptor` 同时作用于 `request` 和 `uploadFile`。登录信息放在 Pinia 的 `member` store，并通过 `pinia-plugin-persistedstate` 写入小程序本地存储。401 时清理会员状态并进入登录页。

这个设计的优点是请求头和鉴权逻辑集中；缺点是网络层直接弹 Toast 和跳页面，UI、副作用与数据访问耦合，并且 401 连续发生时没有显式的跳转去重。

### 4.2 典型业务流

#### 首页

`onLoad` 并行请求 Banner、分类和热门推荐，骨架屏由统一 `isLoading` 控制；猜你喜欢组件独立分页，页面触底时通过 composable 调用组件暴露的 `getMore()`。

#### 商品详情

路由参数 `id` -> 商品详情 API -> 转换为 SKU 弹窗需要的本地数据结构 -> 加购物车或携带 `skuId/count` 进入确认订单页。

#### 购物车

`onShow` 重新拉取购物车，保证从其他页面返回后数据更新；数量、选中状态、删除等动作先调用 API，再同步本地列表。

#### 下单与支付

确认订单页根据购物车、立即购买或再次购买三种入口请求预订单；提交成功后 `redirectTo` 订单详情，避免返回重复提交；支付结果继续使用替换式导航。

#### 订单列表

分页状态由 `page/isLoading/isFinish` 管理，触底时阻止重复请求和结束后的无效请求，下拉刷新会重置页码、列表和结束标记。

## 5. 当前项目的数据流

当前项目的主链路是：

```text
Vue 页面
  -> frontend/src/services/*.ts
  -> frontend/src/utils/request.ts
  -> /api/v1/*
  -> Spring Security: RateLimitFilter -> JwtAuthFilter
  -> Controller -> Service/Domain Service -> Mapper/外部适配器
  -> ApiResponse<T> { success, data, error, traceId }
  -> request.ts 解包 data、归一化上传 URL
  -> 页面 ref / Pinia store
```

鉴权流程：

1. 小程序调用 `uni.login` 获取微信 code。
2. `session` store 调用 `/auth/wechat` 换取 access token。
3. token 写入 `xq_access_token`，后续请求使用 `Bearer`。
4. Spring Security 的 JWT filter 恢复用户身份。
5. Controller 从 `SecurityContext` 读取用户 ID。
6. 401/403 时前端清理 token，并以去重方式重启到个人中心登录入口。

订单创建还通过 `Idempotency-Key` 请求头和请求体传递幂等键。金额统一使用整数分，接口契约以 `docs/api/openapi/openapi.yaml` 为真源。

## 6. 各类页面逻辑模式

| 页面类型 | 当前项目模式 | 建议约束 |
| --- | --- | --- |
| Tab 首页 | `onMounted/onLoad` 并行加载配置、Banner、商品 | 首屏请求并行，独立模块允许部分失败 |
| 分类/搜索 | 筛选条件 + 分页商品列表 | 页码只在请求成功后推进，旧响应不得覆盖新筛选 |
| 详情 | 路由 ID -> 详情 API -> 操作按钮 | 校验路由参数；提交操作防重复 |
| 表单/下单 | 本地表单 -> 预览 -> 创建 -> 支付 | 金额以后端预览为准；创建必须幂等 |
| 订单/资产/钱包 | `onShow` 刷新，分页追加 | 防并发、失败不跳页、空/错/加载三态 |
| 社区 | Feed + 点赞/收藏/关注局部更新 | 服务端成功后更新本地状态；失败给可观察反馈 |
| 我的 | 会话 store + 多业务入口 | 登录态集中管理；高频分包可预下载 |

## 7. 本次已落地优化

### 7.1 页面切换

在 `frontend/src/pages.json` 增加 `preloadRule`，按分类、社区、我的三个高频入口预下载后续分包，减少首次进入分包页面时的等待。

### 7.2 分页一致性

修正分类、消息和钱包三个列表：

- 请求成功后才更新页码或游标。
- 加载中不再重复发起“下一页”请求。
- 新筛选或新一轮刷新会使旧请求结果失效，避免旧响应覆盖新状态。
- 请求失败后仍保留原页码，用户重试不会跳过数据。

### 7.3 订单中心真实数据流

参考项目把订单列表视为交易链路的核心页面：列表按状态分页，商品信息与订单状态共同组成视图，触底和下拉刷新都具有明确门禁。

当前项目此前虽然调用了 `/orders`，但返回值没有进入页面，界面实际渲染的是固定 mock 数据，详情跳转和加载更多也被关闭。本次改为：

- `/orders` 负责订单分页和状态筛选。
- 根据每条订单的 `skuId` 调用目录服务补齐商品标题、图片和品牌型号。
- 页面组合 `OrderCard` 视图模型，不扩张后端订单契约。
- 支持触底分页、下拉刷新、请求竞态保护、错误重试和空状态。
- 待付款订单恢复取消和支付入口，其他订单恢复详情跳转。
- 后端没有物流或激活码数据时不再展示虚构内容。

### 7.4 首页刷新

首页增加原生下拉刷新，将活动、场景和推荐商品统一纳入 `loadHome()`。推荐商品切换和刷新使用请求序号阻止较慢的旧响应覆盖当前 Tab 数据。

## 8. 不建议直接照搬的内容

- 不引入参考项目的固定公网 base URL。
- 不把 Toast、登录跳转等所有 UI 行为塞进底层请求函数。
- 不复制参考项目的模拟支付逻辑。
- 不引入 `pinia-plugin-persistedstate`：当前项目只有少量会话字段，手动持久化已足够，新增依赖收益不足。
- 不机械复制 SKU 组件和商品模型：当前项目包含租赁、购买、软件三种订单模型，契约不同。

## 9. 后续优先级

1. 将仍直接调用 `uni.navigateTo` 的页面逐步收口到统一导航模块，并补登录后返回策略。
2. 把分页门禁抽成项目级 composable，待至少三个页面的 API 契约稳定后再统一迁移。
3. 清理订单中心当前保留的 mock 数据，让页面完全由 `/orders` 和商品快照字段驱动。
4. 为请求层补可测试的错误类型，保留 `error.code` 和 `traceId`，方便页面按业务错误处理。
5. 为登录、下单、支付、订单状态流补端到端冒烟测试。

## 10. 参考

- 参考项目：<https://gitee.com/Megasu/uniapp-shop-vue3-ts>
- uni-app `pages.json` / `preloadRule`：<https://uniapp.dcloud.net.cn/collocation/pages>
- uni-app 拦截器：<https://uniapp.dcloud.net.cn/api/interceptor.html>
