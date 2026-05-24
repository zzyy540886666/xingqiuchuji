<template>
  <view class="page safe-bottom">
    <view class="header">
      <StatusBar />
      <view class="title-bar">
        <view class="brand">
          <image class="brand-logo" src="/static/icons/app-logo.svg" mode="aspectFit" />
          <view>
            <text class="title">星球·出机</text>
            <text class="sub-title">机器人租赁、购买、软件服务平台</text>
          </view>
        </view>
        <MiniCapsule />
      </view>
      <view class="home-search">
        <view class="search-bar" @tap="go('/pages/search/index')">
          <image class="search-icon" src="/static/icons/search.svg" mode="aspectFit" />
          <text class="search-placeholder">搜索机器人、场景、品牌、软件</text>
          <text class="search-button">搜索</text>
        </view>
      </view>
    </view>

    <view class="hero-shell">
      <view class="hero" @tap="openActivity">
        <image class="hero-image" :src="heroActivity?.coverUrl || '/static/images/robot-humanoid.png'" mode="aspectFill" />
        <view class="hero-mask"></view>
        <view class="hero-content">
          <text class="hero-maker">{{ heroActivity?.tag || "精选活动" }}</text>
          <text class="hero-title">{{ heroActivity?.title || "机器人场景服务" }}</text>
          <text class="hero-desc">{{ heroActivity?.subtitle || "租赁 / 购买 / 软件 · 一站式机器人服务平台" }}</text>
          <view class="hero-cta">查看活动详情</view>
        </view>
      </view>

      <view class="quick-grid">
        <view class="quick-card rent" @tap="goCategory('RENT')">
          <text class="quick-title">租机器人</text><text class="quick-desc">灵活租赁\n按需使用</text>
          <image class="quick-img" src="/static/images/robot-dog.png" mode="aspectFill" />
        </view>
        <view class="quick-card buy" @tap="goCategory('BUY')">
          <text class="quick-title">买机器人</text><text class="quick-desc">品质保障\n快速交付</text>
          <image class="quick-img" src="/static/images/robot-humanoid.png" mode="aspectFill" />
        </view>
        <view class="quick-card app" @tap="goCategory('SOFTWARE')">
          <text class="quick-title">应用商店</text><text class="quick-desc">场景软件\n定制开发</text>
          <image class="quick-img" src="/static/images/hero-robot.jpg" mode="aspectFill" />
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
            <image class="scene-img" :src="scene.imageUrl" mode="aspectFill" />
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
        <ProductCard v-for="product in recommendProducts" :key="product.id" :product="product" />
      </view>
    </view>
    <BottomNav current="home" />
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import BottomNav from "../../components/BottomNav.vue";
import MiniCapsule from "../../components/MiniCapsule.vue";
import ProductCard from "../../components/ProductCard.vue";
import StatusBar from "../../components/StatusBar.vue";
import { getActivities } from "../../services/activity";
import type { ActivityDetail } from "../../services/activity";
import { getScenes, getSkuList } from "../../services/catalog";
import type { SceneItem, SkuItem } from "../../services/catalog";
import { useConfigStore } from "../../stores/config";

const configStore = useConfigStore();
const activeTab = ref("rent");
const activities = ref<ActivityDetail[]>([]);
const scenes = ref<(SceneItem & { tags?: string[] })[]>([]);
const products = ref<SkuItem[]>([]);
const heroActivity = computed(() => activities.value[0]);
const recommendProducts = computed(() => products.value.slice(0, 3));

async function loadProducts() {
  const type = activeTab.value === "rent" ? "RENT" : "BUY";
  try {
    products.value = (await getSkuList({ type, pageSize: 6 })).items;
  } catch {
    products.value = [];
  }
}

onMounted(async () => {
  const [activityResult, sceneResult] = await Promise.allSettled([getActivities(), getScenes()]);
  if (activityResult.status === "fulfilled") activities.value = activityResult.value;
  if (sceneResult.status === "fulfilled") {
    scenes.value = sceneResult.value.map((scene) => ({ ...scene, tags: configStore.config.sceneTags?.[String(scene.id)] || [] }));
  }
  await loadProducts();
});
watch(activeTab, loadProducts);

function go(url: string) { uni.navigateTo({ url }); }
function goCategory(type: "RENT" | "BUY" | "SOFTWARE") { uni.navigateTo({ url: `/pages/category/index?type=${type}` }); }
function openScene(name: string) { uni.navigateTo({ url: `/pages/category/index?keyword=${encodeURIComponent(name)}` }); }
function openActivity() {
  if (heroActivity.value) uni.navigateTo({ url: `/pages/activity-detail/index?id=${heroActivity.value.id}` });
}
</script>

<style scoped lang="scss">
.brand { display: flex; align-items: center; gap: 18rpx; }
.brand-logo { width: 68rpx; height: 68rpx; display: block; }
.title, .sub-title { display: block; }
.title { color: #111827; font-size: 32rpx; font-weight: 700; }
.sub-title { color: #6b7280; font-size: 21rpx; }
.home-search { padding: 12rpx 28rpx 22rpx; }
.hero-shell { background: #fff; padding: 0 28rpx 28rpx; border-radius: 0 0 32rpx 32rpx; }
.hero { height: 420rpx; border-radius: 32rpx; overflow: hidden; position: relative; background: #111827; }
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
.scene-title { display: block; margin-top: 12rpx; font-size: 25rpx; font-weight: 600; }
.scene-tags { gap: 8rpx; margin-top: 9rpx; }
.tag { color: #2563eb; background: #eff6ff; padding: 4rpx 9rpx; border-radius: 5rpx; font-size: 20rpx; }
.recommend-title { gap: 24rpx; }
.segment { background: #f3f4f6; border-radius: 28rpx; padding: 4rpx; }
.segment text { padding: 8rpx 18rpx; font-size: 22rpx; color: #6b7280; }
.segment .active { color: #0a4bfe; background: #fff; border-radius: 22rpx; }
.product-grid { display: grid; grid-template-columns: repeat(2, minmax(0,1fr)); gap: 18rpx; }
</style>
