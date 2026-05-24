<template>
  <view class="page">
    <Skeleton v-if="loading" :rows="5" />
    <view v-else-if="dashboard" class="detail">
      <view class="dashboard-card">
        <view class="stat"><text class="stat-value">{{ dashboard.deviceStatus }}</text><text class="stat-label">设备状态</text></view>
        <view class="stat"><text class="stat-value">{{ formatAmount(dashboard.totalRevenueMinor) }}</text><text class="stat-label">累计收益</text></view>
        <view class="stat"><text class="stat-value">{{ formatAmount(dashboard.monthRevenueMinor) }}</text><text class="stat-label">本月收益</text></view>
      </view>
      <view class="action-row">
        <view class="action-btn primary" @tap="goTrusteeship">托管上架</view>
      </view>
      <view class="section">
        <text class="section-title">收益记录</text>
        <EmptyState v-if="dashboard.revenueRecords.length === 0" text="暂无收益记录" />
        <view v-else class="records">
          <view v-for="r in dashboard.revenueRecords" :key="r.id" class="record-item">
            <text class="record-name">{{ r.status === 'SETTLED' ? '已结算' : '待结算' }}</text>
            <text class="record-time">{{ r.createdAt }}</text>
            <text class="record-amount">{{ formatAmount(r.amountMinor) }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import Skeleton from "../../components/Skeleton.vue";
import EmptyState from "../../components/EmptyState.vue";
import { formatAmount } from "../../utils/format";
import { getAssetDashboard, type AssetDashboard } from "../../services/asset";

const dashboard = ref<AssetDashboard | null>(null);
const loading = ref(true);
let assetId = "";

onLoad(async (options) => {
  assetId = options?.id || "";
  try { dashboard.value = await getAssetDashboard(assetId); } catch {} finally { loading.value = false; }
});

function goTrusteeship() { uni.navigateTo({ url: `/pages/asset/trusteeship?id=${assetId}` }); }
</script>

<style scoped lang="scss">
.dashboard-card { display: flex; justify-content: space-around; background: #fff; margin: 28rpx; border-radius: 20rpx; padding: 36rpx; }
.stat { text-align: center; }
.stat-value { display: block; font-size: 32rpx; font-weight: 800; color: #111827; }
.stat-label { display: block; font-size: 22rpx; color: #6b7280; margin-top: 8rpx; }
.action-row { padding: 0 28rpx; margin-bottom: 28rpx; }
.action-btn { height: 80rpx; border-radius: 999rpx; display: flex; align-items: center; justify-content: center; font-size: 28rpx; font-weight: 700; }
.action-btn.primary { background: #0a4bfe; color: #fff; }
.section { background: #fff; margin: 0 28rpx; border-radius: 20rpx; padding: 28rpx; }
.section-title { font-size: 28rpx; font-weight: 700; display: block; margin-bottom: 20rpx; }
.record-item { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 0; border-bottom: 1rpx solid #f9fafb; }
.record-name { font-size: 26rpx; color: #111827; }
.record-time { font-size: 22rpx; color: #9ca3af; }
.record-amount { font-size: 26rpx; font-weight: 600; color: #10b981; }
</style>
