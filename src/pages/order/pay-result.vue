<script setup lang="ts">
function goOrders() {
  uni.redirectTo({ url: '/pages/order/list' })
}

function goHome() {
  uni.redirectTo({ url: '/pages/home/index' })
}
</script>

<template>
  <view class="result-page">
    <view class="result-card">
      <view class="success-mark" />
      <text class="title">支付成功</text>
      <text class="desc">订单状态以后端确认为准，可稍后进入订单详情查看合同入口。</text>
      <button @tap="goOrders">查看订单</button>
      <button class="ghost" @tap="goHome">返回首页</button>
    </view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.result-page {
  min-height: 100vh;
  padding: 120rpx 32rpx;
}

.result-card {
  padding: 56rpx 32rpx;
  border-radius: $radius-lg;
  background: #fff;
  box-shadow: $shadow-card;
  text-align: center;
}

.success-mark {
  width: 112rpx;
  height: 112rpx;
  margin: 0 auto 28rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #19c37d, #8cf2c2);
}

.title,
.desc {
  display: block;
}

.title {
  color: $color-text;
  font-size: 40rpx;
  font-weight: 900;
}

.desc {
  margin: 20rpx 0 42rpx;
  color: $color-subtext;
  font-size: 26rpx;
  line-height: 38rpx;
}

button {
  height: 84rpx;
  margin-top: 18rpx;
  border-radius: 999rpx;
  background: $color-primary;
  color: #fff;
  font-size: 30rpx;
  font-weight: 800;
}

button.ghost {
  background: $color-primary-soft;
  color: $color-primary;
}
</style>
