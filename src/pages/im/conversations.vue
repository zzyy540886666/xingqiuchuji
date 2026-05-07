<script setup lang="ts">
function goChat(conversationId: string) {
  uni.navigateTo({ url: `/pages/im/chat?id=${conversationId}` })
}
</script>

<template>
  <view class="page im-page">
    <view class="token-card"><text>IM 初始化</text><text>进入时向后端 GET /im/token 换取厂商 Token</text></view>
    <view class="conversation" @tap="goChat('c1')">
      <view class="avatar" />
      <view><text>星球客服</text><text>订单、售后、托管问题都可以咨询</text></view>
      <text>12:30</text>
    </view>
    <view class="conversation" @tap="goChat('c2')">
      <view class="avatar" />
      <view><text>圈子群聊</text><text>机器人活动共创讨论</text></view>
      <text>昨日</text>
    </view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.im-page { min-height: 100vh; padding: 32rpx; }
.token-card { padding: 30rpx; border-radius: $radius-md; background: $color-primary-soft; color: $color-primary; }
.token-card text { display: block; }
.token-card text:first-child { font-size: 30rpx; font-weight: 900; }
.token-card text:last-child { margin-top: 10rpx; font-size: 24rpx; }
.conversation { display: grid; grid-template-columns: 88rpx 1fr auto; gap: 20rpx; align-items: center; margin-top: 24rpx; padding: 26rpx; border-radius: $radius-md; background: #fff; box-shadow: $shadow-card; }
.avatar { width: 88rpx; height: 88rpx; border-radius: 50%; background: linear-gradient(135deg, #e8f1ff, #2f7cf6); }
.conversation view:nth-child(2) text { display: block; }
.conversation view:nth-child(2) text:first-child { color: $color-text; font-size: 30rpx; font-weight: 900; }
.conversation view:nth-child(2) text:last-child, .conversation > text { margin-top: 8rpx; color: $color-subtext; font-size: 24rpx; }
</style>
