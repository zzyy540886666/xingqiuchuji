# 小程序页面切换进程设计

## 目标

页面切换统一走 `frontend/src/utils/navigation.ts`，避免每个页面各自判断 `navigateTo`、`switchTab`、`redirectTo`、`reLaunch`。这套设计参考 `D:\Pro\screenshot-to-code` 中“显式状态 + store 记录流程”的思路，但保持为 UniApp 小程序里的轻量实现。

## 核心文件

- `frontend/src/stores/navigation.ts`：记录当前导航进程、最近历史、状态和失败信息。
- `frontend/src/utils/navigation.ts`：页面意图入口，负责 Tab 页判断、防重复点击、跳转方法选择和回调落点。

## 使用规则

1. 普通页面跳转使用 `navigateToPage(url, { reason })`。
2. 替换当前页使用 `redirectToPage(url, reason)`。
3. 清空栈进入页面使用 `reLaunchToPage(url, reason)`。
4. 返回按钮使用 `navigateBackOrHome(reason)`，没有上一页时回首页。
5. 不直接在业务点击事件里写 `uni.switchTab` 或 `uni.reLaunch`，除非是微信 API 的特殊限制且必须绕开统一入口。

## 页面切换状态

每次跳转都会产生一个 `NavigationProcess`：

- `pending`：已发起，等待小程序回调。
- `settled`：跳转完成。
- `failed`：跳转失败，保存 `errMsg`。
- `blocked`：已有跳转未结束时又触发了新跳转，防止重复点击造成栈异常。

## 已接入入口

- 底部导航 `BottomNav.vue`
- 启动页进入主流程 `pages/loading/index.vue`
- 分类页搜索和返回 `pages/category/index.vue`
- 个人中心功能入口 `pages/profile/index.vue`
- 登录过期重定向 `utils/request.ts`

## 后续接入建议

新增页面或改旧页面时，优先替换业务按钮里的直接 `uni.navigateTo`。`uni.navigateBack` 可以逐步迁移到 `navigateBackOrHome`，但不需要为了形式一次性改完整个仓库。
