<template>
  <view class="page-white search-page">
    <view class="header search-header">
      <StatusBar />
      <view class="search-top">
        <view class="back-button" @tap="back"><image class="back-icon" src="/static/icons/back.svg" mode="aspectFit" /></view>
        <view class="search-input-wrap">
          <image class="search-icon" src="/static/icons/search.svg" mode="aspectFit" />
          <input v-model="searchValue" class="search-input" placeholder="搜索商品/型号" />
          <image v-if="searchValue" class="clear" src="/static/icons/close.svg" mode="aspectFit" @tap="searchValue = ''" />
        </view>
        <text class="search-submit" @tap="submitSearch">搜索</text>
      </view>
    </view>

    <view class="search-content">
      <view v-if="history.length" class="search-block">
        <view class="block-title-row">
          <text class="block-title">历史搜索</text>
          <image class="trash" src="/static/icons/trash.svg" mode="aspectFit" @tap="clearHistory" />
        </view>
        <view class="history-tags">
          <text v-for="item in history" :key="item" @tap="searchValue = item">{{ item }}</text>
        </view>
      </view>

      <view class="search-block">
        <view class="block-title-row">
          <text class="block-title">搜索发现</text>
          <image class="eye" :src="showDiscovery ? '/static/icons/eye.svg' : '/static/icons/eye-off.svg'" mode="aspectFit" @tap="showDiscovery = !showDiscovery" />
        </view>
        <view v-if="showDiscovery" class="discover-grid">
          <view v-for="item in discoveryItems" :key="item.id" class="discover-item" @tap="searchValue = item.text">
            <text class="rank" :class="`rank-${item.id}`">{{ item.id }}</text>
            <text class="discover-text">{{ item.text }}</text>
            <text v-if="item.isHot" class="hot">HOT</text>
          </view>
        </view>
        <view v-else class="hidden-tip">当前搜索发现已隐藏</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import StatusBar from "../../components/StatusBar.vue";
import { useConfigStore } from "../../stores/config";

const configStore = useConfigStore();
const searchValue = ref("");
const history = ref<string[]>(JSON.parse(uni.getStorageSync("xq_search_history") || "[]"));
const showDiscovery = ref(true);

const discoveryItems = computed(() => {
  const keywords = configStore.config.hotKeywords || [];
  return keywords.map((text, idx) => ({
    id: idx + 1,
    text,
    isHot: idx === 0,
  }));
});

function back() {
  uni.navigateBack({ delta: 1 });
}

function submitSearch() {
  const val = searchValue.value.trim();
  if (!val) return;
  if (!history.value.includes(val)) {
    history.value.unshift(val);
    if (history.value.length > 10) history.value.pop();
    uni.setStorageSync("xq_search_history", JSON.stringify(history.value));
  }
  uni.navigateTo({ url: `/pages/category/index?keyword=${encodeURIComponent(val)}` });
}

function clearHistory() {
  history.value = [];
  uni.removeStorageSync("xq_search_history");
  uni.showToast({ title: "搜索历史已清空", icon: "none" });
}
</script>

<style scoped lang="scss">
.search-header {
  border-bottom: 1rpx solid #f3f4f6;
}

.search-top {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 18rpx 28rpx;
}

.search-input-wrap {
  height: 72rpx;
  flex: 1;
  min-width: 0;
  border-radius: 999rpx;
  background: #f5f6f8;
  padding: 0 28rpx;
  display: flex;
  align-items: center;
}

.search-input {
  flex: 1;
  min-width: 0;
  height: 72rpx;
  font-size: 28rpx;
  color: #111827;
}

.search-submit {
  color: #111827;
  font-size: 30rpx;
  flex-shrink: 0;
}

.clear {
  width: 30rpx;
  height: 30rpx;
  border-radius: 50%;
  background: #c9cdd4;
  padding: 4rpx;
}

.search-content {
  padding: 36rpx 28rpx;
}

.search-block {
  margin-bottom: 56rpx;
}

.block-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28rpx;
}

.block-title {
  font-size: 30rpx;
  font-weight: 900;
}

.trash {
  width: 32rpx;
  height: 32rpx;
  display: block;
}

.eye {
  width: 38rpx;
  height: 38rpx;
  display: block;
}

.history-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.history-tags text {
  height: 70rpx;
  padding: 0 30rpx;
  border-radius: 999rpx;
  background: #f5f6f8;
  color: #374151;
  font-size: 26rpx;
  display: flex;
  align-items: center;
}

.discover-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 42rpx;
  row-gap: 30rpx;
}

.discover-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  min-width: 0;
}

.rank {
  width: 34rpx;
  height: 34rpx;
  border-radius: 6rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 900;
  color: #9ca3af;
}

.rank-1 { color: #fff; background: #f53f3f; }
.rank-2 { color: #fff; background: #ff7d00; }
.rank-3 { color: #fff; background: #f7ba1e; }

.discover-text {
  font-size: 28rpx;
  color: #374151;
}

.hot {
  color: #f53f3f;
  background: #ffece8;
  font-size: 18rpx;
  font-weight: 900;
  padding: 2rpx 8rpx;
  border-radius: 6rpx;
}

.hidden-tip {
  background: #f8f9fa;
  border-radius: 24rpx;
  color: #9ca3af;
  font-size: 26rpx;
  text-align: center;
  padding: 42rpx 0;
}
</style>
