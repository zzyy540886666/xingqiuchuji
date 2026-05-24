<template>
  <view class="redirect-page">正在进入订单确认页...</view>
</template>

<script setup lang="ts">
import { onLoad } from "@dcloudio/uni-app";

onLoad((options) => {
  const params = Object.entries(options || {})
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    .join("&");
  uni.redirectTo({ url: `/pages/order/confirm${params ? `?${params}` : ""}` });
});
</script>

<style scoped>
.redirect-page {
  padding: 80rpx 28rpx;
  color: #6b7280;
  font-size: 28rpx;
  text-align: center;
}
</style>
