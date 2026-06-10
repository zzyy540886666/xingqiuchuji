# 最新异步任务保护

## 目标

参考 `D:\Pro\screenshot-to-code` 中用 event id / variant index 防止旧流污染当前状态的做法，小程序页面里的异步请求也必须证明自己仍是最新任务，才能写入页面状态。

这个约束主要解决三类问题：

- 快速切换筛选条件后，旧请求晚返回并覆盖新结果。
- 下拉刷新和分页请求交错时，旧分页结果追加到新列表。
- 页面重新进入或重新加载后，上一轮请求仍在 finally 中关闭当前 loading。

## 核心文件

- `frontend/src/utils/latestTask.ts`

## 使用方式

```ts
const productTask = createLatestTask();

async function loadProducts() {
  const requestId = productTask.begin();
  const result = await getSkuList(params);
  if (!productTask.isCurrent(requestId)) return;
  products.value = result.items;
}
```

需要主动废弃上一轮请求时使用 `cancel()`：

```ts
function reload() {
  task.cancel();
  fetchData();
}
```

## 已接入

- `pages/home/index.vue` 推荐商品切换。
- `pages/category/index.vue` 分类商品列表、筛选、分页请求。
- `pages/notifications/index.vue` 消息分页请求。
- `pages/order/list.vue` 订单筛选、刷新、分页及商品信息补全。
- `pages/wallet/index.vue` 钱包流水分页和重新加载。

## 约定

- 页面内不要再手写 `let xxxRequestId = 0`、`++xxxRequestId` 这类局部竞态保护。
- 新增列表、搜索、筛选、分页、详情补全等异步状态写入时，优先使用 `createLatestTask()`。
- 如果异步结果会写入共享 store，应在 store action 或调用方明确放置最新任务保护，不要让旧结果静默覆盖当前页面状态。
