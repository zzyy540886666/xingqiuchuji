<script setup lang="ts">
function goDevice(deviceId: string) {
  uni.navigateTo({ url: `/pages/asset/device?id=${deviceId}` })
}
</script>

<template>
  <view class="page asset-page">
    <view class="summary-card"><text>我的分身资产</text><text>2 台机器人 · 累计收益 ¥12,800</text></view>
    <view class="device-card" @tap="goDevice('device-001')">
      <view class="device-card__cover" />
      <view><text>Alpha 表演机器人</text><text>空闲 · 可托管上架</text></view>
    </view>
    <view class="device-card" @tap="goDevice('device-002')">
      <view class="device-card__cover" />
      <view><text>导购机器人 Pro</text><text>维护中 · 暂不可上架</text></view>
    </view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.asset-page { min-height: 100vh; padding: 32rpx; }
.summary-card { padding: 42rpx; border-radius: 36rpx; background: linear-gradient(135deg, #2f7cf6, #8ec0ff); color: #fff; }
.summary-card text { display: block; }
.summary-card text:first-child { font-size: 40rpx; font-weight: 900; }
.summary-card text:last-child { margin-top: 14rpx; font-size: 26rpx; }
.device-card { display: flex; gap: 24rpx; margin-top: 24rpx; padding: 26rpx; border-radius: $radius-md; background: #fff; box-shadow: $shadow-card; }
.device-card__cover { width: 120rpx; height: 120rpx; border-radius: 24rpx; background: linear-gradient(135deg, #e8f1ff, #2f7cf6); }
.device-card view:last-child text { display: block; }
.device-card view:last-child text:first-child { color: $color-text; font-size: 30rpx; font-weight: 900; }
.device-card view:last-child text:last-child { margin-top: 12rpx; color: $color-subtext; font-size: 24rpx; }
</style>
