<template>
  <view class="page">
    <view class="title">管理员控制台</view>
    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="error" class="state error">{{ error }}</view>
    <view v-else class="grid">
      <view class="card">
        <view class="label">待处理工单</view>
        <view class="value">{{ data.pendingWorkOrders }}</view>
      </view>
      <view class="card">
        <view class="label">逾期巡检</view>
        <view class="value">{{ data.overdueInspections }}</view>
      </view>
      <view class="card">
        <view class="label">完成率</view>
        <view class="value">{{ data.completionRate }}%</view>
      </view>
      <view class="card alerts">
        <view class="label">预警提醒</view>
        <view v-if="data.alerts.length === 0" class="hint">暂无预警</view>
        <view v-for="(alert, idx) in data.alerts" :key="idx" class="hint">- {{ alert }}</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { getAdminOverview } from '@/services/repair'

const loading = ref(false)
const error = ref('')
const data = reactive({
  pendingWorkOrders: 0,
  overdueInspections: 0,
  completionRate: 0,
  alerts: [] as string[]
})

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    const result = await getAdminOverview()
    data.pendingWorkOrders = result.pendingWorkOrders
    data.overdueInspections = result.overdueInspections
    data.completionRate = result.completionRate
    data.alerts = result.alerts
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.page { padding: 24rpx; }
.title { font-size: 32rpx; font-weight: 600; margin-bottom: 16rpx; }
.state { text-align: center; color: #7a8699; padding: 60rpx 0; }
.state.error { color: #d93025; }
.grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16rpx; }
.card { background: #fff; border-radius: 14rpx; padding: 20rpx; min-height: 120rpx; }
.label { color: #7a8699; font-size: 24rpx; }
.value { margin-top: 10rpx; font-size: 40rpx; font-weight: 700; color: #2f6bff; }
.alerts { grid-column: 1 / 3; }
.hint { margin-top: 8rpx; font-size: 24rpx; color: #222; }
</style>
