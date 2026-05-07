<template>
  <view class="page invite-page">
    <view class="poster">
      <text>星球出机城市合伙人</text>
      <text>邀请好友租/买机器人，获得佣金奖励</text>
      <view class="qr">inviteCode</view>
    </view>
    <button>复制邀请路径</button>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.invite-page { min-height: 100vh; padding: 32rpx; }
.poster { min-height: 780rpx; padding: 48rpx; border-radius: 40rpx; background: linear-gradient(180deg, #2f7cf6, #111a35); color: #fff; text-align: center; }
.poster text { display: block; }
.poster text:first-child { margin-top: 60rpx; font-size: 44rpx; font-weight: 900; }
.poster text:nth-child(2) { margin-top: 22rpx; font-size: 26rpx; line-height: 38rpx; }
.qr { width: 220rpx; height: 220rpx; margin: 360rpx auto 0; display: flex; align-items: center; justify-content: center; border-radius: 24rpx; background: #fff; color: $color-primary; font-size: 24rpx; }
button { height: 88rpx; margin-top: 32rpx; border-radius: 999rpx; background: $color-primary; color: #fff; font-size: 30rpx; font-weight: 800; }
</style>
