<template>
  <view class="page withdraw-page">
    <view class="panel">
      <text class="title">提现申请</text>
      <view class="input-row"><text>提现金额</text><input type="digit" placeholder="请输入金额" /></view>
      <view class="input-row"><text>手续费</text><text>以后端试算为准</text></view>
      <view class="notice">提交前会进行二次确认，防重复点击由按钮 loading 配合后端幂等保证。</view>
      <button>提交申请</button>
    </view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.withdraw-page { min-height: 100vh; padding: 32rpx; }
.panel { padding: 32rpx; border-radius: $radius-md; background: #fff; box-shadow: $shadow-card; }
.title { color: $color-text; font-size: 34rpx; font-weight: 900; }
.input-row { display: flex; justify-content: space-between; align-items: center; padding: 30rpx 0; border-bottom: 1rpx solid $color-border; color: $color-text; font-size: 28rpx; }
.input-row input { text-align: right; color: $color-text; }
.input-row text:last-child { color: $color-subtext; }
.notice { margin-top: 28rpx; color: $color-subtext; font-size: 24rpx; line-height: 36rpx; }
button { height: 88rpx; margin-top: 36rpx; border-radius: 999rpx; background: $color-primary; color: #fff; font-size: 30rpx; font-weight: 800; }
</style>
