<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { previewOrder } from '@/services/orders'
import { formatMoney } from '@/utils/format'

const skuId = ref('')
const step = ref(1)
const preview = ref({ amountCent: 0, discountCent: 0, payableCent: 0, payMode: '' })

onLoad(async query => {
  skuId.value = typeof query?.skuId === 'string' ? query.skuId : 'sku-rent-alpha'
  preview.value = await previewOrder(skuId.value)
})

function next() {
  if (step.value < 3) step.value += 1
}

function pay() {
  uni.navigateTo({ url: '/pages/order/pay-result?status=success' })
}
</script>

<template>
  <view class="create-page">
    <view class="steps">
      <view v-for="item in [1, 2, 3]" :key="item" class="steps__item" :class="{ active: step >= item }">
        <text>{{ item }}</text>
      </view>
    </view>

    <view v-if="step === 1" class="panel">
      <text class="panel-title">填写服务信息</text>
      <view class="form-row"><text>服务城市</text><text>上海</text></view>
      <view class="form-row"><text>服务日期</text><text>5月8日 - 5月9日</text></view>
      <view class="form-row"><text>服务地点</text><text>请填写详细地址</text></view>
      <button class="primary" @tap="next">下一步</button>
    </view>

    <view v-else-if="step === 2" class="panel">
      <text class="panel-title">订单试算</text>
      <view class="form-row"><text>商品金额</text><text>{{ formatMoney(preview.amountCent) }}</text></view>
      <view class="form-row"><text>活动优惠</text><text>-{{ formatMoney(preview.discountCent) }}</text></view>
      <view class="form-row total"><text>应付金额</text><text>{{ formatMoney(preview.payableCent) }}</text></view>
      <button class="primary" @tap="next">确认试算</button>
    </view>

    <view v-else class="panel">
      <text class="panel-title">微信支付</text>
      <text class="pay-desc">支付参数以后端返回的预支付结果为准，当前为前端样机流程。</text>
      <button class="primary" @tap="pay">调起支付</button>
    </view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.create-page {
  min-height: 100vh;
  padding: 32rpx;
}

.steps {
  display: flex;
  justify-content: center;
  gap: 42rpx;
  margin: 20rpx 0 36rpx;
}

.steps__item {
  width: 54rpx;
  height: 54rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #fff;
  color: $color-muted;
}

.steps__item.active {
  background: $color-primary;
  color: #fff;
  font-weight: 900;
}

.panel {
  padding: 32rpx;
  border-radius: $radius-md;
  background: #fff;
  box-shadow: $shadow-card;
}

.panel-title {
  color: $color-text;
  font-size: 34rpx;
  font-weight: 900;
}

.form-row {
  display: flex;
  justify-content: space-between;
  padding: 28rpx 0;
  border-bottom: 1rpx solid $color-border;
  color: $color-text;
  font-size: 28rpx;
}

.form-row text:last-child {
  color: $color-subtext;
}

.form-row.total text:last-child {
  color: $color-primary;
  font-size: 34rpx;
  font-weight: 900;
}

.pay-desc {
  display: block;
  margin: 28rpx 0;
  color: $color-subtext;
  font-size: 26rpx;
  line-height: 38rpx;
}

.primary {
  width: 100%;
  height: 88rpx;
  margin-top: 34rpx;
  border-radius: 999rpx;
  background: $color-primary;
  color: #fff;
  font-size: 30rpx;
  font-weight: 800;
}
</style>
