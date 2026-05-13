<template>
  <view class="page">
    <view class="title">报修进度</view>
    <view class="desc">工单号：{{ orderId || '-' }}</view>
    <view class="timeline">
      <view v-for="(step, idx) in steps" :key="idx" class="step">
        <view class="dot" />
        <view class="content">
          <view class="name">{{ step.name }}</view>
          <view class="time">{{ step.time }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const orderId = ref('')
const steps = [
  { name: '工单已提交', time: '刚刚' },
  { name: '等待派单', time: '-' },
  { name: '维修处理中', time: '-' },
  { name: '待验收', time: '-' },
  { name: '已完成', time: '-' }
]

onLoad((query) => {
  orderId.value = String(query?.id ?? '')
})
</script>

<style scoped>
.page { padding: 24rpx; }
.title { font-size: 32rpx; font-weight: 600; }
.desc { margin: 12rpx 0 24rpx; color: #7a8699; font-size: 24rpx; }
.timeline { background: #fff; border-radius: 14rpx; padding: 8rpx 20rpx; }
.step { display: flex; gap: 16rpx; padding: 18rpx 0; border-bottom: 1rpx solid #f0f2f5; }
.step:last-child { border-bottom: 0; }
.dot { width: 14rpx; height: 14rpx; border-radius: 50%; margin-top: 10rpx; background: #2f6bff; }
.name { font-size: 28rpx; }
.time { margin-top: 6rpx; color: #7a8699; font-size: 24rpx; }
</style>
