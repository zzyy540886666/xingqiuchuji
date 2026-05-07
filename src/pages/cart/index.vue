<script setup lang="ts">
import EmptyState from '@/components/EmptyState.vue'
import ProductCard from '@/components/ProductCard.vue'
import SectionTitle from '@/components/SectionTitle.vue'
import { products } from '@/mock/data'

function goShopping() {
  uni.navigateTo({ url: '/pages/catalog/index' })
}
</script>

<template>
  <view class="page cart-page">
    <EmptyState title="购物车还没有加入商品" action="前往选购" @action="goShopping" />
    <SectionTitle title="为您推荐" />
    <scroll-view scroll-x class="recommend-scroll">
      <ProductCard v-for="product in products" :key="product.id" :product="product" compact />
    </scroll-view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.cart-page {
  min-height: 100vh;
  padding-bottom: 48rpx;
}

.recommend-scroll {
  white-space: nowrap;
  padding-left: 32rpx;
}

.recommend-scroll :deep(.product-card) {
  display: inline-block;
  margin-right: 22rpx;
  vertical-align: top;
}
</style>
