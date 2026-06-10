<template>
  <view class="page">
    <!-- 自定义导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="back-btn" @tap="goBack"><image class="back-icon" src="/static/icons/back-light.svg" mode="aspectFit" /></view>
      <text class="nav-title">我的资产</text>
      <view class="nav-placeholder"></view>
    </view>

    <template v-if="loading && allAssets.length === 0">
      <Skeleton variant="asset-list" />
    </template>
    <template v-else>

    <!-- 资产状态分组 tabs -->
    <view class="tabs">
      <text v-for="tab in statusTabs" :key="tab.value" class="tab" :class="{ active: currentTab === tab.value }" @tap="currentTab = tab.value">
        {{ tab.label }}
        <text v-if="counts[tab.value] !== undefined" class="tab-count">({{ counts[tab.value] }})</text>
      </text>
    </view>

    <!-- 托管收益入口 -->
    <view class="earnings-entry" @tap="goEarnings">
      <view class="entry-left">
        <image class="entry-icon" src="/static/icons/income.svg" mode="aspectFit" />
        <view class="entry-text">
          <text class="entry-title">托管收益</text>
          <text class="entry-hint">{{ allAssets.length }}台设备 · 总收益 {{ formatAmount(totalRevenue) }}</text>
        </view>
      </view>
      <image class="chevron" src="/static/icons/chevron-right.svg" mode="aspectFit" />
    </view>

    <ErrorRetry v-if="error && allAssets.length === 0" @retry="loadAssets" />
    <EmptyState v-else-if="!loading && filteredAssets.length === 0" text="暂无资产" action-text="去购买" @action="goBuy" />
    <view v-else class="asset-list">
      <view v-for="item in filteredAssets" :key="item.id" class="asset-card" @tap="goDetail(item.id)">
        <image v-if="item.imageUrl" class="asset-img" :src="item.imageUrl" mode="aspectFill" />
        <view v-else class="asset-img placeholder"></view>
        <view class="asset-info">
          <view class="asset-header">
            <text class="asset-name">{{ item.name }}</text>
            <text class="asset-status-tag" :class="item.status">{{ statusLabel(item.status) }}</text>
          </view>
          <text class="asset-model">{{ item.modelInfo }}</text>
          <view class="asset-meta">
            <text class="asset-earnings">累计收益 {{ formatAmount(item.totalRevenueMinor) }}</text>
          </view>
          <view class="asset-actions" @tap.stop>
            <view v-if="item.status === 'IDLE'" class="mini-btn primary" @tap="goTrusteeship(item.id)">去托管</view>
            <view class="mini-btn outline" @tap="goDetail(item.id)">详情</view>
          </view>
        </view>
      </view>
    </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import Skeleton from "../../components/PageSkeleton.vue";
import EmptyState from "../../components/EmptyState.vue";
import ErrorRetry from "../../components/ErrorRetry.vue";
import { formatAmount } from "../../utils/format";
import { getAssets, getAssetDashboard, type Asset } from "../../services/asset";
import { useSessionStore } from "../../stores/session";
import { navigateToPage } from "../../utils/navigation";
import { buildPageUrl } from "../../utils/query";

const session = useSessionStore();

const statusBarHeight = ref(0);
{
  const systemInfo = uni.getWindowInfo();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;
}

type AssetCard = Asset & { totalRevenueMinor: number };

const statusTabs = [
  { label: "全部", value: "" },
  { label: "空闲", value: "IDLE" },
  { label: "托管中", value: "TRUSTEED" },
  { label: "维护中", value: "MAINTENANCE" },
];

const currentTab = ref("");
const allAssets = ref<AssetCard[]>([]);
const loading = ref(true);
const error = ref(false);

const filteredAssets = computed(() => {
  if (!currentTab.value) return allAssets.value;
  return allAssets.value.filter((a) => a.status === currentTab.value);
});

const totalRevenue = computed(() => allAssets.value.reduce((sum, a) => sum + a.totalRevenueMinor, 0));

const counts = computed(() => {
  const c: Record<string, number> = { "": allAssets.value.length };
  for (const tab of statusTabs) {
    if (tab.value) c[tab.value] = allAssets.value.filter((a) => a.status === tab.value).length;
  }
  return c;
});

function statusLabel(s: string): string {
  const m: Record<string, string> = { IDLE: "空闲", TRUSTEED: "托管中", MAINTENANCE: "维护中" };
  return m[s] || s;
}

async function loadAssets() {
  if (!session.token) {
    uni.showToast({ title: "请先登录", icon: "none" });
    setTimeout(() => uni.navigateBack(), 1200);
    return;
  }
  loading.value = true;
  error.value = false;
  try {
    const res = await getAssets();
    allAssets.value = await Promise.all(res.map(async (asset) => {
      const dashboard = await getAssetDashboard(asset.id);
      return { ...asset, totalRevenueMinor: dashboard.totalRevenueMinor };
    }));
  } catch { error.value = true; } finally { loading.value = false; }
}

onShow(() => loadAssets());

function goDetail(id: string) { navigateToPage(buildPageUrl("/pages/asset/detail", { id })); }
function goTrusteeship(id: string) { navigateToPage(buildPageUrl("/pages/asset/trusteeship", { id })); }
function goEarnings() { uni.navigateTo({ url: "/pages/asset/earnings" }); }
function goBuy() { navigateToPage("/pages/category/index"); }
function goBack() { uni.navigateBack({ delta: 1 }); }
</script>

<style scoped lang="scss">
.nav-bar {
  height: 88rpx;
  padding: 0 28rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1rpx solid #f3f4f6;
}
.back-btn {
  width: 56rpx; height: 56rpx;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex; align-items: center; justify-content: center;
}
.back-icon { width: 28rpx; height: 28rpx; display: block; }
.nav-title { font-size: 32rpx; font-weight: 700; color: #111827; }
.nav-placeholder { width: 56rpx; }

.tabs { display: flex; padding: 24rpx 28rpx; gap: 16rpx; background: #fff; }
.tab { font-size: 26rpx; color: #6b7280; padding: 10rpx 24rpx; border-radius: 999rpx; background: #f3f4f6; }
.tab.active { background: #eef2ff; color: #0a4bfe; font-weight: 600; }
.tab-count { font-size: 22rpx; }

.earnings-entry {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #e8f1ff, #f0f5ff);
  margin: 20rpx 28rpx;
  border-radius: 20rpx;
  padding: 28rpx;
}
.entry-left { display: flex; align-items: center; gap: 16rpx; }
.entry-icon { width: 56rpx; height: 56rpx; border-radius: 16rpx; background: #dbeafe; padding: 12rpx; }
.entry-text { display: flex; flex-direction: column; gap: 4rpx; }
.entry-title { font-size: 28rpx; font-weight: 700; color: #111827; }
.entry-hint { font-size: 22rpx; color: #6b7280; }
.chevron { width: 32rpx; height: 32rpx; }

.asset-list { padding: 8rpx 28rpx 40rpx; }
.asset-card { display: flex; gap: 20rpx; background: #fff; border-radius: 20rpx; padding: 24rpx; margin-bottom: 20rpx; }
.asset-img { width: 140rpx; height: 140rpx; border-radius: 16rpx; background: #f3f4f6; flex-shrink: 0; }
.asset-img.placeholder { background: linear-gradient(135deg, #e5e7eb, #f3f4f6); }
.asset-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; min-width: 0; }
.asset-header { display: flex; justify-content: space-between; align-items: center; }
.asset-name { font-size: 28rpx; font-weight: 700; color: #111827; }
.asset-status-tag { font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 999rpx; font-weight: 600; }
.asset-status-tag.IDLE { background: #dbeafe; color: #1d4ed8; }
.asset-status-tag.TRUSTEED { background: #dcfce7; color: #15803d; }
.asset-status-tag.MAINTENANCE { background: #fef3c7; color: #92400e; }
.asset-model { font-size: 24rpx; color: #6b7280; }
.asset-meta { display: flex; justify-content: space-between; align-items: center; }
.asset-earnings { font-size: 22rpx; color: #f59e0b; font-weight: 600; }
.asset-actions { display: flex; gap: 12rpx; margin-top: 8rpx; }
.mini-btn { padding: 8rpx 20rpx; border-radius: 999rpx; font-size: 22rpx; font-weight: 600; }
.mini-btn.primary { background: #0a4bfe; color: #fff; }
.mini-btn.outline { border: 2rpx solid #d1d5db; color: #6b7280; }
</style>
