<script setup lang="ts">
export interface ShowcaseItem {
  id: string
  name: string
  badge: string
  pricePerDayYuan?: number
  priceBuyYuan?: number
  tags: string[]
  cover: string
  mode: 'rent' | 'buy'
}

const props = defineProps<{ item: ShowcaseItem }>()

function goDetail() {
  uni.navigateTo({ url: `/pages/product/detail?id=${props.item.id}` })
}

function onRent() {
  uni.navigateTo({ url: `/pages/order/create?skuId=${props.item.id}` })
}

function onCart() {
  uni.navigateTo({ url: '/pages/cart/index' })
}
</script>

<template>
  <view class="showcase">
    <view class="showcase__media" :style="{ backgroundImage: item.cover, backgroundSize: 'contain', backgroundRepeat: 'no-repeat', backgroundPosition: 'center' }" @tap="goDetail">
      <text class="showcase__badge" :class="item.mode === 'buy' ? 'showcase__badge--buy' : ''">{{ item.badge }}</text>
    </view>
    <view class="showcase__body">
      <text class="showcase__title">{{ item.name }}</text>
      <view class="showcase__tags">
        <text v-for="t in item.tags" :key="t" class="showcase__tag">{{ t }}</text>
      </view>
      <view class="showcase__price-row">
        <view v-if="item.mode === 'rent' && item.pricePerDayYuan != null" class="showcase__price">
          <text class="showcase__price-label">租赁价</text>
          <text class="showcase__price-num"><text style="font-size: 20rpx; margin-right: 2rpx;">¥</text>{{ item.pricePerDayYuan }}</text>
          <text class="showcase__price-unit">/天起</text>
        </view>
        <view v-else-if="item.mode === 'buy' && item.priceBuyYuan != null" class="showcase__price showcase__price--buy">
          <text class="showcase__price-label">购买价</text>
          <text class="showcase__price-num"><text style="font-size: 20rpx; margin-right: 2rpx;">¥</text>{{ item.priceBuyYuan }}</text>
          <text class="showcase__price-unit">起</text>
        </view>
      </view>
      <view class="showcase__actions">
        <button class="showcase__btn-main" @tap.stop="item.mode === 'rent' ? onRent() : goDetail()">
          {{ item.mode === 'rent' ? '立即租赁' : '立即购买' }}
        </button>
        <view class="showcase__btn-cart" @tap.stop="onCart">
          <image src="/static/icons/cart-outline.svg" mode="aspectFit" class="showcase__cart-icon" />
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
$design-primary: #0052d9;
$design-red: #e34d59;

.showcase {
  width: 280rpx;
  border-radius: 16rpx;
  overflow: hidden;
  background: #fff;
  margin-right: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  display: inline-block;
  vertical-align: top;
}

.showcase__media {
  position: relative;
  height: 280rpx;
  background-color: #f9f9f9;
}

.showcase__badge {
  position: absolute;
  left: 12rpx;
  bottom: 12rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx 8rpx 16rpx 8rpx;
  background: rgba(0, 82, 217, 0.1);
  color: #0052d9;
  font-size: 18rpx;
  font-weight: 600;
}

.showcase__badge--buy {
  background: linear-gradient(135deg, #e34d59 0%, #ff7a45 100%);
}

.showcase__body {
  padding: 16rpx;
}

.showcase__title {
  font-size: 26rpx;
  font-weight: 700;
  color: #333;
  line-height: 36rpx;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.showcase__tags {
  display: flex;
  flex-wrap: nowrap;
  gap: 8rpx;
  margin-top: 12rpx;
  overflow: hidden;
}

.showcase__tag {
  padding: 4rpx 10rpx;
  border-radius: 4rpx;
  background: #f5f5f5;
  color: #999;
  font-size: 18rpx;
  white-space: nowrap;
}

.showcase__price-row {
  margin-top: 16rpx;
}

.showcase__price {
  display: flex;
  align-items: baseline;
  flex-wrap: nowrap;
}

.showcase__price-label {
  font-size: 18rpx;
  color: #999;
  margin-right: 6rpx;
}

.showcase__price-num {
  font-size: 32rpx;
  font-weight: 700;
  color: $design-red;
}

.showcase__price-unit {
  font-size: 18rpx;
  color: #999;
  margin-left: 2rpx;
}

.showcase__price--buy .showcase__price-num {
  color: $design-red;
}

.showcase__price--buy .showcase__price-unit {
  color: #999;
}

.showcase__actions {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 16rpx;
}

.showcase__btn-main {
  flex: 1;
  height: 56rpx;
  line-height: 56rpx;
  border-radius: 999rpx;
  background: $design-primary !important;
  color: #fff !important;
  font-size: 22rpx;
  font-weight: 600;
  border: none !important;
  margin: 0;
  padding: 0;
}

.showcase__btn-cart {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  border: 2rpx solid #0052d9;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.showcase__cart-icon {
  width: 32rpx;
  height: 32rpx;
}
</style>
