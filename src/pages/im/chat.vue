<template>
  <view class="page chat-page">
    <view class="message service">您好，欢迎来到星球出机，请问需要租赁、购买还是托管咨询？</view>
    <view class="message me">我想了解年会机器人租赁方案。</view>
    <view class="input-bar"><input placeholder="输入消息" /><button>发送</button></view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.chat-page { min-height: 100vh; padding: 32rpx 32rpx 140rpx; }
.message { max-width: 560rpx; margin-bottom: 24rpx; padding: 22rpx 26rpx; border-radius: 24rpx; font-size: 28rpx; line-height: 40rpx; }
.service { background: #fff; color: $color-text; box-shadow: $shadow-card; }
.me { margin-left: auto; background: $color-primary; color: #fff; }
.input-bar { position: fixed; left: 0; right: 0; bottom: 0; display: grid; grid-template-columns: 1fr 132rpx; gap: 16rpx; padding: 20rpx 32rpx calc(20rpx + env(safe-area-inset-bottom)); background: #fff; }
.input-bar input { height: 76rpx; padding: 0 24rpx; border-radius: 999rpx; background: #f3f6fa; font-size: 28rpx; }
.input-bar button { height: 76rpx; border-radius: 999rpx; background: $color-primary; color: #fff; font-size: 28rpx; }
</style>
