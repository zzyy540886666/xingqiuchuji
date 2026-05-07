<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import AppTabBar from '@/components/AppTabBar.vue'
import ProductCard from '@/components/ProductCard.vue'
import SectionTitle from '@/components/SectionTitle.vue'
import { products, scenes } from '@/mock/data'

const tabs = [
  { label: '租机器人', value: 'rent' },
  { label: '买机器人', value: 'buy' },
  { label: '软件程序', value: 'software' }
] as const
const active = ref<(typeof tabs)[number]['value']>('rent')
const filtered = computed(() => products.filter(item => item.type === active.value))

onLoad((query) => {
  const t = query?.type
  if (t === 'buy' || t === 'software' || t === 'rent') {
    active.value = t
  }
})

function goSearch() {
  uni.navigateTo({ url: '/pages/search/index' })
}

function goScene(sceneId: string) {
  uni.navigateTo({ url: `/pages/scene/detail?id=${sceneId}` })
}
</script>

<template>
  <view class="page catalog-page">
    <view class="search-bar" @tap="goSearch">
      <image src="/static/icons/search.svg" mode="aspectFit" />
      <text>请输入关键词搜索</text>
    </view>

    <scroll-view scroll-x class="tab-row">
      <text v-for="tab in tabs" :key="tab.value" class="tab-row__item" :class="{ active: active === tab.value }" @tap="active = tab.value">{{ tab.label }}</text>
    </scroll-view>

    <SectionTitle title="场景入口" action="组合方案" />
    <scroll-view scroll-x class="scene-scroll">
      <view v-for="scene in scenes" :key="scene.id" class="scene-chip" @tap="goScene(scene.id)">
        <view :style="{ background: scene.image }" />
        <text>{{ scene.name }}</text>
      </view>
    </scroll-view>

    <SectionTitle title="筛选结果" action="品牌 价格 机型" />
    <view class="product-list">
      <ProductCard v-for="product in filtered" :key="product.id" :product="product" />
    </view>

    <view class="page-bottom" />
    <AppTabBar current="catalog" />
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.catalog-page {
  min-height: 100vh;
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}

.page-bottom {
  height: 24rpx;
}

.search-bar {
  height: 72rpx;
  margin: 24rpx 32rpx 8rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
  border-radius: 999rpx;
  background: #fff;
  color: $color-muted;
  font-size: 26rpx;
}

.search-bar image {
  width: 36rpx;
  height: 36rpx;
}

.tab-row {
  white-space: nowrap;
  padding: 24rpx 0;
}

.tab-row__item {
  display: inline-flex;
  margin-left: 32rpx;
  padding: 16rpx 26rpx;
  border-radius: 999rpx;
  background: #fff;
  color: $color-subtext;
  font-size: 28rpx;
}

.tab-row__item.active {
  background: $color-primary;
  color: #fff;
  font-weight: 800;
}

.scene-scroll {
  white-space: nowrap;
}

.scene-chip {
  display: inline-flex;
  flex-direction: column;
  width: 180rpx;
  margin-left: 32rpx;
  color: $color-text;
  font-size: 24rpx;
}

.scene-chip view {
  height: 110rpx;
  margin-bottom: 12rpx;
  border-radius: $radius-sm;
}

.product-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 22rpx;
  padding: 0 32rpx;
}
</style>
