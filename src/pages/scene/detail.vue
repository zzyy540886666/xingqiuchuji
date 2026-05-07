<script setup lang="ts">
import ProductCard from '@/components/ProductCard.vue'
import SectionTitle from '@/components/SectionTitle.vue'
import { products, scenes } from '@/mock/data'

const scene = scenes[0]
</script>

<template>
  <view class="scene-page">
    <view class="scene-hero" :style="{ background: scene.image }">
      <text class="scene-hero__title">{{ scene.name }}机器人方案</text>
      <text class="scene-hero__desc">可租、可买、配软件组合推荐，适合活动快启和品牌定制。</text>
    </view>

    <SectionTitle title="方案组合" action="租买软组合" />
    <view class="solution-panel">
      <view class="solution-step">
        <text>选择机器人</text>
        <text>表演、迎宾、讲解</text>
      </view>
      <view class="solution-step">
        <text>配置技能包</text>
        <text>动作、音乐、话术</text>
      </view>
      <view class="solution-step">
        <text>确认服务</text>
        <text>时间、地点、售后</text>
      </view>
    </view>

    <SectionTitle title="推荐商品" />
    <view class="product-grid">
      <ProductCard v-for="product in products" :key="product.id" :product="product" />
    </view>
  </view>
</template>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.scene-page {
  min-height: 100vh;
  padding-bottom: 48rpx;
}

.scene-hero {
  height: 360rpx;
  margin: 32rpx;
  padding: 42rpx;
  border-radius: 36rpx;
  box-shadow: $shadow-card;
}

.scene-hero__title,
.scene-hero__desc {
  display: block;
}

.scene-hero__title {
  color: $color-text;
  font-size: 42rpx;
  font-weight: 900;
}

.scene-hero__desc {
  width: 460rpx;
  margin-top: 18rpx;
  color: $color-subtext;
  font-size: 26rpx;
  line-height: 38rpx;
}

.solution-panel {
  margin: 0 32rpx;
  padding: 28rpx;
  border-radius: $radius-md;
  background: #fff;
  box-shadow: $shadow-card;
}

.solution-step {
  display: flex;
  justify-content: space-between;
  padding: 22rpx 0;
  border-bottom: 1rpx solid $color-border;
  color: $color-text;
  font-size: 28rpx;
}

.solution-step text:last-child {
  color: $color-subtext;
  font-size: 24rpx;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 22rpx;
  padding: 0 32rpx;
}
</style>
