<script setup lang="ts">
import { computed, ref } from 'vue'
import AppTabBar from '@/components/AppTabBar.vue'
import HomeProductShowcase from '@/components/HomeProductShowcase.vue'
import {
  homeSceneApplications,
  homeShowcaseBuy,
  homeShowcaseRent
} from '@/mock/data'

type RecommendTab = 'rent' | 'buy'

const recommendTab = ref<RecommendTab>('rent')
const showcaseList = computed(() =>
  recommendTab.value === 'rent' ? homeShowcaseRent : homeShowcaseBuy
)

function go(url: string) {
  uni.navigateTo({ url })
}

function goSearch() {
  uni.navigateTo({ url: '/pages/search/index' })
}

function moreScenes() {
  uni.navigateTo({ url: '/pages/catalog/index' })
}

function moreRecommend() {
  uni.navigateTo({ url: '/pages/catalog/index?type=rent' })
}

function onSceneTap(sceneId: string) {
  uni.navigateTo({ url: `/pages/scene/detail?id=${sceneId}` })
}

function exploreBanner() {
  uni.navigateTo({ url: '/pages/catalog/index?type=rent' })
}
</script>

<template>
  <view class="page-home">
    <!-- 状态栏占位 -->
    <view class="home-status-spacer" />

    <!-- 头部品牌 + 搜索（白色卡片，对齐 V3.0） -->
    <view class="home-header-card">
      <view class="brand-row">
        <view class="brand-logo-container">
          <view class="brand-logo-circle"></view>
          <image class="brand-logo-img" src="/static/logo-mark.svg" mode="aspectFit" />
        </view>
        <view class="brand-text">
          <text class="brand-title">星球·出机</text>
          <text class="brand-sub">机器人租赁 · 购买 · 软件服务平台</text>
        </view>
      </view>

      <view class="search-bar" @tap="goSearch">
        <image class="search-bar__icon" src="/static/icons/search.svg" mode="aspectFit" />
        <text class="search-bar__placeholder">搜索机器人、场景、品牌、软件</text>
        <view class="search-bar__btn" @tap.stop="goSearch">
          <text>搜索</text>
        </view>
      </view>
    </view>

    <view class="home-body">
      <!-- 宇树科技 Banner -->
      <view class="hero-unitree" @tap="exploreBanner">
        <image class="hero-unitree__bg" src="/static/images/home/banner.jpg" mode="aspectFill" />
        <view class="hero-unitree__glow" aria-hidden="true" />
        <view class="hero-unitree__content">
          <view class="hero-unitree__copy">
            <image class="hero-unitree__brand-logo" src="/static/logo-mark.svg" mode="aspectFit" />
            <text class="hero-unitree__brand">UNITREE 宇树科技</text>
            <text class="hero-unitree__title">宇树科技 · 智能未来</text>
            <text class="hero-unitree__desc">租赁 / 购买 / 软件 · 一站式机器人服务平台</text>
            <view class="hero-unitree__badges">
              <text class="hero-unitree__pill hero-unitree__pill--fill">春季焕新季</text>
              <text class="hero-unitree__pill hero-unitree__pill--outline">租赁低至5折</text>
            </view>
            <view class="hero-unitree__cta">
              <text>立即探索</text>
              <text class="hero-unitree__cta-arrow">›</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 三大服务入口 -->
      <view class="service-trio">
        <view class="service-card service-card--rent" @tap="go('/pages/catalog/index?type=rent')">
          <text class="service-card__title">租机器人</text>
          <text class="service-card__desc">灵活租赁<br/>按需使用</text>
          <image class="service-card__illus service-card__illus--dog" src="/static/images/home/Go2-W.png" mode="aspectFit" />
          <image class="service-card__arrow" src="/static/icons/arrow-in-circle.svg" mode="aspectFit" />
        </view>
        <view class="service-card service-card--buy" @tap="go('/pages/catalog/index?type=buy')">
          <text class="service-card__title service-card__title--teal">买机器人</text>
          <text class="service-card__desc">品质保障<br/>快速交付</text>
          <image class="service-card__illus service-card__illus--human" src="/static/images/home/G1-u2.png" mode="aspectFit" />
          <image class="service-card__arrow" src="/static/icons/arrow-in-circle-teal.svg" mode="aspectFit" />
        </view>
        <view class="service-card service-card--soft" @tap="go('/pages/catalog/index?type=software')">
          <text class="service-card__title">应用商店</text>
          <text class="service-card__desc">场景软件<br/>定制开发</text>
          <view class="service-card__illus service-card__illus--code">
            <view class="laptop-screen" />
            <view class="laptop-base" />
          </view>
          <image class="service-card__arrow" src="/static/icons/arrow-in-circle.svg" mode="aspectFit" />
        </view>
      </view>

      <!-- 场景应用 -->
      <view class="section-head">
        <text class="section-head__title">场景应用</text>
        <text class="section-head__more" @tap="moreScenes">更多场景 &gt;</text>
      </view>
      <scroll-view class="scene-scroll" scroll-x enable-flex>
        <view
          v-for="scene in homeSceneApplications"
          :key="scene.id"
          class="scene-chip"
          @tap="onSceneTap(scene.id)"
        >
          <view class="scene-chip__cover" :style="{ backgroundImage: scene.image }" />
          <text class="scene-chip__name">{{ scene.name }}</text>
          <view class="scene-chip__actions">
            <text v-for="a in scene.actions" :key="a" class="scene-chip__link">{{ a }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- 为你推荐 -->
      <view class="section-head section-head--rec">
        <text class="section-head__title">为你推荐</text>
        <view class="rec-tabs">
          <view class="rec-tabs__inner">
            <view
              class="rec-tabs__item"
              :class="{ active: recommendTab === 'rent' }"
              @tap="recommendTab = 'rent'"
            >
              <text>可租赁</text>
            </view>
            <view
              class="rec-tabs__item"
              :class="{ active: recommendTab === 'buy' }"
              @tap="recommendTab = 'buy'"
            >
              <text>可购买</text>
            </view>
          </view>
        </view>
        <text class="section-head__more" @tap="moreRecommend">更多推荐 &gt;</text>
      </view>

      <scroll-view class="showcase-list" scroll-x enable-flex>
        <HomeProductShowcase v-for="item in showcaseList" :key="item.id" :item="item" />
      </scroll-view>

      <view class="page-bottom" />
    </view>

    <AppTabBar current="home" />
  </view>
</template>

<style scoped lang="scss">
$design-primary: #0052d9;
$design-bg: #f5f5f5;
$design-text: #000000;
$design-sub: #666666;
$design-muted: #999999;
$design-card: #ffffff;
$design-red: #e34d59;

.page-home {
  min-height: 100vh;
  background: #ffffff;
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}

.home-status-spacer {
  height: env(safe-area-inset-top);
  width: 100%;
  background: #ffffff;
}

.home-header-card {
  padding: 28rpx 24rpx 32rpx;
  background: #ffffff;
}

.brand-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.brand-logo-container {
  width: 80rpx;
  height: 80rpx;
  position: relative;
  flex-shrink: 0;
}

.brand-logo-circle {
  display: none;
}

.brand-logo-img {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  height: 100%;
  z-index: 1;
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.brand-title {
  font-size: 36rpx;
  font-weight: 700;
  color: $design-text;
  line-height: 48rpx;
}

.brand-sub {
  font-size: 22rpx;
  color: $design-sub;
  line-height: 32rpx;
}

.search-bar {
  margin-top: 28rpx;
  height: 72rpx;
  padding: 0 8rpx 0 24rpx;
  border-radius: 999rpx;
  border: 2rpx solid $design-primary;
  background: #ffffff;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.search-bar__icon {
  width: 32rpx;
  height: 32rpx;
  flex-shrink: 0;
}

.search-bar__placeholder {
  flex: 1;
  font-size: 26rpx;
  color: $design-muted;
}

.search-bar__btn {
  height: 56rpx;
  padding: 0 32rpx;
  border-radius: 999rpx;
  background: $design-primary;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.search-bar__btn text {
  font-size: 26rpx;
  color: #fff;
  font-weight: 600;
}

.home-body {
  padding: 24rpx 24rpx 0;
}

/* 宇树 Banner */
.hero-unitree {
  position: relative;
  border-radius: 24rpx;
  overflow: hidden;
  background: #0d1117;
  min-height: 320rpx;
}

.hero-unitree__bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.hero-unitree__glow {
  position: absolute;
  left: 10%;
  right: 10%;
  bottom: -40rpx;
  height: 160rpx;
  background: radial-gradient(ellipse at center, rgba(0, 120, 255, 0.45) 0%, transparent 70%);
  pointer-events: none;
  z-index: 1;
}

.hero-unitree__content {
  position: relative;
  z-index: 2;
  display: flex;
  padding: 36rpx 28rpx 32rpx;
  min-height: 320rpx;
  background: linear-gradient(90deg, rgba(13, 17, 23, 0.9) 0%, rgba(13, 17, 23, 0.4) 50%, transparent 100%);
}

.hero-unitree__copy {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  padding-right: 12rpx;
}

.hero-unitree__brand-logo {
  display: none;
}

.hero-unitree__brand {
  font-size: 24rpx;
  font-weight: 800;
  font-style: italic;
  color: #fff;
  letter-spacing: 1rpx;
}

.hero-unitree__title {
  margin-top: 16rpx;
  font-size: 40rpx;
  font-weight: 700;
  color: #fff;
  line-height: 52rpx;
}

.hero-unitree__desc {
  margin-top: 12rpx;
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.85);
  line-height: 34rpx;
}

.hero-unitree__badges {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 20rpx;
}

.hero-unitree__pill {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
}

.hero-unitree__pill--fill {
  background: $design-primary;
  color: #ffffff;
  border: 1rpx solid $design-primary;
}

.hero-unitree__pill--outline {
  border: 1rpx solid rgba(255, 255, 255, 0.3);
  color: rgba(255, 255, 255, 0.8);
  background: transparent;
}

.hero-unitree__cta {
  margin-top: 28rpx;
  align-self: flex-start;
  padding: 12rpx 32rpx;
  border-radius: 999rpx;
  background: $design-primary;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.hero-unitree__cta text {
  font-size: 24rpx;
  color: #fff;
  font-weight: 600;
}

.hero-unitree__cta-arrow {
  font-size: 28rpx;
  line-height: 1;
  opacity: 0.9;
}

/* 三大服务卡 */
.service-trio {
  display: flex;
  gap: 16rpx;
  margin-top: 24rpx;
}

.service-card {
  flex: 1;
  position: relative;
  min-height: 280rpx;
  padding: 24rpx 16rpx 56rpx;
  border-radius: 24rpx;
  overflow: hidden;
}

.service-card--rent {
  background: linear-gradient(180deg, #f4f8ff 0%, #e6f0ff 100%);
}

.service-card--buy {
  background: linear-gradient(180deg, #f0fbf9 0%, #e0f6f0 100%);
}

.service-card--soft {
  background: linear-gradient(180deg, #f6f7fb 0%, #ebf0f6 100%);
}

.service-card__title {
  font-size: 30rpx;
  font-weight: 700;
  color: $design-primary;
}

.service-card__title--teal {
  color: #00c8b0;
}

.service-card__desc {
  display: block;
  margin-top: 12rpx;
  font-size: 22rpx;
  color: $design-sub;
  line-height: 32rpx;
}

.service-card__illus {
  position: absolute;
  right: 0;
  bottom: 24rpx;
}

.service-card__illus--dog {
  width: 140rpx;
  height: 100rpx;
}

.service-card__illus--human {
  width: 90rpx;
  height: 160rpx;
  right: 10rpx;
}

.service-card__illus--code {
  width: 100rpx;
  height: 80rpx;
  bottom: 30rpx;
  right: 10rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
}

.laptop-screen {
  width: 80rpx;
  height: 56rpx;
  background: #2a2a2a;
  border-radius: 8rpx 8rpx 0 0;
  border: 4rpx solid #1a1a1a;
  border-bottom: none;
}

.laptop-base {
  width: 100rpx;
  height: 8rpx;
  background: #1a1a1a;
  border-radius: 0 0 8rpx 8rpx;
  position: relative;
}

.laptop-base::after {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 20rpx;
  height: 4rpx;
  background: #4a4a4a;
  border-radius: 0 0 4rpx 4rpx;
}

.service-card__arrow {
  position: absolute;
  left: 16rpx;
  bottom: 16rpx;
  width: 40rpx;
  height: 40rpx;
}

/* 场景应用 */
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 40rpx;
  margin-bottom: 20rpx;
  padding: 0 4rpx;
}

.section-head--rec {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  margin-top: 20rpx;
}

.section-head__title {
  font-size: 32rpx;
  font-weight: 700;
  color: $design-text;
}

.section-head__more {
  font-size: 24rpx;
  color: $design-muted;
}

.scene-scroll {
  display: flex;
  flex-direction: row;
  white-space: nowrap;
  width: 100%;
  padding-bottom: 8rpx;
}

.scene-chip {
  display: inline-flex;
  flex-direction: column;
  width: 200rpx;
  margin-right: 16rpx;
  vertical-align: top;
  flex-shrink: 0;
}

.scene-chip:last-child {
  margin-right: 24rpx;
}

.scene-chip__cover {
  width: 200rpx;
  height: 150rpx;
  border-radius: 16rpx;
  background-size: cover;
  background-position: center;
}

.scene-chip__name {
  margin-top: 12rpx;
  font-size: 24rpx;
  font-weight: 600;
  color: $design-text;
  text-align: left;
  white-space: normal;
  line-height: 32rpx;
}

.scene-chip__actions {
  display: flex;
  justify-content: flex-start;
  flex-wrap: wrap;
  gap: 6rpx;
  margin-top: 10rpx;
}

.scene-chip__link {
  font-size: 18rpx;
  color: $design-primary;
  background: rgba(0, 82, 217, 0.08);
  padding: 4rpx 10rpx;
  border-radius: 4rpx;
  line-height: 1.2;
  font-weight: 500;
}

/* 推荐 Tab */
.rec-tabs {
  margin-left: 24rpx;
  flex: 1;
}

.rec-tabs__inner {
  display: flex;
  gap: 12rpx;
  align-items: center;
}

.rec-tabs__item {
  padding: 8rpx 20rpx;
  border-radius: 999rpx;
  background: transparent;
}

.rec-tabs__item text {
  font-size: 24rpx;
  color: $design-sub;
}

.rec-tabs__item.active {
  background: rgba(0, 82, 217, 0.1);
}

.rec-tabs__item.active text {
  color: $design-primary;
  font-weight: 600;
}

.showcase-list {
  display: flex;
  flex-direction: row;
  white-space: nowrap;
  width: 100%;
  padding-bottom: 24rpx;
  padding-top: 12rpx;
}

.page-bottom {
  height: 16rpx;
}
</style>
