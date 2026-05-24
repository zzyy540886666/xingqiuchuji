<template>
  <view class="redirect-page">正在进入会员中心...</view>
</template>

<script setup lang="ts">
import { onLoad } from "@dcloudio/uni-app";

onLoad(() => {
  uni.redirectTo({ url: "/pages/member/index" });
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
