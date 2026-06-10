<template>
  <view class="page">
    <view class="nav-bar">
      <view class="back-btn" @tap="goBack">
        <image class="back-icon" src="/static/icons/back-light.svg" mode="aspectFit" />
      </view>
      <text class="nav-title">佣金明细</text>
      <view class="nav-placeholder"></view>
    </view>

    <view class="tabs">
      <text
        v-for="tab in tabs"
        :key="tab.value"
        class="tab"
        :class="{ active: currentTab === tab.value }"
        @tap="currentTab = tab.value"
      >
        {{ tab.label }}
      </text>
    </view>

    <template v-if="loading">
      <Skeleton variant="distribution-commission" />
    </template>
    <ErrorRetry v-else-if="error" @retry="fetchList" />
    <EmptyState v-else-if="list.length === 0" text="暂无佣金记录" />

    <view v-else class="commission-list">
      <view v-for="item in list" :key="item.id" class="commission-item">
        <view class="commission-left">
          <text class="commission-order">订单 #{{ item.orderId }}</text>
          <text class="commission-time">{{ item.createdAt?.slice(0, 16) }}</text>
        </view>
        <text class="commission-amount">+{{ formatAmount(item.amountMinor) }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { onShow } from "@dcloudio/uni-app";
import Skeleton from "../../components/PageSkeleton.vue";
import EmptyState from "../../components/EmptyState.vue";
import ErrorRetry from "../../components/ErrorRetry.vue";
import { formatAmount } from "../../utils/format";
import { getCommissions, type Commission } from "../../services/distribution";

const tabs = [
  { label: "保护期", value: "PENDING_PROTECT" },
  { label: "可结算", value: "SETTLEABLE" },
  { label: "已结算", value: "SETTLED" },
];

const currentTab = ref("PENDING_PROTECT");
const list = ref<Commission[]>([]);
const loading = ref(false);
const error = ref(false);

async function fetchList() {
  loading.value = true;
  error.value = false;
  try {
    const res = await getCommissions({ status: currentTab.value });
    list.value = res.items;
  } catch {
    list.value = [];
    error.value = true;
  }
  finally {
    loading.value = false;
  }
}

watch(currentTab, () => fetchList());
onShow(() => fetchList());

function goBack() {
  uni.navigateBack({ delta: 1 });
}
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
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  width: 28rpx;
  height: 28rpx;
  display: block;
}

.nav-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
}

.nav-placeholder {
  width: 56rpx;
}

.tabs {
  display: flex;
  padding: 24rpx 28rpx;
  gap: 24rpx;
}

.tab {
  font-size: 26rpx;
  color: #6b7280;
  padding: 10rpx 24rpx;
  border-radius: 999rpx;
  background: #f3f4f6;
}

.tab.active {
  background: #eef2ff;
  color: #0a4bfe;
  font-weight: 600;
}

.commission-list {
  padding: 0 28rpx;
}

.commission-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.commission-left {
  flex: 1;
}

.commission-order {
  display: block;
  font-size: 26rpx;
  color: #111827;
}

.commission-time {
  display: block;
  font-size: 22rpx;
  color: #9ca3af;
  margin-top: 6rpx;
}

.commission-amount {
  font-size: 30rpx;
  font-weight: 700;
  color: #10b981;
}
</style>
