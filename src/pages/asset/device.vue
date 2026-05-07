<script setup lang="ts">
function goTrusteeship() {
  uni.navigateTo({ url: '/pages/asset/trusteeship?id=device-001' })
}
</script>

<template>
  <view class="page device-page">
    <view class="hero"><view class="robot" /></view>
    <view class="panel">
      <text class="title">Alpha 表演机器人</text>
      <view class="row"><text>设备状态</text><text>空闲</text></view>
      <view class="row"><text>累计收益</text><text>¥8,600</text></view>
      <view class="row"><text>租用记录</text><text>12 次</text></view>
      <button @tap="goTrusteeship">托管上架</button>
    </view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.device-page { min-height: 100vh; padding-bottom: 48rpx; }
.hero { height: 360rpx; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #e8f1ff, #8ec0ff); }
.robot { width: 150rpx; height: 210rpx; border-radius: 46rpx; background: #fff; box-shadow: inset 0 -28rpx 48rpx rgba(47,124,246,.22); }
.panel { margin: 32rpx; padding: 32rpx; border-radius: $radius-md; background: #fff; box-shadow: $shadow-card; }
.title { color: $color-text; font-size: 36rpx; font-weight: 900; }
.row { display: flex; justify-content: space-between; padding: 28rpx 0; border-bottom: 1rpx solid $color-border; color: $color-text; font-size: 28rpx; }
.row text:last-child { color: $color-subtext; }
button { height: 88rpx; margin-top: 34rpx; border-radius: 999rpx; background: $color-primary; color: #fff; font-size: 30rpx; font-weight: 800; }
</style>
