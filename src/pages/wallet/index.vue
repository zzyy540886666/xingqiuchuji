<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getWallet } from '@/services/member'
import { formatMoney } from '@/utils/format'

const wallet = ref({ balanceCent: 0, records: [] as Array<{ id: string; title: string; amountCent: number; type: string }> })

onLoad(async () => {
  wallet.value = await getWallet()
})
</script>

<template>
  <view class="wallet-page">
    <view class="balance-card">
      <text>光年币余额</text>
      <text>{{ formatMoney(wallet.balanceCent) }}</text>
      <button>申请提现</button>
    </view>
    <view class="record-panel">
      <text class="title">收支明细</text>
      <view v-for="record in wallet.records" :key="record.id" class="record-row">
        <view>
          <text>{{ record.title }}</text>
          <text>{{ record.type }}</text>
        </view>
        <text :class="{ income: record.amountCent > 0 }">{{ record.amountCent > 0 ? '+' : '' }}{{ formatMoney(record.amountCent) }}</text>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.wallet-page {
  min-height: 100vh;
  padding: 32rpx;
}

.balance-card {
  padding: 42rpx;
  border-radius: 36rpx;
  background: linear-gradient(135deg, #2f7cf6, #8ec0ff);
  color: #fff;
}

.balance-card text {
  display: block;
}

.balance-card text:first-child {
  font-size: 26rpx;
}

.balance-card text:nth-child(2) {
  margin-top: 18rpx;
  font-size: 54rpx;
  font-weight: 900;
}

.balance-card button {
  width: 188rpx;
  height: 68rpx;
  margin-top: 34rpx;
  border-radius: 999rpx;
  background: #fff;
  color: $color-primary;
  font-size: 26rpx;
  font-weight: 800;
}

.record-panel {
  margin-top: 28rpx;
  padding: 30rpx;
  border-radius: $radius-md;
  background: #fff;
  box-shadow: $shadow-card;
}

.title {
  color: $color-text;
  font-size: 32rpx;
  font-weight: 900;
}

.record-row {
  display: flex;
  justify-content: space-between;
  padding: 28rpx 0;
  border-bottom: 1rpx solid $color-border;
  color: $color-text;
}

.record-row view text {
  display: block;
}

.record-row view text:last-child {
  margin-top: 8rpx;
  color: $color-muted;
  font-size: 22rpx;
}

.income {
  color: $color-success;
}
</style>
