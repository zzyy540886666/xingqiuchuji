<template>
  <view class="page">
    <view class="head">
      <view class="title">维修工单</view>
      <button class="ghost" size="mini" @click="loadData">刷新</button>
    </view>
    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="error" class="state error">{{ error }}</view>
    <view v-else-if="orders.length === 0" class="state">暂无待处理工单</view>
    <view v-else class="list">
      <view v-for="item in orders" :key="item.id" class="card">
        <view class="line">
          <text class="name">{{ item.title }}</text>
          <text class="status">{{ item.status }}</text>
        </view>
        <view class="meta">{{ item.faultType }} · {{ item.location }}</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listTechnicianWorkOrders, type WorkOrderItem } from '@/services/repair'

const loading = ref(false)
const error = ref('')
const orders = ref<WorkOrderItem[]>([])

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    orders.value = await listTechnicianWorkOrders()
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
.head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.title { font-size: 32rpx; font-weight: 600; }
.ghost { color: #2f6bff; border-color: #2f6bff; background: #fff; }
.state { text-align: center; color: #7a8699; padding: 60rpx 0; }
.state.error { color: #d93025; }
.list { display: flex; flex-direction: column; gap: 16rpx; }
.card { background: #fff; border-radius: 14rpx; padding: 20rpx; }
.line { display: flex; justify-content: space-between; }
.name { font-size: 28rpx; font-weight: 600; }
.status { font-size: 24rpx; color: #2f6bff; }
.meta { margin-top: 8rpx; font-size: 24rpx; color: #7a8699; }
</style>
