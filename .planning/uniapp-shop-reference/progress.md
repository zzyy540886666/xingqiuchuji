# Progress

## 2026-06-08
- 读取仓库级说明、当前前端入口、路由、请求层和 Pinia stores。
- 确认根目录计划文件属于上一项骨架屏任务，因此为本任务创建独立规划目录。
- 发现 `rg.exe` 无法执行，后续改用 PowerShell 搜索。
- 确认工作区有大量用户未提交改动，后续仅做聚焦修改。
- 成功拉取参考仓库并分析目录、路由、服务层、Pinia、请求拦截、典型页面和最近 100 个提交热点。
- 核对 uni-app 官方文档，确认 `preloadRule` 的 key、packages 和 network 规则。
- 输出 `docs/frontend/uniapp-shop-vue3-ts-study.md`。
- 在 `pages.json` 增加 Wi-Fi 分包预下载。
- 修正分类、消息、钱包分页推进和并发响应问题。
- 定点核验时发现消息首次失败后的重试会被分页结束门禁阻止，已调整为仅在已有数据且无更多页时拦截。
- `npm run type-check` 通过。
- `frontend/npm run build:mp-weixin` 通过，生成的 `app.json` 含预期 `preloadRule`。
- `git diff --check` 通过。
- `npm run test:unit` 共 5 项，通过 3 项；2 项失败来自现有过时断言（主包页数和旧品牌文案），与本次改动无关。
- 第二轮深入分析确认订单中心已被静态 mock 替代，真实接口响应没有渲染。
- 恢复订单分页、状态筛选、SKU 信息补全、详情/支付/取消入口、空态、错误态和下拉刷新。
- 首页增加原生下拉刷新和推荐商品请求竞态保护。
- 更新测试：不再固定主包页数或依赖品牌整段字符串，新增订单真实数据流和首页刷新行为断言。
- 首轮第二阶段验证中新增行为测试均通过，仅旧“微信一键登录”文案断言失败；已改为验证当前登录入口和会话调用。
- 移除订单页无行为的装饰性筛选按钮，并补齐待履约状态色。
- 第二轮 `npm run type-check` 通过。
- 第二轮 `npm run test:unit` 通过，7/7。
- 第二轮微信小程序生产构建通过。
- `npm run harness:ci` 通过，状态 `SUCCESS`，7/7。
- 构建产物确认首页 `enablePullDownRefresh: true`，订单页无 `mockOrders` 残留。
