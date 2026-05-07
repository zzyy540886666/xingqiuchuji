<template>
  <view class="page commission-page">
    <view class="amount-card"><text>可提现佣金</text><text>¥8,600</text><button>发起提现</button></view>
    <view class="bill"><text>待结算</text><text>¥2,300 · 维权期后 T+7</text></view>
    <view class="bill"><text>可提现</text><text>¥8,600</text></view>
    <view class="bill"><text>已提现</text><text>¥12,400</text></view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.commission-page { min-height: 100vh; padding: 32rpx; }
.amount-card { padding: 42rpx; border-radius: 36rpx; background: linear-gradient(135deg, #111a35, #2f7cf6); color: #fff; }
.amount-card text { display: block; }
.amount-card text:first-child { font-size: 26rpx; }
.amount-card text:nth-child(2) { margin-top: 16rpx; font-size: 54rpx; font-weight: 900; }
.amount-card button { width: 180rpx; height: 68rpx; margin-top: 32rpx; border-radius: 999rpx; background: #fff; color: $color-primary; font-size: 26rpx; font-weight: 800; }
.bill { display: flex; justify-content: space-between; margin-top: 22rpx; padding: 28rpx; border-radius: $radius-md; background: #fff; box-shadow: $shadow-card; color: $color-text; font-size: 28rpx; }
.bill text:last-child { color: $color-subtext; font-size: 24rpx; }
</style>
