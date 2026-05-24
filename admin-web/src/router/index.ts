import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'Login', component: () => import('@/layouts/LoginLayout.vue') },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/modules/dashboard/index.vue'), meta: { title: '工作台' } },
      {
        path: 'catalog', name: 'Catalog', redirect: '/catalog/sku',
        children: [
          { path: 'sku', name: 'SkuList', component: () => import('@/modules/catalog/SkuList.vue'), meta: { title: 'SKU 列表' } },
          { path: 'sku/:id', name: 'SkuDetail', component: () => import('@/modules/catalog/SkuDetail.vue'), meta: { title: '商品详情' } },
          { path: 'brands', name: 'BrandList', component: () => import('@/modules/catalog/BrandList.vue'), meta: { title: '品牌主数据' } },
          { path: 'scenes', name: 'SceneList', component: () => import('@/modules/catalog/SceneList.vue'), meta: { title: '场景组合' } },
          { path: 'filters', name: 'CatalogFilters', component: () => import('@/modules/catalog/FilterList.vue'), meta: { title: '商品筛选菜单' } },
        ],
      },
      {
        path: 'operations', name: 'Operations', redirect: '/operations/activities',
        children: [
          { path: 'activities', name: 'ActivityList', component: () => import('@/modules/operations/ActivityList.vue'), meta: { title: '活动详情' } },
          { path: 'topics', name: 'TopicList', component: () => import('@/modules/operations/TopicList.vue'), meta: { title: '场景专题' } },
          { path: 'banners', name: 'BannerList', component: () => import('@/modules/operations/BannerList.vue'), meta: { title: '运营位' } },
          { path: 'config', name: 'ConfigPublish', component: () => import('@/modules/operations/ConfigPublish.vue'), meta: { title: '配置发布' } },
        ],
      },
      {
        path: 'moderation', name: 'Moderation', redirect: '/moderation/queue',
        children: [
          { path: 'queue', name: 'ModerationQueue', component: () => import('@/modules/moderation/Queue.vue'), meta: { title: '审核队列' } },
          { path: 'logs', name: 'ModerationLogs', component: () => import('@/modules/moderation/Logs.vue'), meta: { title: '审核记录' } },
        ],
      },
      {
        path: 'community', name: 'Community', redirect: '/community/posts',
        children: [
          { path: 'posts', name: 'CommunityPosts', component: () => import('@/modules/community/PostList.vue'), meta: { title: '帖子内容' } },
          { path: 'topics', name: 'CommunityTopics', component: () => import('@/modules/community/TopicList.vue'), meta: { title: '话题管理' } },
          { path: 'analytics', name: 'CommunityAnalytics', component: () => import('@/modules/community/Analytics.vue'), meta: { title: '运营数据' } },
        ],
      },
      {
        path: 'orders', name: 'Orders', redirect: '/orders/list',
        children: [
          { path: 'list', name: 'OrderList', component: () => import('@/modules/orders/OrderList.vue'), meta: { title: '订单列表' } },
          { path: ':id', name: 'OrderDetail', component: () => import('@/modules/orders/OrderDetail.vue'), meta: { title: '订单详情' } },
        ],
      },
      {
        path: 'users', name: 'Users', redirect: '/users/list',
        children: [
          { path: 'list', name: 'UserList', component: () => import('@/modules/users/UserList.vue'), meta: { title: '用户列表' } },
          { path: ':id', name: 'UserDetail', component: () => import('@/modules/users/UserDetail.vue'), meta: { title: '用户详情' } },
        ],
      },
      {
        path: 'finance', name: 'Finance', redirect: '/finance/wallet',
        children: [
          { path: 'wallet', name: 'WalletFlow', component: () => import('@/modules/finance/WalletFlow.vue'), meta: { title: '钱包流水' } },
          { path: 'withdraw', name: 'WithdrawAudit', component: () => import('@/modules/finance/WithdrawAudit.vue'), meta: { title: '提现审核' } },
          { path: 'commission', name: 'Commission', component: () => import('@/modules/finance/Commission.vue'), meta: { title: '佣金与结算' } },
        ],
      },
      {
        path: 'repair', name: 'Repair', redirect: '/repair/devices',
        children: [
          { path: 'devices', name: 'DeviceList', component: () => import('@/modules/repair/DeviceList.vue'), meta: { title: '设备管理' } },
          { path: 'workorders', name: 'WorkOrderList', component: () => import('@/modules/repair/WorkOrderList.vue'), meta: { title: '工单管理' } },
          { path: 'inspection', name: 'InspectionList', component: () => import('@/modules/repair/InspectionList.vue'), meta: { title: '巡检管理' } },
        ],
      },
      {
        path: 'analytics', name: 'Analytics', redirect: '/analytics/overview',
        children: [
          { path: 'overview', name: 'AnalyticsOverview', component: () => import('@/modules/analytics/Overview.vue'), meta: { title: '经营概览' } },
          { path: 'product', name: 'ProductAnalytics', component: () => import('@/modules/analytics/Product.vue'), meta: { title: '商品分析' } },
          { path: 'operations', name: 'OpsAnalytics', component: () => import('@/modules/analytics/Operations.vue'), meta: { title: '运营分析' } },
        ],
      },
      {
        path: 'system', name: 'System', redirect: '/system/admins',
        children: [
          { path: 'admins', name: 'AdminList', component: () => import('@/modules/system/AdminList.vue'), meta: { title: '管理员' } },
          { path: 'roles', name: 'RoleList', component: () => import('@/modules/system/RoleList.vue'), meta: { title: '角色权限' } },
          { path: 'audit', name: 'AuditLog', component: () => import('@/modules/system/AuditLog.vue'), meta: { title: '审计日志' } },
        ],
      },
    ],
  },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.path === '/login') return auth.isLoggedIn ? '/' : true
  if (!auth.isLoggedIn) return '/login'
  if (!auth.admin) await auth.fetchCurrentAdmin()
  return true
})

export default router
