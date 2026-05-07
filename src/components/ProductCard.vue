<script setup lang="ts">
import { formatMoney } from '@/utils/format'
import type { ProductItem } from '@/services/catalog'

const props = defineProps<{ product: ProductItem; compact?: boolean }>()

function goDetail() {
  uni.navigateTo({ url: `/pages/product/detail?id=${props.product.id}` })
}
</script>

<template>
  <view class="product-card" :class="{ compact }" @tap="goDetail">
    <view class="product-card__cover" :style="{ background: product.cover }">
      <view class="robot-mark">
        <view class="robot-mark__head" />
        <view class="robot-mark__body" />
      </view>
      <text class="product-card__tag">{{ product.tag }}</text>
    </view>
    <view class="product-card__body">
      <text class="product-card__name">{{ product.name }}</text>
      <view class="product-card__specs">
        <text v-for="spec in product.specs.slice(0, 2)" :key="spec">{{ spec }}</text>
      </view>
      <view class="product-card__price-row">
        <text class="product-card__price">{{ formatMoney(product.priceCent) }}</text>
        <text class="product-card__origin">{{ formatMoney(product.originalPriceCent) }}</text>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.product-card {
  overflow: hidden;
  border-radius: $radius-md;
  background: $color-card;
  box-shadow: $shadow-card;
}

.product-card.compact {
  width: 332rpx;
  flex-shrink: 0;
}

.product-card__cover {
  position: relative;
  height: 220rpx;
  padding: 18rpx;
}

.robot-mark {
  position: absolute;
  right: 34rpx;
  bottom: 20rpx;
  width: 82rpx;
  height: 118rpx;
}

.robot-mark__head {
  width: 72rpx;
  height: 54rpx;
  margin: 0 auto;
  border-radius: 24rpx;
  background: #16213e;
  box-shadow: inset 0 -10rpx 20rpx rgba(47, 124, 246, 0.5);
}

.robot-mark__body {
  width: 58rpx;
  height: 58rpx;
  margin: 6rpx auto 0;
  border-radius: 18rpx;
  background: rgba(255, 255, 255, 0.82);
  border: 4rpx solid rgba(47, 124, 246, 0.28);
}

.product-card__tag {
  display: inline-flex;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.86);
  color: $color-primary-dark;
  font-size: 22rpx;
  font-weight: 700;
}

.product-card__body {
  padding: 18rpx 20rpx 22rpx;
}

.product-card__name {
  display: block;
  min-height: 72rpx;
  color: $color-text;
  font-size: 28rpx;
  font-weight: 700;
  line-height: 36rpx;
}

.product-card__specs {
  display: flex;
  gap: 10rpx;
  margin-top: 12rpx;
  color: $color-subtext;
  font-size: 22rpx;
}

.product-card__specs text {
  padding: 6rpx 10rpx;
  border-radius: 999rpx;
  background: $color-primary-soft;
}

.product-card__price-row {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  margin-top: 16rpx;
}

.product-card__price {
  color: $color-text;
  font-size: 32rpx;
  font-weight: 800;
}

.product-card__origin {
  color: $color-muted;
  font-size: 22rpx;
  text-decoration: line-through;
}
</style>
