<template>
  <view class="page">
    <template v-if="loading">
      <Skeleton variant="admin-dashboard" />
    </template>
    <view v-else class="dashboard">
      <view class="stats-grid">
        <view class="stat-card">
          <text class="stat-value">{{ overview.totalOrders }}</text>
          <text class="stat-label">总工单</text>
        </view>
        <view class="stat-card warn">
          <text class="stat-value">{{ overview.pendingOrders }}</text>
          <text class="stat-label">待处理</text>
        </view>
        <view class="stat-card success">
          <text class="stat-value">{{ (overview.completionRate * 100).toFixed(0) }}%</text>
          <text class="stat-label">完成率</text>
        </view>
        <view class="stat-card danger">
          <text class="stat-value">{{ overview.overdueInspections }}</text>
          <text class="stat-label">逾期巡检</text>
        </view>
      </view>

      <view class="menu-list">
        <view class="menu-item" @tap="go('/pages/workorder/hall')">
          <text>工单管理</text>
          <text class="arrow">></text>
        </view>
        <view class="menu-item" @tap="go('/pages/inspection/task-list')">
          <text>巡检任务</text>
          <text class="arrow">></text>
        </view>
        <view class="menu-item" @tap="go('/pages/admin/notifications')">
          <text>消息提醒</text>
          <text class="arrow">></text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import Skeleton from "../../components/PageSkeleton.vue";
import { getDashboardOverview } from "../../services/repair";

const overview = ref({
  totalOrders: 0,
  pendingOrders: 0,
  completionRate: 0,
  overdueInspections: 0,
});
const loading = ref(true);

onShow(async () => {
  loading.value = true;
  try {
    overview.value = await getDashboardOverview();
  } catch {}
  finally {
    loading.value = false;
  }
});

function go(url: string) {
  uni.navigateTo({ url });
}
</script>

<style scoped lang="scss">
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  padding: 28rpx;
}

.stat-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  text-align: center;
}

.stat-card.warn {
  border-left: 6rpx solid #f59e0b;
}

.stat-card.success {
  border-left: 6rpx solid #10b981;
}

.stat-card.danger {
  border-left: 6rpx solid #ef4444;
}

.stat-value {
  display: block;
  font-size: 36rpx;
  font-weight: 800;
  color: #111827;
}

.stat-label {
  display: block;
  font-size: 22rpx;
  color: #6b7280;
  margin-top: 6rpx;
}

.menu-list {
  background: #fff;
  margin: 0 28rpx;
  border-radius: 20rpx;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 28rpx;
  border-bottom: 1rpx solid #f9fafb;
  font-size: 28rpx;
}

.arrow {
  color: #9ca3af;
}
</style>
