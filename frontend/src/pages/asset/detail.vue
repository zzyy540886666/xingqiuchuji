<template>
  <view class="page">
    <!-- 自定义导航栏 -->
    <view class="nav-bar">
      <view class="back-btn" @tap="goBack"><image class="back-icon" src="/static/icons/back-light.svg" mode="aspectFit" /></view>
      <text class="nav-title">设备详情</text>
      <view class="nav-placeholder"></view>
    </view>

    <template v-if="loading">
      <Skeleton variant="asset-detail" />
    </template>
    <ErrorRetry v-else-if="error" @retry="loadDetail" />
    <view v-else-if="asset" class="detail">
      <!-- 资产基础信息 -->
      <view class="asset-header-card">
        <image v-if="asset.imageUrl" class="asset-img" :src="asset.imageUrl" mode="aspectFill" />
        <view v-else class="asset-img placeholder"></view>
        <view class="asset-summary">
          <text class="asset-name">{{ asset.name }}</text>
          <text class="asset-model">{{ asset.modelInfo }}</text>
          <text class="asset-status" :class="asset.status">{{ statusLabel(asset.status) }}</text>
        </view>
      </view>

      <!-- 收益概览 -->
      <view class="dashboard-card">
        <view class="stat">
          <text class="stat-value">{{ formatAmount(dashboard?.totalRevenueMinor || 0) }}</text>
          <text class="stat-label">累计收益</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat">
          <text class="stat-value">{{ formatAmount(dashboard?.monthRevenueMinor || 0) }}</text>
          <text class="stat-label">本月收益</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat">
          <text class="stat-value">{{ activeSlots.length }}</text>
          <text class="stat-label">活跃时段</text>
        </view>
      </view>

      <!-- 操作区 -->
      <view class="action-row">
        <view v-if="asset.status === 'IDLE'" class="action-btn primary" @tap="goTrusteeship">托管上架</view>
        <view v-if="asset.status === 'TRUSTEED'" class="action-btn outline" @tap="goEarnings">查看托管详情</view>
      </view>

      <!-- 活跃托管时段 -->
      <view v-if="activeSlots.length > 0" class="section card">
        <text class="section-title">活跃托管时段</text>
        <view v-for="slot in activeSlots" :key="slot.id" class="slot-card">
          <view class="slot-row">
            <text class="slot-label">时段</text>
            <text class="slot-value">{{ slot.startTime?.slice(0, 16) }} ~ {{ slot.endTime?.slice(0, 16) }}</text>
          </view>
          <view class="slot-row">
            <text class="slot-label">日收益</text>
            <text class="slot-value income">{{ formatAmount(slot.dailyRateMinor) }}</text>
          </view>
          <view class="slot-row">
            <text class="slot-label">状态</text>
            <text class="slot-status active">活跃</text>
          </view>
        </view>
      </view>

      <!-- 收益记录 -->
      <view class="section card">
        <view class="section-title-row">
          <text class="section-title">收益记录</text>
          <text v-if="currentMonthFilter" class="filter-reset" @tap="currentMonthFilter = ''">清除筛选</text>
        </view>
        <view class="month-filters">
          <text v-for="m in recentMonths" :key="m.value" class="month-tag" :class="{ active: currentMonthFilter === m.value }" @tap="currentMonthFilter = m.value">{{ m.label }}</text>
        </view>
        <EmptyState v-if="filteredRecords.length === 0" text="暂无收益记录" />
        <view v-else class="records">
          <view v-for="r in filteredRecords" :key="r.id" class="record-item">
            <view class="record-left">
              <text class="record-name">{{ r.status === 'SETTLED' ? '已结算' : '待结算' }}</text>
              <text class="record-time">{{ r.createdAt?.slice(0, 16) }}</text>
            </view>
            <text class="record-amount" :class="r.status === 'SETTLED' ? 'settled' : 'pending'">{{ formatAmount(r.amountMinor) }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import Skeleton from "../../components/PageSkeleton.vue";
import EmptyState from "../../components/EmptyState.vue";
import ErrorRetry from "../../components/ErrorRetry.vue";
import { formatAmount } from "../../utils/format";
import { getAssetDetail, getAssetDashboard } from "../../services/asset";
import type { Asset, AssetDashboard } from "../../services/asset";
import { navigateToPage } from "../../utils/navigation";
import { buildPageUrl } from "../../utils/query";

interface SlotRecord {
  id: string;
  startTime: string;
  endTime: string;
  dailyRateMinor: number;
  status: string;
}

const asset = ref<Asset | null>(null);
const dashboard = ref<AssetDashboard | null>(null);
const activeSlots = ref<SlotRecord[]>([]);
const loading = ref(true);
const error = ref(false);
const currentMonthFilter = ref("");
let assetId = "";

const recentMonths = computed(() => {
  const now = new Date();
  const months: { label: string; value: string }[] = [];
  for (let i = 0; i < 6; i++) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
    const value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`;
    const label = `${d.getFullYear()}年${d.getMonth() + 1}月`;
    months.push({ label, value });
  }
  return months;
});

const filteredRecords = computed(() => {
  if (!dashboard.value?.revenueRecords) return [];
  if (!currentMonthFilter.value) return dashboard.value.revenueRecords;
  return dashboard.value.revenueRecords.filter((r) => r.createdAt?.startsWith(currentMonthFilter.value));
});

function statusLabel(s: string): string {
  const m: Record<string, string> = { IDLE: "空闲", TRUSTEED: "托管中", MAINTENANCE: "维护中" };
  return m[s] || s;
}

onLoad((options) => {
  assetId = options?.id || "";
  loadDetail();
});

async function loadDetail() {
  if (!assetId) return;
  loading.value = true;
  error.value = false;
  try {
    const [detailData, dashboardData] = await Promise.all([
      getAssetDetail(assetId),
      getAssetDashboard(assetId),
    ]);
    asset.value = detailData.asset;
    activeSlots.value = detailData.activeSlots || [];
    dashboard.value = dashboardData;
  } catch {
    error.value = true;
  } finally {
    loading.value = false;
  }
}

function goTrusteeship() {
  if (!assetId) return;
  navigateToPage(buildPageUrl("/pages/asset/trusteeship", { id: assetId }));
}
function goEarnings() { uni.navigateTo({ url: "/pages/asset/earnings" }); }
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

.asset-header-card {
  display: flex;
  gap: 24rpx;
  background: #fff;
  margin: 28rpx;
  border-radius: 20rpx;
  padding: 32rpx;
  align-items: center;
}
.asset-img { width: 160rpx; height: 160rpx; border-radius: 20rpx; background: #f3f4f6; flex-shrink: 0; }
.asset-img.placeholder { background: linear-gradient(135deg, #e5e7eb, #f3f4f6); }
.asset-summary { flex: 1; display: flex; flex-direction: column; gap: 8rpx; }
.asset-name { font-size: 32rpx; font-weight: 800; color: #111827; }
.asset-model { font-size: 24rpx; color: #6b7280; }
.asset-status { font-size: 22rpx; padding: 4rpx 12rpx; border-radius: 999rpx; font-weight: 600; align-self: flex-start; }
.asset-status.IDLE { background: #dbeafe; color: #1d4ed8; }
.asset-status.TRUSTEED { background: #dcfce7; color: #15803d; }
.asset-status.MAINTENANCE { background: #fef3c7; color: #92400e; }

.dashboard-card {
  display: flex;
  justify-content: space-around;
  align-items: center;
  background: #fff;
  margin: 0 28rpx;
  border-radius: 20rpx;
  padding: 36rpx;
}
.stat { text-align: center; flex: 1; }
.stat-value { display: block; font-size: 32rpx; font-weight: 800; color: #111827; }
.stat-label { display: block; font-size: 22rpx; color: #6b7280; margin-top: 8rpx; }
.stat-divider { width: 2rpx; height: 48rpx; background: #f3f4f6; }

.action-row { padding: 0 28rpx; margin: 24rpx 0; }
.action-btn { height: 80rpx; border-radius: 999rpx; display: flex; align-items: center; justify-content: center; font-size: 28rpx; font-weight: 700; }
.action-btn.primary { background: #0a4bfe; color: #fff; }
.action-btn.outline { border: 2rpx solid #0a4bfe; color: #0a4bfe; }

.card { background: #fff; margin: 0 28rpx 28rpx; border-radius: 20rpx; padding: 28rpx; }
.section-title { font-size: 28rpx; font-weight: 700; display: block; margin-bottom: 20rpx; }
.section-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.section-title-row .section-title { margin-bottom: 0; }
.filter-reset { font-size: 24rpx; color: #0a4bfe; }

.month-filters { display: flex; gap: 12rpx; margin-bottom: 20rpx; flex-wrap: wrap; }
.month-tag { font-size: 22rpx; color: #6b7280; padding: 8rpx 16rpx; border-radius: 999rpx; background: #f3f4f6; }
.month-tag.active { background: #eef2ff; color: #0a4bfe; font-weight: 600; }

.slot-card { border: 1rpx solid #f3f4f6; border-radius: 12rpx; padding: 16rpx; margin-bottom: 12rpx; }
.slot-row { display: flex; justify-content: space-between; padding: 6rpx 0; }
.slot-label { font-size: 22rpx; color: #9ca3af; }
.slot-value { font-size: 22rpx; color: #374151; }
.slot-value.income { color: #10b981; font-weight: 600; }
.slot-status.active { font-size: 22rpx; color: #10b981; font-weight: 600; }

.record-item { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 0; border-bottom: 1rpx solid #f9fafb; }
.record-left { flex: 1; }
.record-name { font-size: 26rpx; color: #111827; display: block; }
.record-time { font-size: 22rpx; color: #9ca3af; display: block; margin-top: 4rpx; }
.record-amount { font-size: 26rpx; font-weight: 600; }
.record-amount.settled { color: #10b981; }
.record-amount.pending { color: #f59e0b; }
</style>
