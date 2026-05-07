<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getOrderDetail } from '@/services/orders'
import { formatMoney } from '@/utils/format'

const order = ref<{ id: string; title: string; status: string; amountCent: number; allowedActions: string[] } | null>(null)

onLoad(async query => {
  const id = typeof query?.id === 'string' ? query.id : 'order-001'
  order.value = await getOrderDetail(id)
})
</script>

<template>
  <view v-if="order" class="detail-page">
    <view class="status-card">
      <text>{{ order.status }}</text>
      <text>操作按钮以后端 allowedActions 渲染</text>
    </view>
    <view class="panel">
      <text class="title">{{ order.title }}</text>
      <view class="row"><text>订单编号</text><text>{{ order.id }}</text></view>
      <view class="row"><text>订单金额</text><text>{{ formatMoney(order.amountCent) }}</text></view>
      <view class="actions">
        <button v-for="action in order.allowedActions" :key="action">{{ action }}</button>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.detail-page {
  min-height: 100vh;
  padding: 32rpx;
}

.status-card {
  padding: 42rpx;
  border-radius: 32rpx;
  background: linear-gradient(135deg, #2f7cf6, #83b7ff);
  color: #fff;
}

.status-card text {
  display: block;
}

.status-card text:first-child {
  font-size: 42rpx;
  font-weight: 900;
}

.status-card text:last-child {
  margin-top: 14rpx;
  font-size: 24rpx;
}

.panel {
  margin-top: 28rpx;
  padding: 32rpx;
  border-radius: $radius-md;
  background: #fff;
  box-shadow: $shadow-card;
}

.title {
  color: $color-text;
  font-size: 34rpx;
  font-weight: 900;
}

.row {
  display: flex;
  justify-content: space-between;
  padding: 28rpx 0;
  border-bottom: 1rpx solid $color-border;
  color: $color-subtext;
  font-size: 26rpx;
}

.row text:last-child {
  color: $color-text;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 18rpx;
  margin-top: 28rpx;
}

.actions button {
  height: 64rpx;
  padding: 0 28rpx;
  border-radius: 999rpx;
  background: $color-primary-soft;
  color: $color-primary;
  font-size: 24rpx;
}
</style>
