<script setup lang="ts">
defineProps<{ title: string; action?: string }>()
const emit = defineEmits<{ action: [] }>()
</script>

<template>
  <view class="empty-state">
    <image class="empty-state__icon" src="/static/icons/doc.svg" mode="aspectFit" />
    <text class="empty-state__title">{{ title }}</text>
    <button v-if="action" class="empty-state__button" @tap="emit('action')">{{ action }}</button>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 32rpx 72rpx;
}

.empty-state__icon {
  width: 144rpx;
  height: 144rpx;
  opacity: 0.88;
}

.empty-state__title {
  margin-top: 24rpx;
  color: $color-muted;
  font-size: 28rpx;
}

.empty-state__button {
  width: 460rpx;
  height: 84rpx;
  margin-top: 44rpx;
  border-radius: 10rpx;
  background: $color-primary;
  color: #fff;
  font-size: 30rpx;
  font-weight: 700;
}
</style>
