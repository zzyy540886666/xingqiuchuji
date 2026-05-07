<script setup lang="ts">
import { computed, ref } from 'vue'
import EmptyState from '@/components/EmptyState.vue'
import ProductCard from '@/components/ProductCard.vue'
import SectionTitle from '@/components/SectionTitle.vue'
import { orders, products } from '@/mock/data'
import { formatMoney } from '@/utils/format'

const filters = ['全部', '待支付', '进行中', '完成/评价', '已取消']
const active = ref('全部')
const visibleOrders = computed(() => {
  if (active.value === '全部') return orders
  const key = active.value.replace('进行中', '服务中')
  return orders.filter((order) => order.status.includes(key))
})

function goHome() {
  uni.reLaunch({ url: '/pages/home/index' })
}

function goDetail(orderId: string) {
  uni.navigateTo({ url: `/pages/order/detail?id=${orderId}` })
}
</script>

<template>
  <view class="page order-page">
    <view class="search-bar">
      <image src="/static/icons/search.svg" mode="aspectFit" />
      <text>请输入关键词搜索</text>
    </view>
    <scroll-view scroll-x class="filter-row">
      <text v-for="filter in filters" :key="filter" class="filter-row__item" :class="{ active: active === filter }" @tap="active = filter">{{ filter }}</text>
    </scroll-view>

    <EmptyState v-if="visibleOrders.length === 0" title="抱歉，您还没有相关订单" action="前往选购" @action="goHome" />
    <view v-else class="order-list">
      <view v-for="order in visibleOrders" :key="order.id" class="order-card" @tap="goDetail(order.id)">
        <view class="order-card__top">
          <text>{{ order.title }}</text>
          <text>{{ order.status }}</text>
        </view>
        <text class="order-card__amount">{{ formatMoney(order.amountCent) }}</text>
      </view>
    </view>

    <SectionTitle title="为您推荐" />
    <scroll-view scroll-x class="recommend-scroll">
      <ProductCard v-for="product in products" :key="product.id" :product="product" compact />
    </scroll-view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.order-page {
  min-height: 100vh;
  padding-bottom: 48rpx;
  background: linear-gradient(180deg, #fff 0%, #f5f7fb 36%);
}

.search-bar {
  height: 72rpx;
  margin: 24rpx 32rpx 6rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
  border-radius: 999rpx;
  background: #f2f4f8;
  color: $color-muted;
  font-size: 26rpx;
}

.search-bar image {
  width: 36rpx;
  height: 36rpx;
}

.filter-row {
  white-space: nowrap;
  padding: 24rpx 0;
}

.filter-row__item {
  display: inline-flex;
  margin-left: 48rpx;
  color: $color-subtext;
  font-size: 28rpx;
}

.filter-row__item.active {
  color: $color-primary;
  font-weight: 800;
}

.order-list {
  padding: 0 32rpx;
}

.order-card {
  margin-bottom: 20rpx;
  padding: 28rpx;
  border-radius: $radius-md;
  background: #fff;
  box-shadow: $shadow-card;
}

.order-card__top {
  display: flex;
  justify-content: space-between;
  color: $color-text;
  font-size: 28rpx;
}

.order-card__amount {
  display: block;
  margin-top: 18rpx;
  color: $color-primary;
  font-size: 32rpx;
  font-weight: 900;
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
