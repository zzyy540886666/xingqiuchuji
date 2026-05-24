<template>
  <view class="page-white category-page">
    <view class="header category-header">
      <StatusBar />
      <view class="title-bar">
        <view class="bar-spacer"></view>
        <text class="title">分类</text>
        <MiniCapsule />
      </view>
      <view class="category-search">
        <view class="search-bar gray" @tap="go('/pages/search/index')">
          <image class="search-icon" src="/static/icons/search.svg" mode="aspectFit" />
          <text class="search-placeholder">搜索商品/型号</text>
        </view>
      </view>
      <view class="top-tabs">
        <text v-for="tab in topTabs" :key="tab.value" :class="{ active: activeType === tab.value }" @tap="selectTopTab(tab.value)">
          {{ tab.label }}
        </text>
      </view>
    </view>

    <view class="category-main">
      <scroll-view scroll-y class="left-menu">
        <view v-for="section in leftMenu" :key="section.id" class="menu-section">
          <view class="menu-title" @tap="toggle(section.code)">
            <text>{{ section.title }}</text>
            <image class="menu-chevron" :class="{ open: expanded[section.code] }" src="/static/icons/chevron-down.svg" mode="aspectFit" />
          </view>
          <view v-if="expanded[section.code]">
            <view
              v-for="option in section.options"
              :key="option.id"
              class="menu-sub"
              :class="{ active: selectedOptions[section.code]?.id === option.id }"
              @tap="selectOption(section, option)"
            >
              <text>{{ option.label }}</text>
            </view>
          </view>
        </view>
      </scroll-view>

      <view class="right-panel">
        <view class="filters">
          <text v-for="filter in sortFilters" :key="filter" :class="{ active: activeFilter === filter }" @tap="selectFilter(filter)">
            {{ filter }}
          </text>
          <text class="filter-entry" @tap="openFilter">筛选</text>
        </view>
        <scroll-view scroll-y class="product-list">
          <ProductCard v-for="product in visibleProducts" :key="product.id" row :product="product" />
          <view v-if="!loading && !visibleProducts.length" class="empty-products">暂无匹配商品</view>
        </scroll-view>
      </view>
    </view>

    <BottomNav current="category" />
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import BottomNav from "../../components/BottomNav.vue";
import MiniCapsule from "../../components/MiniCapsule.vue";
import ProductCard from "../../components/ProductCard.vue";
import StatusBar from "../../components/StatusBar.vue";
import { getCatalogFilters, getSkuList } from "../../services/catalog";
import type { CatalogFilterGroup, CatalogFilterOption, SkuItem } from "../../services/catalog";

const topTabs = [
  { label: "租机器人", value: "RENT" as const },
  { label: "买机器人", value: "BUY" as const },
  { label: "软件程序", value: "SOFTWARE" as const },
];
const sortFilters = ["综合排序", "价格"];
const activeType = ref<"RENT" | "BUY" | "SOFTWARE">("RENT");
const activeFilter = ref("综合排序");
const leftMenu = ref<CatalogFilterGroup[]>([]);
const selectedOptions = reactive<Record<string, CatalogFilterOption | undefined>>({});
const expanded = reactive<Record<string, boolean>>({});
const displayProducts = ref<SkuItem[]>([]);
const loading = ref(false);
const queryKeyword = ref("");
const priceSort = ref<"" | "asc" | "desc">("");

async function loadFilters() {
  try {
    leftMenu.value = await getCatalogFilters();
    leftMenu.value.forEach((section) => {
      expanded[section.code] = true;
      selectedOptions[section.code] = section.options[0];
    });
  } catch {
    leftMenu.value = [];
  }
}

async function loadProducts() {
  loading.value = true;
  try {
    const params: Parameters<typeof getSkuList>[0] = { type: activeType.value, pageSize: 20 };
    for (const section of leftMenu.value) {
      const selected = selectedOptions[section.code];
      if (!selected) continue;
      if (section.filterField === "BRAND_ID" && selected.value) params.brandId = selected.value;
      if (section.filterField === "MODEL_ID" && selected.value) params.modelId = selected.value;
      if (section.filterField === "PRICE_RANGE") {
        params.minPrice = selected.minPriceMinor;
        params.maxPrice = selected.maxPriceMinor;
      }
      if (section.filterField === "KEYWORD" && selected.value) params.keyword = selected.value;
    }
    if (queryKeyword.value) params.keyword = queryKeyword.value;
    const result = await getSkuList(params);
    displayProducts.value = result.items;
  } catch {
    displayProducts.value = [];
  } finally {
    loading.value = false;
  }
}

watch([activeType, queryKeyword], loadProducts);
onMounted(async () => {
  await loadFilters();
  await loadProducts();
});
onLoad((options) => {
  const incomingType = String(options?.type || "");
  if (incomingType === "RENT" || incomingType === "BUY" || incomingType === "SOFTWARE") activeType.value = incomingType;
  queryKeyword.value = decodeURIComponent(String(options?.keyword || ""));
});

const visibleProducts = computed(() => {
  if (!priceSort.value) return displayProducts.value;
  const multiplier = priceSort.value === "asc" ? 1 : -1;
  return [...displayProducts.value].sort((a, b) => (a.priceAmount - b.priceAmount) * multiplier);
});

function toggle(code: string) {
  expanded[code] = !expanded[code];
}

function selectTopTab(type: "RENT" | "BUY" | "SOFTWARE") {
  activeType.value = type;
  queryKeyword.value = "";
}

function selectOption(section: CatalogFilterGroup, option: CatalogFilterOption) {
  selectedOptions[section.code] = option;
  queryKeyword.value = "";
  loadProducts();
}

function selectFilter(filter: string) {
  activeFilter.value = filter;
  if (filter === "价格") {
    priceSort.value = priceSort.value === "asc" ? "desc" : "asc";
    uni.showToast({ title: priceSort.value === "asc" ? "价格从低到高" : "价格从高到低", icon: "none" });
    return;
  }
  priceSort.value = "";
}

function openFilter() {
  uni.showActionSheet({
    itemList: ["综合排序", "价格从低到高", "价格从高到低"],
    success(result) {
      activeFilter.value = result.tapIndex === 0 ? "综合排序" : "价格";
      priceSort.value = result.tapIndex === 1 ? "asc" : result.tapIndex === 2 ? "desc" : "";
    },
  });
}

function go(url: string) {
  uni.navigateTo({ url });
}
</script>

<style scoped lang="scss">
.category-page { height: 100vh; overflow: hidden; padding-bottom: calc(108rpx + env(safe-area-inset-bottom)); }
.category-header { box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04); }
.bar-spacer { width: 168rpx; }
.category-search { padding: 8rpx 28rpx 16rpx; }
.top-tabs { display: flex; gap: 48rpx; padding: 16rpx 28rpx 0; border-bottom: 1rpx solid #f3f4f6; }
.top-tabs text { height: 60rpx; color: #6b7280; font-size: 28rpx; font-weight: 600; position: relative; }
.top-tabs .active { color: #0a4bfe; }
.top-tabs .active::after { content: ""; position: absolute; left: 0; right: 0; bottom: 0; height: 5rpx; border-radius: 4rpx; background: #0a4bfe; }
.category-main { display: flex; height: calc(100vh - 315rpx); }
.left-menu { width: 208rpx; flex-shrink: 0; background: #f8fafc; padding-bottom: 30rpx; }
.menu-section { padding-top: 22rpx; }
.menu-title { display: flex; justify-content: space-between; align-items: center; padding: 0 16rpx 12rpx 22rpx; color: #111827; font-size: 25rpx; font-weight: 700; }
.menu-chevron { width: 22rpx; height: 22rpx; transition: transform .16s ease; }
.menu-chevron.open { transform: rotate(180deg); }
.menu-sub { padding: 18rpx 12rpx 18rpx 22rpx; color: #6b7280; font-size: 24rpx; }
.menu-sub.active { color: #0a4bfe; font-weight: 700; background: #fff; border-left: 5rpx solid #0a4bfe; }
.right-panel { min-width: 0; flex: 1; padding: 0 22rpx; }
.filters { display: flex; gap: 30rpx; align-items: center; height: 72rpx; border-bottom: 1rpx solid #f3f4f6; font-size: 24rpx; color: #6b7280; }
.filters .active { color: #0a4bfe; font-weight: 700; }
.filter-entry { margin-left: auto; }
.product-list { height: calc(100% - 72rpx); }
.empty-products { padding: 70rpx 0; text-align: center; color: #9ca3af; font-size: 26rpx; }
</style>
