<template>
  <view class="error-retry">
    <text class="error-retry__text">{{ text }}</text>
    <view class="error-retry__btn" @tap="$emit('retry')">
      <text>重试</text>
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ text?: string }>(), {
  text: "加载失败，请重试",
});
defineEmits<{ retry: [] }>();
</script>

<style scoped lang="scss">
.error-retry {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 40rpx;
}

.error-retry__text {
  font-size: 28rpx;
  color: #ef4444;
  margin-bottom: 32rpx;
}

.error-retry__btn {
  padding: 16rpx 40rpx;
  border-radius: 999rpx;
  border: 2rpx solid #0a4bfe;
  color: #0a4bfe;
  font-size: 26rpx;
}
</style>
