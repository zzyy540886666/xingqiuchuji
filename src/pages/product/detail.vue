<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getSkuDetail } from '@/services/catalog'
import type { ProductItem } from '@/services/catalog'
import { formatMoney } from '@/utils/format'

interface DetailProduct extends ProductItem {
  qa: Array<{ question: string; answer: string }>
}

const product = ref<DetailProduct | null>(null)
const activeQa = ref(0)

onLoad(async query => {
  const id = typeof query?.id === 'string' ? query.id : 'sku-rent-alpha'
  product.value = await getSkuDetail(id) as DetailProduct
})

function createOrder() {
  if (!product.value || product.value.availability !== 'available') {
    uni.showToast({ title: '当前不可下单', icon: 'none' })
    return
  }
  uni.navigateTo({ url: `/pages/order/create?skuId=${product.value.id}` })
}
</script>

<template>
  <view v-if="product" class="detail-page">
    <view class="media" :style="{ background: product.cover }">
      <view class="robot-large">
        <view class="robot-large__head" />
        <view class="robot-large__body" />
      </view>
    </view>

    <view class="panel product-main">
      <view class="tag-row">
        <text>{{ product.tag }}</text>
        <text>{{ product.availability === 'available' ? '可租可买' : '库存紧张' }}</text>
      </view>
      <text class="product-name">{{ product.name }}</text>
      <view class="price-row">
        <text class="price">{{ formatMoney(product.priceCent) }}</text>
        <text class="origin">{{ formatMoney(product.originalPriceCent) }}</text>
      </view>
    </view>

    <view class="panel">
      <text class="panel-title">参数与能力</text>
      <view class="spec-list">
        <text v-for="spec in product.specs" :key="spec">{{ spec }}</text>
      </view>
    </view>

    <view class="panel">
      <text class="panel-title">常见问题</text>
      <view v-for="(qa, index) in product.qa" :key="qa.question" class="qa-item" @tap="activeQa = index">
        <view class="qa-item__question">
          <text>{{ qa.question }}</text>
          <text>{{ activeQa === index ? '收起' : '展开' }}</text>
        </view>
        <text v-if="activeQa === index" class="qa-item__answer">{{ qa.answer }}</text>
      </view>
    </view>

    <view class="action-bar">
      <button class="secondary">加入购物车</button>
      <button class="primary" :class="{ disabled: product.availability !== 'available' }" @tap="createOrder">{{ product.type === 'rent' ? '立即租赁' : product.type === 'buy' ? '立即购买' : '获取软件' }}</button>
    </view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.detail-page {
  min-height: 100vh;
  padding-bottom: 140rpx;
}

.media {
  height: 560rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.robot-large {
  width: 220rpx;
  height: 300rpx;
}

.robot-large__head {
  width: 180rpx;
  height: 116rpx;
  margin: 0 auto;
  border-radius: 48rpx;
  background: #111a35;
  box-shadow: inset 0 -18rpx 36rpx rgba(47, 124, 246, 0.7);
}

.robot-large__body {
  width: 138rpx;
  height: 140rpx;
  margin: 16rpx auto 0;
  border-radius: 42rpx;
  background: rgba(255, 255, 255, 0.9);
  border: 6rpx solid rgba(47, 124, 246, 0.2);
}

.panel {
  margin: 24rpx 32rpx 0;
  padding: 28rpx;
  border-radius: $radius-md;
  background: #fff;
  box-shadow: $shadow-card;
}

.tag-row {
  display: flex;
  gap: 16rpx;
}

.tag-row text {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: $color-primary-soft;
  color: $color-primary;
  font-size: 22rpx;
}

.product-name {
  display: block;
  margin-top: 22rpx;
  color: $color-text;
  font-size: 38rpx;
  font-weight: 900;
  line-height: 48rpx;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 16rpx;
  margin-top: 22rpx;
}

.price {
  color: $color-primary;
  font-size: 42rpx;
  font-weight: 900;
}

.origin {
  color: $color-muted;
  text-decoration: line-through;
}

.panel-title {
  color: $color-text;
  font-size: 32rpx;
  font-weight: 800;
}

.spec-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 22rpx;
}

.spec-list text {
  padding: 12rpx 20rpx;
  border-radius: 999rpx;
  background: $color-primary-soft;
  color: $color-primary-dark;
  font-size: 24rpx;
}

.qa-item {
  padding: 22rpx 0;
  border-bottom: 1rpx solid $color-border;
}

.qa-item__question {
  display: flex;
  justify-content: space-between;
  color: $color-text;
  font-size: 28rpx;
}

.qa-item__answer {
  display: block;
  margin-top: 14rpx;
  color: $color-subtext;
  font-size: 24rpx;
  line-height: 34rpx;
}

.action-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: grid;
  grid-template-columns: 0.8fr 1.2fr;
  gap: 20rpx;
  padding: 20rpx 32rpx calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -12rpx 32rpx rgba(17, 24, 39, 0.06);
}

.action-bar button {
  height: 88rpx;
  border-radius: 999rpx;
  font-size: 30rpx;
  font-weight: 800;
}

.secondary {
  background: $color-primary-soft;
  color: $color-primary;
}

.primary {
  background: $color-primary;
  color: #fff;
}

.primary.disabled {
  background: $color-muted;
}
</style>
