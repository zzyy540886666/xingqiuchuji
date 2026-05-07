<script setup lang="ts">
import { computed, ref } from 'vue'
import ProductCard from '@/components/ProductCard.vue'
import { products } from '@/mock/data'

const keyword = ref('')
const hotWords = ['年会机器人', '商场促销', '导购机器人', '技能包']
const results = computed(() => {
  if (!keyword.value.trim()) return products
  return products.filter(item => item.name.includes(keyword.value.trim()) || item.specs.some(spec => spec.includes(keyword.value.trim())))
})
</script>

<template>
  <view class="search-page">
    <view class="search-box">
      <image src="/static/icons/search.svg" mode="aspectFit" />
      <input v-model="keyword" placeholder="请输入关键词搜索" confirm-type="search" />
    </view>

    <view class="hot-panel">
      <text class="panel-title">热门搜索</text>
      <view class="hot-list">
        <text v-for="word in hotWords" :key="word" @tap="keyword = word">{{ word }}</text>
      </view>
    </view>

    <view class="product-grid">
      <ProductCard v-for="product in results" :key="product.id" :product="product" />
    </view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.search-page {
  min-height: 100vh;
  padding: 24rpx 32rpx 48rpx;
}

.search-box {
  height: 76rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
  border-radius: 999rpx;
  background: #fff;
}

.search-box image {
  width: 36rpx;
  height: 36rpx;
}

.search-box input {
  flex: 1;
  color: $color-text;
  font-size: 28rpx;
}

.hot-panel {
  margin: 28rpx 0;
  padding: 28rpx;
  border-radius: $radius-md;
  background: #fff;
  box-shadow: $shadow-card;
}

.panel-title {
  color: $color-text;
  font-size: 30rpx;
  font-weight: 800;
}

.hot-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 22rpx;
}

.hot-list text {
  padding: 12rpx 20rpx;
  border-radius: 999rpx;
  background: $color-primary-soft;
  color: $color-primary;
  font-size: 24rpx;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 22rpx;
}
</style>
