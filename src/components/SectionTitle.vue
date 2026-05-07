<script setup lang="ts">
defineProps<{ title: string; action?: string }>()
</script>

<template>
  <view class="section-title">
    <text class="section-title__main">{{ title }}</text>
    <text v-if="action" class="section-title__action">{{ action }}</text>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 36rpx $page-side 20rpx;
}

.section-title__main {
  color: $color-text;
  font-size: 34rpx;
  font-weight: 700;
}

.section-title__action {
  color: $color-subtext;
  font-size: 24rpx;
}
</style>
