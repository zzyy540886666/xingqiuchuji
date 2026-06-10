<template>
  <view class="page">
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
      <Skeleton variant="workorder-hall" />
    </template>
    <EmptyState v-else-if="list.length === 0" text="暂无工单" />

    <view v-else class="order-list">
      <view v-for="item in list" :key="item.id" class="order-card" @tap="goProcess(item.id)">
        <view class="order-header">
          <text class="fault-type">{{ item.faultType }}</text>
          <text class="priority" :class="item.priority">{{ item.priority }}</text>
        </view>
        <text class="order-desc">{{ item.description }}</text>
        <text v-if="item.location" class="order-location">{{ item.location }}</text>
        <text class="order-time">{{ item.createdAt }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { onShow } from "@dcloudio/uni-app";
import Skeleton from "../../components/PageSkeleton.vue";
import EmptyState from "../../components/EmptyState.vue";
import { getWorkOrders, type WorkOrder } from "../../services/repair";
import { navigateToPage } from "../../utils/navigation";
import { buildPageUrl } from "../../utils/query";

const tabs = [
  { label: "待接单", value: "PENDING_ACCEPT" },
  { label: "处理中", value: "IN_PROGRESS" },
  { label: "已完成", value: "DONE" },
  { label: "已驳回", value: "REJECTED" },
];

const currentTab = ref("PENDING_ACCEPT");
const list = ref<WorkOrder[]>([]);
const loading = ref(false);

async function fetchList() {
  loading.value = true;
  try {
    const res = await getWorkOrders({ status: currentTab.value });
    list.value = res.items;
  } catch {}
  finally {
    loading.value = false;
  }
}

watch(currentTab, () => fetchList());
onShow(() => fetchList());

function goProcess(id: string) {
  navigateToPage(buildPageUrl("/pages/workorder/process", { id }));
}
</script>

<style scoped lang="scss">
.tabs {
  display: flex;
  padding: 24rpx 28rpx;
  gap: 16rpx;
  flex-wrap: wrap;
}

.tab {
  font-size: 24rpx;
  color: #6b7280;
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  background: #f3f4f6;
}

.tab.active {
  background: #eef2ff;
  color: #0a4bfe;
  font-weight: 600;
}

.order-list {
  padding: 0 28rpx;
}

.order-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.fault-type {
  font-size: 28rpx;
  font-weight: 600;
}

.priority {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
}

.priority.HIGH,
.priority.URGENT {
  background: #fee2e2;
  color: #dc2626;
}

.priority.MEDIUM {
  background: #fef3c7;
  color: #92400e;
}

.priority.LOW {
  background: #f3f4f6;
  color: #6b7280;
}

.order-desc {
  display: block;
  font-size: 24rpx;
  color: #6b7280;
  margin-bottom: 6rpx;
}

.order-location {
  display: block;
  font-size: 22rpx;
  color: #9ca3af;
  margin-bottom: 6rpx;
}

.order-time {
  font-size: 22rpx;
  color: #9ca3af;
}
</style>
