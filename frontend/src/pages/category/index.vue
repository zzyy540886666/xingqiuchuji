<template>
  <view class="page-white category-page">
    <view class="header category-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="title-bar">
        <view class="nav-back" @tap="goBack">
          <image class="back-icon" src="/static/icons/back.svg" mode="aspectFit" />
        </view>
        <text class="title">分类</text>
      </view>
      <view class="category-search">
        <view class="search-bar gray" @tap="go('/pages/search/index')">
          <image class="search-icon" src="/static/icons/search.svg" mode="aspectFit" />
          <text class="search-placeholder">搜索机器人、品牌、场景、软件功能</text>
        </view>
      </view>
      <view class="top-tabs">
        <view
          v-for="tab in topTabs"
          :key="tab.value"
          class="tab-item"
          :class="{ active: activeType === tab.value }"
          @tap="selectTopTab(tab.value)"
        >
          <image class="tab-icon" :src="tab.icon" mode="aspectFit" />
          <text>{{ tab.label }}</text>
        </view>
      </view>
    </view>

    <template v-if="pageLoading">
      <Skeleton variant="category" />
    </template>
    <template v-else>

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
            <view class="filter-item" @tap="openDropdown('brand')">
              <text>品牌</text>
              <image class="filter-arrow" src="/static/icons/chevron-down.svg" mode="aspectFit" />
            </view>
            <view class="filter-item" @tap="openDropdown('price')">
              <text>价格</text>
              <image class="filter-arrow" src="/static/icons/chevron-down.svg" mode="aspectFit" />
            </view>
            <view class="filter-item" @tap="openDropdown('model')">
              <text>适配机型</text>
              <image class="filter-arrow" src="/static/icons/chevron-down.svg" mode="aspectFit" />
            </view>
            <view class="filter-item sort" @tap="toggleSort">
              <text :class="{ active: activeFilter === '价格' }">综合排序</text>
              <image class="filter-arrow" src="/static/icons/chevron-down.svg" mode="aspectFit" />
            </view>
            <view class="filter-btn" @tap="openFilter">
              <image class="filter-icon" src="/static/icons/gear.svg" mode="aspectFit" />
              <text>筛选</text>
            </view>
          </view>
          <scroll-view scroll-y class="product-list" @scrolltolower="loadMore">
            <ProductCard v-for="product in visibleProducts" :key="product.id" row :product="product" />
            <view v-if="loading" class="load-tip">
              <view class="loading-spinner" />
              <text>加载中...</text>
            </view>
            <view v-else-if="visibleProducts.length > 0" class="load-tip">
              <text>已加载 {{ visibleProducts.length }} / {{ totalCount }}</text>
            </view>
            <view v-else-if="!loading && !visibleProducts.length" class="empty-products">暂无匹配商品</view>
          </scroll-view>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import ProductCard from "../../components/ProductCard.vue";
import Skeleton from "../../components/PageSkeleton.vue";
import { getCatalogFilters, getSkuList } from "../../services/catalog";
import type { CatalogFilterGroup, CatalogFilterOption, SkuItem } from "../../services/catalog";
import { createLatestTask } from "../../utils/latestTask";
import { navigateBackOrHome, navigateToPage } from "../../utils/navigation";

const topTabs = [
  { label: "租机器人", value: "RENT" as const, icon: "/static/icons/grid.svg" },
  { label: "买机器人", value: "BUY" as const, icon: "/static/icons/check.svg" },
  { label: "软件程序", value: "SOFTWARE" as const, icon: "/static/icons/gear.svg" },
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
const statusBarHeight = ref(0);
const totalCount = ref(0);
const currentPage = ref(1);
const pageLoading = ref(true);
const productTask = createLatestTask();

onMounted(() => {
  const systemInfo = uni.getWindowInfo();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;
});

function goBack() {
  navigateBackOrHome("category_back");
}

async function loadFilters() {
  try {
    leftMenu.value = await getCatalogFilters();
    leftMenu.value.forEach((section) => {
      expanded[section.code] = true;
      selectedOptions[section.code] = defaultOption(section);
    });
  } catch {
    leftMenu.value = [];
  }
}

async function loadProducts(reset = true, targetPage = reset ? 1 : currentPage.value + 1) {
  if (!reset && loading.value) return;
  const requestId = productTask.begin();
  loading.value = true;
  try {
    const params: Parameters<typeof getSkuList>[0] = { type: activeType.value, page: targetPage, pageSize: 20 };
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
    if (!productTask.isCurrent(requestId)) return;
    if (reset) {
      displayProducts.value = result.items;
    } else {
      displayProducts.value.push(...result.items);
    }
    currentPage.value = targetPage;
    totalCount.value = result.total;
  } catch {
    if (reset && productTask.isCurrent(requestId)) displayProducts.value = [];
  } finally {
    if (productTask.isCurrent(requestId)) loading.value = false;
  }
}

function loadMore() {
  if (loading.value || visibleProducts.value.length >= totalCount.value) return;
  loadProducts(false);
}

watch([activeType, queryKeyword], () => loadProducts(true));
onMounted(async () => {
  pageLoading.value = true;
  await loadFilters();
  await loadProducts();
  pageLoading.value = false;
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
  resetFilters();
}

function selectOption(section: CatalogFilterGroup, option: CatalogFilterOption) {
  selectedOptions[section.code] = option;
  queryKeyword.value = "";
  loadProducts(true);
}

function toggleSort() {
  activeFilter.value = activeFilter.value === "价格" ? "综合排序" : "价格";
  if (activeFilter.value === "价格") {
    priceSort.value = priceSort.value === "asc" ? "desc" : "asc";
    uni.showToast({ title: priceSort.value === "asc" ? "价格从低到高" : "价格从高到低", icon: "none" });
  } else {
    priceSort.value = "";
  }
}

function openDropdown(type: string) {
  const sectionMap: Record<string, string> = { brand: "BRAND", price: "PRICE", model: "MODEL" };
  const targetSection = leftMenu.value.find((s) => s.code.includes(sectionMap[type] || type.toUpperCase()));
  if (targetSection && targetSection.options.length > 0) {
    uni.showActionSheet({
      itemList: targetSection.options.map((o) => o.label),
      success(result) {
        selectOption(targetSection, targetSection.options[result.tapIndex]);
      },
    });
  }
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
  navigateToPage(url, { reason: "category_action" });
}

function defaultOption(section: CatalogFilterGroup) {
  return section.options.find((option) =>
    !option.value &&
    option.minPriceMinor == null &&
    option.maxPriceMinor == null
  ) || section.options[0];
}

function resetFilters() {
  leftMenu.value.forEach((section) => {
    selectedOptions[section.code] = defaultOption(section);
  });
  loadProducts(true);
}
</script>

<style scoped lang="scss">
.category-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding-bottom: 0;
}
.category-header { box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04); }
.title-bar { display: flex; align-items: center; justify-content: center; }
.nav-back {
  position: absolute;
  left: 20rpx;
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.back-icon { width: 36rpx; height: 36rpx; }
.category-search { padding: 8rpx 28rpx 16rpx; }

.top-tabs {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 28rpx 20rpx;
}

.tab-item {
  flex: 1;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  border-radius: 12rpx;
  background: #f3f4f6;
  color: #6b7280;
  font-size: 26rpx;
  font-weight: 600;
  transition: all .16s ease;
}

.tab-item.active {
  background: #0a4bfe;
  color: #fff;
}

.tab-icon {
  width: 32rpx;
  height: 32rpx;
}

.category-main {
  flex: 1;
  min-height: 0;
  display: flex;
}

.left-menu {
  width: 208rpx;
  flex-shrink: 0;
  height: 100%;
  background: #f8fafc;
  padding-bottom: env(safe-area-inset-bottom);
}

.menu-section { padding-top: 22rpx; }

.menu-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16rpx 12rpx 22rpx;
  color: #111827;
  font-size: 25rpx;
  font-weight: 700;
}

.menu-chevron {
  width: 22rpx;
  height: 22rpx;
  transition: transform .16s ease;
}

.menu-chevron.open { transform: rotate(180deg); }

.menu-sub {
  padding: 18rpx 12rpx 18rpx 22rpx;
  color: #6b7280;
  font-size: 24rpx;
}

.menu-sub.active {
  color: #0a4bfe;
  font-weight: 700;
  background: #fff;
  border-left: 5rpx solid #0a4bfe;
}

.right-panel {
  min-width: 0;
  flex: 1;
  min-height: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.filters {
  display: flex;
  align-items: center;
  height: 76rpx;
  border-bottom: 1rpx solid #f3f4f6;
  font-size: 22rpx;
  color: #6b7280;
  gap: 16rpx;
  flex-shrink: 0;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 4rpx;
  white-space: nowrap;
}

.filter-item.sort {
  color: #0a4bfe;
  font-weight: 700;
}

.filter-arrow {
  width: 18rpx;
  height: 18rpx;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 4rpx;
  margin-left: auto;
  white-space: nowrap;
}

.filter-icon {
  width: 22rpx;
  height: 22rpx;
}

.product-list {
  flex: 1;
  height: 0;
  min-height: 0;
  padding: 0 22rpx env(safe-area-inset-bottom);
}

.empty-products { padding: 70rpx 0; text-align: center; color: #9ca3af; font-size: 26rpx; }

.load-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 30rpx 0;
  color: #9ca3af;
  font-size: 22rpx;
}

.loading-spinner {
  width: 28rpx;
  height: 28rpx;
  border: 3rpx solid #e5e7eb;
  border-top-color: #0a4bfe;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
