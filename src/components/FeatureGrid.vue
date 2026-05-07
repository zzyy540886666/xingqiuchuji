<script setup lang="ts">
defineProps<{ items: Array<{ title: string; subtitle?: string; color?: string; url?: string }> }>()

function go(url?: string) {
  if (url) uni.navigateTo({ url })
}
</script>

<template>
  <view class="feature-grid">
    <view v-for="item in items" :key="item.title" class="feature-grid__item" @tap="go(item.url)">
      <view class="feature-grid__icon" :style="{ background: item.color || '#e8f1ff' }" />
      <text class="feature-grid__title">{{ item.title }}</text>
      <text v-if="item.subtitle" class="feature-grid__subtitle">{{ item.subtitle }}</text>
    </view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 28rpx 12rpx;
}

.feature-grid__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 0;
}

.feature-grid__icon {
  width: 48rpx;
  height: 48rpx;
  border-radius: 16rpx;
  box-shadow: 0 8rpx 16rpx rgba(47, 124, 246, 0.14);
}

.feature-grid__title {
  margin-top: 14rpx;
  color: $color-text;
  font-size: 24rpx;
}

.feature-grid__subtitle {
  margin-top: 6rpx;
  color: $color-muted;
  font-size: 20rpx;
}
</style>
