<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '64px' : '220px'" class="aside">
      <div class="logo">{{ collapsed ? 'XQ' : '星球出机' }}</div>
      <el-menu :default-active="route.path" :collapse="collapsed" router background-color="#001529" text-color="#ffffffb3" active-text-color="#fff">
        <el-menu-item index="/dashboard"><el-icon><Odometer /></el-icon><template #title>工作台</template></el-menu-item>
        <el-sub-menu index="/catalog">
          <template #title><el-icon><Goods /></el-icon><span>商品信息</span></template>
          <el-menu-item index="/catalog/sku">SKU 列表</el-menu-item>
          <el-menu-item index="/catalog/brands">品牌主数据</el-menu-item>
          <el-menu-item index="/catalog/filters">商品筛选菜单</el-menu-item>
          <el-menu-item index="/catalog/scenes">首页场景应用</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/operations">
          <template #title><el-icon><Flag /></el-icon><span>活动与运营</span></template>
          <el-menu-item index="/operations/activities">活动详情</el-menu-item>
          <el-menu-item index="/operations/banners">运营位</el-menu-item>
          <el-menu-item index="/operations/topics">场景专题</el-menu-item>
          <el-menu-item index="/operations/config">配置发布</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/moderation">
          <template #title><el-icon><View /></el-icon><span>内容审核</span></template>
          <el-menu-item index="/moderation/queue">审核队列</el-menu-item>
          <el-menu-item index="/moderation/logs">审核记录</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/community">
          <template #title><el-icon><ChatDotRound /></el-icon><span>社区管理</span></template>
          <el-menu-item index="/community/posts">帖子内容</el-menu-item>
          <el-menu-item index="/community/topics">话题管理</el-menu-item>
          <el-menu-item index="/community/analytics">运营数据</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/orders"><template #title><el-icon><Document /></el-icon><span>订单与履约</span></template><el-menu-item index="/orders/list">订单列表</el-menu-item></el-sub-menu>
        <el-sub-menu index="/users"><template #title><el-icon><User /></el-icon><span>用户管理</span></template><el-menu-item index="/users/list">用户列表</el-menu-item></el-sub-menu>
        <el-sub-menu index="/finance">
          <template #title><el-icon><Wallet /></el-icon><span>资金与分销</span></template>
          <el-menu-item index="/finance/wallet">钱包流水</el-menu-item><el-menu-item index="/finance/withdraw">提现审核</el-menu-item><el-menu-item index="/finance/commission">佣金与结算</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/repair">
          <template #title><el-icon><SetUp /></el-icon><span>运维管理</span></template>
          <el-menu-item index="/repair/devices">设备管理</el-menu-item><el-menu-item index="/repair/workorders">工单管理</el-menu-item><el-menu-item index="/repair/inspection">巡检管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/analytics">
          <template #title><el-icon><DataAnalysis /></el-icon><span>数据分析</span></template>
          <el-menu-item index="/analytics/overview">经营概览</el-menu-item><el-menu-item index="/analytics/product">商品分析</el-menu-item><el-menu-item index="/analytics/operations">运营分析</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/system">
          <template #title><el-icon><Setting /></el-icon><span>系统设置</span></template>
          <el-menu-item index="/system/admins">管理员</el-menu-item><el-menu-item index="/system/roles">角色权限</el-menu-item><el-menu-item index="/system/audit">审计日志</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container class="content-shell">
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse" @click="collapsed = !collapsed"><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
          <el-breadcrumb separator="/"><el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item><el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item></el-breadcrumb>
        </div>
        <div class="profile">
          <el-icon class="avatar"><User /></el-icon>
          <span>{{ auth.admin?.displayName || auth.admin?.username || '管理员' }}</span>
          <el-button text :icon="SwitchButton" @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Odometer, Goods, Flag, View, Document, User, Wallet, SetUp, DataAnalysis, Setting, Fold, Expand, SwitchButton, ChatDotRound } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const collapsed = ref(false)
function logout() { auth.logout(); router.push('/login') }
</script>

<style scoped>
.layout { height: 100vh; overflow: hidden; }
.aside { height: 100vh; display: flex; flex-direction: column; background: #001529; transition: width .2s; overflow: hidden; }
.logo { height: 56px; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 18px; font-weight: 600; border-bottom: 1px solid #ffffff1a; }
.aside :deep(.el-menu) { flex: 1; min-height: 0; overflow-y: auto; overflow-x: hidden; border-right: none; }
.content-shell { height: 100vh; min-width: 0; overflow: hidden; }
.header { display: flex; align-items: center; justify-content: space-between; padding: 0 20px; border-bottom: 1px solid #f0f0f0; background: #fff; }
.header-left, .profile { display: flex; align-items: center; gap: 14px; }
.collapse { cursor: pointer; font-size: 20px; }
.avatar { width: 28px; height: 28px; border-radius: 50%; justify-content: center; background: #ecf5ff; color: #409eff; }
.main { height: calc(100vh - 60px); min-height: 0; overflow-y: auto; background: #f5f7fa; }
</style>
