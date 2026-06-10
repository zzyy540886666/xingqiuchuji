<template>
  <view class="page earnings-page">
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="back-btn" @tap="goBack">
        <image class="back-icon" src="/static/icons/back-light.svg" mode="aspectFit" />
      </view>
      <text class="nav-title">托管收益</text>
      <view class="nav-placeholder"></view>
    </view>

    <Skeleton v-if="loading" variant="asset-earnings" />
    <template v-else>
      <ErrorRetry v-if="error" @retry="loadEarnings" />
      <template v-else>
        <view class="summary-card">
          <view class="summary-header">
            <view>
              <text class="summary-label">累计收益</text>
              <text class="summary-value">{{ formatAmount(earnings.totalRevenueMinor) }}</text>
            </view>
            <view class="range-switch">
              <text v-for="item in ranges" :key="item.value" class="range-item" :class="{ active: days === item.value }" @tap="changeRange(item.value)">
                {{ item.label }}
              </text>
            </view>
          </view>
          <view class="summary-grid">
            <view class="summary-item">
              <text class="summary-num">{{ formatAmount(earnings.monthRevenueMinor) }}</text>
              <text class="summary-name">本月收益</text>
            </view>
            <view class="summary-item">
              <text class="summary-num">{{ formatAmount(earnings.pendingRevenueMinor) }}</text>
              <text class="summary-name">待结算</text>
            </view>
            <view class="summary-item">
              <text class="summary-num">{{ earnings.activeDeviceCount }}</text>
              <text class="summary-name">托管设备</text>
            </view>
          </view>
        </view>

        <view class="card">
          <view class="section-header">
            <text class="section-title">收益趋势</text>
            <text class="section-sub">近 {{ days }} 天合计 {{ formatAmount(earnings.trendTotalMinor) }}</text>
          </view>
          <view v-if="earnings.trend.length" class="trend-chart">
            <view v-for="point in earnings.trend" :key="point.date" class="trend-col">
              <view class="bar-track">
                <view class="bar-fill" :style="{ height: barHeight(point.amountMinor) + '%' }"></view>
              </view>
              <text class="bar-label">{{ point.date.slice(5) }}</text>
            </view>
          </view>
          <EmptyState v-else text="暂无趋势数据" />
        </view>

        <view class="card">
          <view class="section-header">
            <text class="section-title">收益分类</text>
          </view>
          <view v-if="earnings.categories.length" class="category-list">
            <view v-for="item in earnings.categories" :key="item.type" class="category-row">
              <view class="category-left">
                <image class="category-icon" src="/static/icons/income.svg" mode="aspectFit" />
                <text class="category-name">{{ item.name }}</text>
              </view>
              <text class="category-amount">{{ formatAmount(item.amountMinor) }}</text>
            </view>
          </view>
          <EmptyState v-else text="暂无分类数据" />
        </view>

        <view class="card">
          <view class="section-header">
            <text class="section-title">收益明细</text>
            <text v-if="earnings.updatedAt" class="section-sub">更新于 {{ earnings.updatedAt.slice(0, 16) }}</text>
          </view>
          <view v-if="earnings.details.length" class="detail-list">
            <view v-for="item in earnings.details" :key="item.id" class="detail-row">
              <image v-if="item.assetImageUrl" class="asset-img" :src="item.assetImageUrl" mode="aspectFill" />
              <image v-else class="asset-img" src="/static/icons/device-placeholder.svg" mode="aspectFit" />
              <view class="detail-main">
                <view class="detail-title-row">
                  <text class="detail-title">{{ item.assetName || '未命名资产' }}</text>
                  <text class="detail-amount">{{ formatAmount(item.amountMinor) }}</text>
                </view>
                <view class="detail-meta-row">
                  <text class="detail-time">{{ item.createdAt?.slice(0, 16) }}</text>
                  <text class="status-tag" :class="item.status">{{ statusLabel(item.status) }}</text>
                </view>
              </view>
            </view>
          </view>
          <EmptyState v-else text="暂无收益明细" />
        </view>
      </template>
    </template>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import Skeleton from "../../components/PageSkeleton.vue";
import EmptyState from "../../components/EmptyState.vue";
import ErrorRetry from "../../components/ErrorRetry.vue";
import { getAssetEarnings, type AssetEarnings } from "../../services/asset";
import { formatAmount } from "../../utils/format";

const statusBarHeight = ref(0);
const systemInfo = uni.getWindowInfo();
statusBarHeight.value = systemInfo.statusBarHeight || 0;

const ranges = [
  { label: "7天", value: 7 },
  { label: "30天", value: 30 },
] as const;

const days = ref<7 | 30>(7);
const loading = ref(true);
const error = ref(false);
const emptyEarnings: AssetEarnings = {
  totalRevenueMinor: 0,
  pendingRevenueMinor: 0,
  withdrawableRevenueMinor: 0,
  monthRevenueMinor: 0,
  trendTotalMinor: 0,
  activeDeviceCount: 0,
  updatedAt: "",
  trend: [],
  categories: [],
  details: [],
};
const earnings = ref<AssetEarnings>(emptyEarnings);

const maxTrendAmount = computed(() => Math.max(0, ...earnings.value.trend.map((item) => item.amountMinor)));

onShow(() => loadEarnings());

async function loadEarnings() {
  loading.value = true;
  error.value = false;
  try {
    earnings.value = await getAssetEarnings(days.value);
  } catch {
    earnings.value = emptyEarnings;
    error.value = true;
  } finally {
    loading.value = false;
  }
}

function changeRange(value: 7 | 30) {
  if (days.value === value) return;
  days.value = value;
  loadEarnings();
}

function barHeight(amountMinor: number) {
  if (maxTrendAmount.value <= 0) return 8;
  return Math.max(8, Math.round((amountMinor / maxTrendAmount.value) * 100));
}

function statusLabel(status: string) {
  const map: Record<string, string> = {
    PENDING_SETTLE: "待结算",
    SETTLED: "已结算",
    DISPUTE: "争议中",
  };
  return map[status] || status;
}

function goBack() {
  uni.navigateBack({ delta: 1 });
}
</script>

<style scoped lang="scss">
.earnings-page { min-height: 100vh; background: #f5f6f8; padding-bottom: calc(40rpx + env(safe-area-inset-bottom)); }
.nav-bar { min-height: 88rpx; padding: 0 28rpx; display: flex; align-items: center; justify-content: space-between; background: #fff; border-bottom: 1rpx solid #eef2f7; }
.back-btn { width: 56rpx; height: 56rpx; border-radius: 50%; background: #f3f4f6; display: flex; align-items: center; justify-content: center; }
.back-icon { width: 28rpx; height: 28rpx; }
.nav-title { font-size: 32rpx; font-weight: 700; color: #111827; }
.nav-placeholder { width: 56rpx; }
.summary-card { margin: 28rpx; padding: 32rpx; border-radius: 24rpx; background: linear-gradient(135deg, #0a4bfe, #2563eb); color: #fff; box-shadow: 0 16rpx 32rpx rgba(10, 75, 254, 0.18); }
.summary-header { display: flex; justify-content: space-between; align-items: flex-start; gap: 20rpx; }
.summary-label { display: block; font-size: 24rpx; opacity: 0.78; }
.summary-value { display: block; margin-top: 10rpx; font-size: 52rpx; font-weight: 800; }
.range-switch { display: flex; padding: 4rpx; border-radius: 999rpx; background: rgba(255, 255, 255, 0.14); flex-shrink: 0; }
.range-item { padding: 10rpx 18rpx; border-radius: 999rpx; font-size: 22rpx; color: rgba(255, 255, 255, 0.76); }
.range-item.active { background: #fff; color: #0a4bfe; font-weight: 700; }
.summary-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14rpx; margin-top: 34rpx; }
.summary-item { min-width: 0; padding: 20rpx 14rpx; border-radius: 18rpx; background: rgba(255, 255, 255, 0.12); }
.summary-num { display: block; font-size: 26rpx; font-weight: 800; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.summary-name { display: block; margin-top: 8rpx; font-size: 20rpx; opacity: 0.72; }
.card { margin: 0 28rpx 24rpx; padding: 28rpx; border-radius: 22rpx; background: #fff; }
.section-header { display: flex; align-items: baseline; justify-content: space-between; gap: 18rpx; margin-bottom: 24rpx; }
.section-title { font-size: 30rpx; font-weight: 800; color: #111827; }
.section-sub { font-size: 22rpx; color: #9ca3af; }
.trend-chart { height: 260rpx; display: flex; align-items: stretch; gap: 10rpx; }
.trend-col { flex: 1; min-width: 0; display: flex; flex-direction: column; align-items: center; gap: 10rpx; }
.bar-track { flex: 1; width: 100%; display: flex; align-items: flex-end; border-radius: 14rpx; background: #f1f5f9; overflow: hidden; }
.bar-fill { width: 100%; border-radius: 14rpx 14rpx 0 0; background: linear-gradient(180deg, #22c55e, #16a34a); }
.bar-label { font-size: 18rpx; color: #94a3b8; }
.category-list, .detail-list { display: flex; flex-direction: column; }
.category-row { display: flex; align-items: center; justify-content: space-between; padding: 18rpx 0; border-bottom: 1rpx solid #f3f4f6; }
.category-row:last-child, .detail-row:last-child { border-bottom: none; }
.category-left { display: flex; align-items: center; gap: 14rpx; }
.category-icon { width: 44rpx; height: 44rpx; }
.category-name { font-size: 26rpx; color: #374151; }
.category-amount { font-size: 26rpx; font-weight: 800; color: #16a34a; }
.detail-row { display: flex; align-items: center; gap: 18rpx; padding: 20rpx 0; border-bottom: 1rpx solid #f3f4f6; }
.asset-img { width: 84rpx; height: 84rpx; border-radius: 16rpx; background: #f3f4f6; flex-shrink: 0; }
.detail-main { flex: 1; min-width: 0; }
.detail-title-row, .detail-meta-row { display: flex; align-items: center; justify-content: space-between; gap: 16rpx; }
.detail-title { font-size: 26rpx; font-weight: 700; color: #111827; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.detail-amount { font-size: 26rpx; font-weight: 800; color: #16a34a; flex-shrink: 0; }
.detail-meta-row { margin-top: 10rpx; }
.detail-time { font-size: 22rpx; color: #94a3b8; }
.status-tag { padding: 4rpx 12rpx; border-radius: 999rpx; font-size: 20rpx; background: #fef3c7; color: #92400e; flex-shrink: 0; }
.status-tag.SETTLED { background: #dcfce7; color: #15803d; }
.status-tag.DISPUTE { background: #fee2e2; color: #b91c1c; }
</style>
