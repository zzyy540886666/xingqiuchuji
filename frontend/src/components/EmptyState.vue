<template>
  <view class="empty-state">
    <image class="empty-state__icon" src="/static/icons/empty.svg" mode="aspectFit" />
    <text class="empty-state__text">{{ text }}</text>
    <view v-if="actionText" class="empty-state__action" @tap="$emit('action')">
      <text>{{ actionText }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ text?: string; actionText?: string }>(), {
  text: "暂无数据",
});
defineEmits<{ action: [] }>();
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 40rpx;
}

.empty-state__icon {
  margin-bottom: 32rpx;
  width: 120rpx;
  height: 120rpx;
  display: block;
}

.empty-state__text {
  font-size: 28rpx;
  color: #9ca3af;
}

.empty-state__action {
  margin-top: 32rpx;
  padding: 16rpx 40rpx;
  border-radius: 999rpx;
  background: #0a4bfe;
  color: #fff;
  font-size: 26rpx;
}
</style>
