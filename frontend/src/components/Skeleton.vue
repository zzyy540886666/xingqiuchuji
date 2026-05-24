<template>
  <view class="skeleton">
    <view v-for="i in rows" :key="i" class="skeleton__row" :style="{ width: i === rows ? '60%' : '100%' }"></view>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ rows?: number }>(), { rows: 3 });
</script>

<style scoped lang="scss">
.skeleton {
  padding: 32rpx;
}

.skeleton__row {
  height: 32rpx;
  border-radius: 8rpx;
  background: linear-gradient(90deg, #f3f4f6 25%, #e5e7eb 50%, #f3f4f6 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  margin-bottom: 24rpx;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>
