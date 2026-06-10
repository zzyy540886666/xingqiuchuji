<template>
  <view class="page-container">
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px', paddingRight: menuButtonRight + 'px' }">
      <view class="nav-content" :style="{ height: navBarHeight + 'px' }">
        <view class="back-btn" @tap="back">
          <image class="icon-back" src="/static/icons/back.svg" mode="aspectFit" />
        </view>
        <view class="search-box">
          <view class="search-input-wrap">
            <image class="search-icon" src="/static/icons/search.svg" mode="aspectFit" />
            <input v-model="searchValue" class="search-input" placeholder="搜索名称、场景、品牌、适配型号" placeholder-class="input-placeholder" />
          </view>
          <view class="search-btn" @tap="submitSearch">搜索</view>
        </view>
      </view>
    </view>

    <view class="content-padding">
      <view class="section" v-if="history.length > 0">
        <view class="section-header">
          <text class="section-title">搜索历史</text>
          <view class="clear-btn" @tap="clearHistory">
            <image class="icon-small" src="/static/icons/trash.svg" mode="aspectFit" />
            <text>清空</text>
          </view>
        </view>
        <view class="tags-container">
          <view class="tag-item" v-for="item in history" :key="item" @tap="searchValue = item; submitSearch()">{{ item }}</view>
        </view>
      </view>

      <view class="section" v-if="hotKeywords.length > 0">
        <view class="section-header">
          <text class="section-title">热门搜索</text>
        </view>
        <view class="tags-container">
          <view class="tag-item hot-tag" v-for="item in hotKeywords" :key="item" @tap="searchValue = item; submitSearch()">{{ item }}</view>
        </view>
      </view>

      <view class="section" v-if="recommendedScenes.length > 0">
        <view class="section-header">
          <text class="section-title">推荐场景</text>
        </view>
        <scroll-view scroll-x class="scene-scroll" :show-scrollbar="false">
          <view class="scene-list">
            <view class="scene-card" v-for="scene in recommendedScenes" :key="scene.id" @tap="goCategory(scene.name)">
              <image class="scene-img" :src="scene.imageUrl" mode="aspectFill" />
              <text class="scene-name">{{ scene.name }}</text>
              <text class="scene-desc">{{ scene.description }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="section" v-if="relatedProducts.length > 0">
        <view class="section-header">
          <text class="section-title">相关商品</text>
        </view>
        <view class="product-list">
          <view class="product-card" v-for="item in relatedProducts" :key="item.id" @tap="goProduct(item.id)">
            <image class="product-img" :src="item.image" mode="aspectFill" />
            <view class="product-info">
              <text class="product-title">{{ item.title }}</text>
              <text class="product-brand" v-if="item.brand">{{ item.brand }}</text>
              <view class="product-tags" v-if="item.tags && item.tags.length">
                <text class="tag" v-for="tag in item.tags.slice(0, 3)" :key="tag">{{ tag }}</text>
              </view>
              <text class="product-spec" v-if="item.adaptedScenesText">适配场景: {{ item.adaptedScenesText }}</text>
              <text class="product-spec" v-if="item.model">适配型号: {{ item.model }}</text>
              <view class="product-price-row">
                <view>
                  <text class="price-symbol">¥</text>
                  <text class="price-amount">{{ item.priceAmount }}</text>
                  <text class="price-unit" v-if="item.type === 'RENT'">/天起</text>
                  <text class="price-unit" v-else-if="item.type === 'BUY'">起</text>
                </view>
                <image class="icon-arrow-blue" src="/static/icons/chevron-right-blue.svg" mode="aspectFit" />
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useCatalogStore } from "../../stores/catalog";
import { useConfigStore } from "../../stores/config";
import { getScenes, getSkuList, type SceneItem, type SkuItem } from "../../services/catalog";
import { navigateToPage } from "../../utils/navigation";

const configStore = useConfigStore();
const catalogStore = useCatalogStore();
const statusBarHeight = ref(20);
const navBarHeight = ref(44);
const menuButtonRight = ref(0);
const searchValue = ref("");
const history = computed(() => catalogStore.searchHistory);
const hotKeywords = computed(() => configStore.config.hotKeywords || []);
const recommendedScenes = ref<SceneItem[]>([]);
const relatedProducts = ref<SkuItem[]>([]);

onMounted(() => {
  const info = uni.getWindowInfo();
  statusBarHeight.value = info.statusBarHeight || 20;
  try {
    const menuButton = uni.getMenuButtonBoundingClientRect();
    navBarHeight.value = menuButton.height + (menuButton.top - statusBarHeight.value) * 2;
    menuButtonRight.value = info.windowWidth - menuButton.left;
  } catch {}
});

onMounted(async () => {
  const [sceneResult, productResult] = await Promise.allSettled([
    getScenes(),
    getSkuList({ recommended: true, pageSize: 10 }),
  ]);
  recommendedScenes.value = sceneResult.status === "fulfilled" ? sceneResult.value : [];
  relatedProducts.value = productResult.status === "fulfilled" ? productResult.value.items : [];
});

function back() {
  uni.navigateBack({ delta: 1 });
}

function submitSearch() {
  const val = searchValue.value.trim();
  if (!val) return;
  catalogStore.addSearchHistory(val);
  goCategory(val);
}

function clearHistory() {
  catalogStore.clearSearchHistory();
  uni.showToast({ title: "搜索历史已清空", icon: "none" });
}

function goCategory(keyword: string) {
  navigateToPage(`/pages/category/index?keyword=${encodeURIComponent(keyword)}`);
}

function goProduct(id: number) {
  navigateToPage(`/pages/product-detail/index?id=${id}`);
}
</script>

<style scoped lang="scss">
.page-container { min-height: 100vh; background: #fff; }
.nav-bar { position: sticky; top: 0; z-index: 100; padding-left: 16px; background: #fff; }
.nav-content { display: flex; align-items: center; gap: 12px; }
.back-btn { padding: 8px 0; }
.icon-back { width: 20px; height: 20px; display: block; }
.search-box { flex: 1; display: flex; align-items: center; height: 32px; border: 1px solid #2563eb; border-radius: 16px; padding: 2px 2px 2px 12px; }
.search-input-wrap { flex: 1; display: flex; align-items: center; gap: 8px; }
.search-icon { width: 16px; height: 16px; }
.search-input { flex: 1; font-size: 14px; color: #111827; }
.input-placeholder { color: #9ca3af; font-size: 14px; }
.search-btn { height: 28px; padding: 0 14px; border-radius: 14px; background: #2563eb; color: #fff; font-size: 13px; display: flex; align-items: center; }
.content-padding { padding: 16px 16px calc(24px + env(safe-area-inset-bottom)); }
.section { margin-bottom: 24px; }
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.section-title { font-size: 16px; font-weight: 600; color: #111827; }
.clear-btn { display: flex; align-items: center; gap: 4px; color: #9ca3af; font-size: 12px; }
.icon-small { width: 14px; height: 14px; }
.tags-container { display: flex; flex-wrap: wrap; gap: 10px; }
.tag-item { padding: 6px 14px; border-radius: 16px; font-size: 13px; background: #f3f4f6; color: #4b5563; }
.hot-tag { background: #eff6ff; color: #2563eb; }
.scene-scroll { width: 100%; white-space: nowrap; }
.scene-list { display: inline-flex; gap: 12px; }
.scene-card { width: 140px; display: inline-flex; flex-direction: column; gap: 4px; }
.scene-img { width: 140px; height: 90px; border-radius: 8px; background: #f3f4f6; }
.scene-name { font-size: 14px; font-weight: 600; color: #111827; }
.scene-desc { font-size: 12px; color: #6b7280; }
.product-list { display: flex; flex-direction: column; gap: 20px; }
.product-card { display: flex; gap: 12px; }
.product-img { width: 110px; height: 110px; border-radius: 8px; background: #f8f9fa; flex-shrink: 0; }
.product-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.product-title { font-size: 15px; font-weight: 600; color: #111827; line-height: 1.4; }
.product-brand, .product-spec { font-size: 12px; color: #6b7280; }
.product-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.tag { font-size: 11px; color: #2563eb; background: #eff6ff; padding: 2px 6px; border-radius: 4px; }
.product-price-row { display: flex; justify-content: space-between; align-items: flex-end; margin-top: auto; }
.price-symbol { font-size: 12px; color: #dc2626; font-weight: 600; }
.price-amount { font-size: 20px; color: #dc2626; font-weight: 700; }
.price-unit { font-size: 12px; color: #9ca3af; margin-left: 2px; }
.icon-arrow-blue { width: 18px; height: 18px; }
</style>
