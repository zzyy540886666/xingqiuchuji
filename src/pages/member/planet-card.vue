<template>
  <view class="page card-page">
    <view class="hero-card">
      <text class="title">星球卡</text>
      <text class="desc">开通后刷新会员等级，权益以服务端会员快照为准。</text>
    </view>
    <view class="sku-card">
      <view><text>年度星球卡</text><text>免押权益、配件折扣、新物种体验提醒</text></view>
      <text>¥399</text>
    </view>
    <view class="sku-card">
      <view><text>季度体验卡</text><text>适合活动高频租赁用户</text></view>
      <text>¥129</text>
    </view>
    <button>微信支付开通</button>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.card-page { min-height: 100vh; padding: 32rpx; }
.hero-card { padding: 42rpx; border-radius: 36rpx; background: linear-gradient(135deg, #111a35, #2f7cf6); color: #fff; }
.title, .desc { display: block; }
.title { font-size: 42rpx; font-weight: 900; }
.desc { margin-top: 14rpx; font-size: 26rpx; line-height: 38rpx; }
.sku-card { display: flex; justify-content: space-between; margin-top: 24rpx; padding: 30rpx; border-radius: $radius-md; background: #fff; box-shadow: $shadow-card; }
.sku-card view text { display: block; }
.sku-card view text:first-child { color: $color-text; font-size: 30rpx; font-weight: 900; }
.sku-card view text:last-child { margin-top: 10rpx; color: $color-subtext; font-size: 24rpx; }
.sku-card > text { color: $color-primary; font-size: 34rpx; font-weight: 900; }
button { height: 88rpx; margin-top: 42rpx; border-radius: 999rpx; background: $color-primary; color: #fff; font-size: 30rpx; font-weight: 800; }
</style>
