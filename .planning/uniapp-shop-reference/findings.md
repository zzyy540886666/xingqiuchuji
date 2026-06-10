# Findings

## Current Project
- 前端位于 `frontend/`，技术栈为 UniApp Vue 3、TypeScript、Pinia。
- `pages.json` 使用主包加多个分包，四个 Tab 页面为首页、分类、社区、我的。
- `src/utils/request.ts` 统一处理 API 前缀、Bearer token、错误包裹、上传 URL 归一化和 401/403 跳转。
- `src/utils/navigation.ts` 已区分 Tab 页面与普通页面，并有简单的重复导航锁。
- `src/stores/session.ts` 负责微信登录、用户信息和邀请码绑定。
- `src/stores/config.ts` 负责全局运营配置，但失败时没有统一错误状态或重试策略。
- 当前工作区有大量未提交变更，本任务必须采用最小改动。

## Reference Project
- 已拉取到 `D:\Pro\_reference_repos\uniapp-shop-vue3-ts`。
- 主包为 `src/pages`，会员和订单使用独立分包，并在“我的”页配置会员分包预下载。
- `services` 按业务域组织，`utils/http.ts` 使用 `uni.addInterceptor` 统一 base URL、超时、来源头和 token。
- Pinia 的会员状态通过持久化插件写入小程序本地存储。
- 首页使用 `Promise.all` 并行加载；订单列表使用 `page/isLoading/isFinish` 控制分页；猜你喜欢通过 composable 调用组件暴露方法。
- 参考仓库不含业务 REST API 后端源码，服务端实现不可从该仓库确认。
- 最近 100 个提交的热点集中在订单列表、订单详情、登录、商品详情和购物车。

## Candidate Optimization Areas
- 已落地 `preloadRule`，按分类、社区、我的入口预下载高概率访问分包。
- 已修正分类、消息、钱包分页在失败时跳页和重复加载的问题。
- 后续可考虑统一导航、抽取分页 composable、移除订单页 mock、保留结构化 API 错误。

## Second Pass
- 当前订单中心请求了 `/orders`，但刻意不使用响应，实际展示固定 mock；详情跳转和 `loadMore` 为空实现。
- 后端 `OrderResponse` 只提供订单与 `skuId`，商品标题和图片应由前端通过目录服务补齐，不能假设后端存在物流、激活码等字段。
- 参考项目订单列表的核心可迁移模式是：分页门禁、状态筛选、下拉刷新、按需组合商品信息、操作成功后局部刷新。
- 首页已有并行加载，但缺少用户主动刷新和推荐 Tab 请求竞态保护。
