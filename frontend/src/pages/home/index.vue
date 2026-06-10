<template>
  <view class="page safe-bottom">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="title-bar">
        <view class="brand">
          <image class="brand-logo" src="/static/icons/planet-logo.svg" mode="aspectFit" />
          <view class="brand-text">
            <view class="title-wrapper">
              <text class="title-text">星球</text>
              <text class="title-dot">·</text>
              <text class="title-text">出机</text>
            </view>
            <text class="sub-title">机器人租赁·购买·软件服务平台</text>
          </view>
        </view>
      </view>
      <view class="home-search">
        <view class="search-bar" @tap="go('/pages/search/index')">
          <image class="search-icon" src="/static/icons/search.svg" mode="aspectFit" />
          <text class="search-placeholder">搜索机器人、场景、品牌、软件</text>
          <text class="search-button">搜索</text>
        </view>
      </view>
    </view>

    <template v-if="loading">
      <Skeleton variant="home" />
    </template>
    <template v-else>

      <view class="hero-shell">
        <swiper v-if="activities.length" class="hero-carousel" circular autoplay :interval="4500" :duration="350" indicator-dots>
          <swiper-item v-for="activity in activities" :key="activity.id">
            <view class="hero" @tap="openActivity(activity)">
              <image v-if="activity.coverUrl" class="hero-image" :src="activity.coverUrl" mode="aspectFill" />
              <view class="hero-mask"></view>
              <view class="hero-content">
                <text class="hero-maker">{{ activity.tag || "精选活动" }}</text>
                <text class="hero-title">{{ activity.title || "机器人场景服务" }}</text>
                <text class="hero-desc">{{ activity.subtitle || "租赁 / 购买 / 软件 · 一站式机器人服务平台" }}</text>
                <view class="hero-cta">查看活动详情</view>
              </view>
            </view>
          </swiper-item>
        </swiper>
        <view v-else class="hero">
          <view class="hero-mask"></view>
          <view class="hero-content">
            <text class="hero-maker">精选活动</text>
            <text class="hero-title">机器人场景服务</text>
            <text class="hero-desc">租赁 / 购买 / 软件 · 一站式机器人服务平台</text>
          </view>
        </view>

        <view class="quick-grid">
          <view class="quick-card rent" @tap="openQuickCard('HOME_RENT_CARD', 'RENT')">
            <text class="quick-title">{{ operationAt("HOME_RENT_CARD")?.title || "租机器人" }}</text><text class="quick-desc">灵活租赁\n按需使用</text>
            <image v-if="operationAt('HOME_RENT_CARD')?.imageUrl" class="quick-img" :src="operationAt('HOME_RENT_CARD')?.imageUrl" mode="aspectFill" />
          </view>
          <view class="quick-card buy" @tap="openQuickCard('HOME_BUY_CARD', 'BUY')">
            <text class="quick-title">{{ operationAt("HOME_BUY_CARD")?.title || "买机器人" }}</text><text class="quick-desc">品质保障\n快速交付</text>
            <image v-if="operationAt('HOME_BUY_CARD')?.imageUrl" class="quick-img" :src="operationAt('HOME_BUY_CARD')?.imageUrl" mode="aspectFill" />
          </view>
          <view class="quick-card app" @tap="openQuickCard('HOME_APP_CARD', 'SOFTWARE')">
            <text class="quick-title">{{ operationAt("HOME_APP_CARD")?.title || "应用商店" }}</text><text class="quick-desc">场景软件\n定制开发</text>
            <image v-if="operationAt('HOME_APP_CARD')?.imageUrl" class="quick-img" :src="operationAt('HOME_APP_CARD')?.imageUrl" mode="aspectFill" />
          </view>
        </view>
      </view>

      <view class="section">
        <view class="section-title-row">
          <text class="section-title">场景应用</text>
          <text class="section-more" @tap="goCategory('RENT')">更多场景</text>
        </view>
        <scroll-view scroll-x class="scene-scroll" show-scrollbar="false">
          <view class="scene-track">
            <view v-for="scene in scenes" :key="scene.id" class="scene-card" @tap="openScene(scene.name)">
              <image v-if="scene.imageUrl" class="scene-img" :src="scene.imageUrl" mode="aspectFill" />
              <view v-else class="scene-img scene-img--empty"></view>
              <text class="scene-title">{{ scene.name }}</text>
              <view class="scene-tags"><text v-for="tag in scene.tags" :key="tag" class="tag">{{ tag }}</text></view>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="section recommend-section">
        <view class="section-title-row">
          <view class="recommend-title">
            <text class="section-title">为你推荐</text>
            <view class="segment">
              <text :class="{ active: activeTab === 'rent' }" @tap="activeTab = 'rent'">可租赁</text>
              <text :class="{ active: activeTab === 'buy' }" @tap="activeTab = 'buy'">可购买</text>
            </view>
          </view>
          <text class="section-more" @tap="goCategory(activeTab === 'rent' ? 'RENT' : 'BUY')">更多推荐</text>
        </view>
        <view class="product-grid">
          <view v-for="product in recommendProducts" :key="product.id" class="product-grid__item">
            <ProductCard :product="product" />
          </view>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { onPullDownRefresh } from "@dcloudio/uni-app";
import ProductCard from "../../components/ProductCard.vue";
import Skeleton from "../../components/PageSkeleton.vue";
import { getActivities } from "../../services/activity";
import type { ActivityDetail } from "../../services/activity";
import { getScenes, getSkuList } from "../../services/catalog";
import type { SceneItem, SkuItem } from "../../services/catalog";
import { useConfigStore } from "../../stores/config";
import type { Banner } from "../../stores/config";
import { createLatestTask } from "../../utils/latestTask";
import { navigateToPage } from "../../utils/navigation";

const configStore = useConfigStore();
type HomeCardPosition = "HOME_RENT_CARD" | "HOME_BUY_CARD" | "HOME_APP_CARD";
const activeTab = ref("rent");
const loading = ref(true);
const activities = ref<ActivityDetail[]>([]);
const scenes = ref<(SceneItem & { tags?: string[] })[]>([]);
const products = ref<SkuItem[]>([]);
const recommendProducts = computed(() => products.value.slice(0, 3));
const statusBarHeight = ref(0);
const productRequestId = createLatestTask();

// 获取状态栏高度
onMounted(() => {
  const systemInfo = uni.getWindowInfo();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;
});

async function loadProducts() {
  const type = activeTab.value === "rent" ? "RENT" : "BUY";
  const requestId = productRequestId.begin();
  try {
    const result = await getSkuList({ type, recommended: true, pageSize: 6 });
    if (productRequestId.isCurrent(requestId)) products.value = result.items;
  } catch {
    if (productRequestId.isCurrent(requestId)) products.value = [];
  }
}

async function loadHome(showSkeleton = false) {
  if (showSkeleton) loading.value = true;
  try {
    const [activityResult, sceneResult] = await Promise.allSettled([getActivities(), getScenes()]);
    if (activityResult.status === "fulfilled") activities.value = activityResult.value;
    if (sceneResult.status === "fulfilled") {
      scenes.value = sceneResult.value.map((scene) => ({
        ...scene,
        tags: scene.tags || configStore.config.sceneTags?.[String(scene.id)] || [],
      }));
    }
    await loadProducts();
  } finally {
    if (showSkeleton) loading.value = false;
  }
}

onMounted(async () => {
  await loadHome(true);
});

onPullDownRefresh(async () => {
  try {
    await loadHome();
  } finally {
    uni.stopPullDownRefresh();
  }
});
watch(activeTab, loadProducts);

function go(url: string) { navigateToPage(url); }
function goCategory(type: "RENT" | "BUY" | "SOFTWARE") { navigateToPage(`/pages/category/index?type=${type}`); }
function operationAt(position: HomeCardPosition): Banner | undefined {
  return (configStore.config.banners || [])
    .filter((item) => item.position === position && (!item.status || item.status === "ACTIVE"))
    .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0))[0];
}
function openQuickCard(position: HomeCardPosition, fallbackType: "RENT" | "BUY" | "SOFTWARE") {
  const targetUrl = operationAt(position)?.linkUrl;
  if (targetUrl) {
    go(targetUrl);
    return;
  }
  goCategory(fallbackType);
}
function openScene(name: string) { navigateToPage(`/pages/category/index?keyword=${encodeURIComponent(name)}`); }
function openActivity(activity: ActivityDetail) {
  navigateToPage(`/pages/activity-detail/index?id=${activity.id}`);
}
</script>

<style scoped lang="scss">
.brand { display: flex; align-items: center; gap: 16rpx; }
.brand-logo { width: 76rpx; height: 76rpx; display: block; }
.brand-text { display: flex; flex-direction: column; justify-content: center; }
.title-wrapper { display: flex; align-items: center; margin-bottom: 6rpx; }
.title-text { color: #000000; font-size: 38rpx; font-weight: 800; letter-spacing: 2rpx; line-height: 1; }
.title-dot { color: #E60000; font-size: 40rpx; font-weight: 900; margin: 0 4rpx; line-height: 1; display: flex; align-items: center; transform: translateY(-2rpx); }
.sub-title { display: block; color: #8C9EB5; font-size: 20rpx; font-weight: 500; letter-spacing: 1rpx; line-height: 1; }
.home-search { padding: 12rpx 28rpx 22rpx; }
.hero-shell { background: #fff; padding: 0 28rpx 28rpx; border-radius: 0 0 32rpx 32rpx; }
.hero-carousel { height: 420rpx; }
.hero { height: 420rpx; border-radius: 32rpx; overflow: hidden; position: relative; background: #111827; }
.hero-carousel .hero { height: 100%; }
.hero-image, .hero-mask { position: absolute; inset: 0; width: 100%; height: 100%; }
.hero-image { opacity: .72; }
.hero-mask { background: linear-gradient(90deg, rgba(0,0,0,.72), transparent); }
.hero-content { position: relative; padding: 54rpx 32rpx; color: #fff; }
.hero-maker { display: block; font-size: 23rpx; opacity: .92; }
.hero-title { display: block; margin-top: 19rpx; font-size: 38rpx; font-weight: 700; }
.hero-desc { display: block; width: 440rpx; margin-top: 15rpx; line-height: 1.55; font-size: 23rpx; }
.hero-cta { display: inline-block; margin-top: 28rpx; padding: 13rpx 30rpx; border-radius: 30rpx; background: #fff; color: #111827; font-size: 24rpx; font-weight: 600; }
.quick-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14rpx; margin-top: 22rpx; }
.quick-card { position: relative; height: 182rpx; overflow: hidden; border-radius: 18rpx; padding: 18rpx; }
.quick-card.rent { background: #eff6ff; }
.quick-card.buy { background: #ecfeff; }
.quick-card.app { background: #f5f3ff; }
.quick-title, .quick-desc { position: relative; z-index: 2; display: block; }
.quick-title { font-size: 26rpx; font-weight: 700; }
.quick-desc { margin-top: 10rpx; font-size: 20rpx; line-height: 1.5; color: #6b7280; white-space: pre-line; }
.quick-img { position: absolute; width: 98rpx; height: 98rpx; right: 0; bottom: 0; opacity: .85; }
.section { margin-top: 18rpx; padding: 28rpx; background: #fff; }
.section-title-row, .recommend-title, .segment, .scene-track, .scene-tags { display: flex; align-items: center; }
.section-title-row { justify-content: space-between; margin-bottom: 22rpx; }
.section-title { color: #111827; font-size: 31rpx; font-weight: 700; }
.section-more { color: #6b7280; font-size: 24rpx; }
.scene-track { gap: 16rpx; }
.scene-card { width: 242rpx; flex-shrink: 0; }
.scene-img { display: block; width: 242rpx; height: 160rpx; border-radius: 16rpx; }
.scene-img--empty { background: linear-gradient(135deg, #eef4ff, #f8fafc); }
.scene-title { display: block; margin-top: 12rpx; font-size: 25rpx; font-weight: 600; }
.scene-tags { gap: 8rpx; margin-top: 9rpx; }
.tag { color: #2563eb; background: #eff6ff; padding: 4rpx 9rpx; border-radius: 5rpx; font-size: 20rpx; }
.recommend-title { gap: 24rpx; }
.segment { background: #f3f4f6; border-radius: 28rpx; padding: 4rpx; }
.segment text { padding: 8rpx 18rpx; font-size: 22rpx; color: #6b7280; }
.segment .active { color: #0a4bfe; background: #fff; border-radius: 22rpx; }
.product-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 18rpx;
}

.product-grid__item {
  width: calc((100% - 18rpx) / 2);
  min-width: 0;
}
</style>
