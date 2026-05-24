<template>
  <view class="page">
    <Skeleton v-if="loading" :rows="4" />
    <ErrorRetry v-else-if="error" @retry="loadAssets" />
    <EmptyState v-else-if="assets.length === 0" text="暂无资产" action-text="去购买" @action="goBuy" />
    <view v-else class="asset-list">
      <view v-for="item in assets" :key="item.id" class="asset-card" @tap="goDetail(item.id)">
        <image v-if="item.imageUrl" class="asset-img" :src="item.imageUrl" mode="aspectFill" />
        <view v-else class="asset-img placeholder"></view>
        <view class="asset-info">
          <text class="asset-name">{{ item.name }}</text>
          <text class="asset-model">{{ item.modelInfo }}</text>
          <view class="asset-meta">
            <text class="asset-status">{{ item.status }}</text>
            <text class="asset-earnings">累计收益 {{ formatAmount(item.totalRevenueMinor) }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import Skeleton from "../../components/Skeleton.vue";
import EmptyState from "../../components/EmptyState.vue";
import ErrorRetry from "../../components/ErrorRetry.vue";
import { formatAmount } from "../../utils/format";
import { getAssets, getAssetDashboard, type Asset } from "../../services/asset";

type AssetCard = Asset & { totalRevenueMinor: number };
const assets = ref<AssetCard[]>([]);
const loading = ref(true);
const error = ref(false);

async function loadAssets() {
  loading.value = true;
  error.value = false;
  try {
    const res = await getAssets();
    assets.value = await Promise.all(res.map(async (asset) => {
      const dashboard = await getAssetDashboard(asset.id);
      return { ...asset, totalRevenueMinor: dashboard.totalRevenueMinor };
    }));
  } catch { error.value = true; } finally { loading.value = false; }
}
onShow(() => loadAssets());

function goDetail(id: string) { uni.navigateTo({ url: `/pages/asset/detail?id=${id}` }); }
function goBuy() { uni.navigateTo({ url: "/pages/category/index" }); }
</script>

<style scoped lang="scss">
.asset-list { padding: 28rpx; }
.asset-card { display: flex; gap: 20rpx; background: #fff; border-radius: 20rpx; padding: 24rpx; margin-bottom: 20rpx; }
.asset-img { width: 140rpx; height: 140rpx; border-radius: 16rpx; background: #f3f4f6; flex-shrink: 0; }
.asset-img.placeholder { background: linear-gradient(135deg, #e5e7eb, #f3f4f6); }
.asset-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.asset-name { font-size: 28rpx; font-weight: 700; color: #111827; }
.asset-model { font-size: 24rpx; color: #6b7280; }
.asset-meta { display: flex; justify-content: space-between; }
.asset-status { font-size: 22rpx; color: #10b981; }
.asset-earnings { font-size: 22rpx; color: #f59e0b; }
</style>
