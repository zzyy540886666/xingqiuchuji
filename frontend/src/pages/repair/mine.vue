<template>
  <view class="page">
    <view class="tabs">
      <text v-for="tab in tabs" :key="tab.value" class="tab" :class="{ active: currentTab === tab.value }" @tap="currentTab = tab.value">{{ tab.label }}</text>
    </view>
    <Skeleton v-if="loading" :rows="4" />
    <EmptyState v-else-if="list.length === 0" text="暂无报修记录" action-text="去报修" @action="goCreate" />
    <view v-else class="order-list">
      <view v-for="item in list" :key="item.id" class="order-card" @tap="goProgress(item.id)">
        <view class="order-header">
          <text class="fault-type">{{ item.faultType }}</text>
          <text class="order-status" :class="item.status">{{ statusLabel(item.status) }}</text>
        </view>
        <text class="order-desc">{{ item.description }}</text>
        <text class="order-time">{{ item.createdAt }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { onShow } from "@dcloudio/uni-app";
import Skeleton from "../../components/Skeleton.vue";
import EmptyState from "../../components/EmptyState.vue";
import { getWorkOrders, type WorkOrder } from "../../services/repair";

const tabs = [
  { label: "全部", value: "" },
  { label: "处理中", value: "IN_PROGRESS" },
  { label: "已完成", value: "DONE" },
];
const currentTab = ref("");
const list = ref<WorkOrder[]>([]);
const loading = ref(false);

const statusMap: Record<string, string> = { NEW: "待处理", ASSIGNED: "已派单", IN_PROGRESS: "处理中", PENDING_ACCEPT: "待接单", DONE: "已完成", REJECTED: "已驳回", CLOSED: "已关闭" };
function statusLabel(s: string) { return statusMap[s] || s; }

async function fetchList() {
  loading.value = true;
  try { const res = await getWorkOrders({ status: currentTab.value || undefined }); list.value = res.items; } catch {} finally { loading.value = false; }
}

watch(currentTab, () => fetchList());
onShow(() => fetchList());

function goCreate() { uni.navigateTo({ url: "/pages/repair/create" }); }
function goProgress(id: string) { uni.navigateTo({ url: `/pages/repair/progress?id=${id}` }); }
</script>

<style scoped lang="scss">
.tabs { display: flex; padding: 24rpx 28rpx; gap: 20rpx; }
.tab { font-size: 26rpx; color: #6b7280; padding: 10rpx 24rpx; border-radius: 999rpx; background: #f3f4f6; }
.tab.active { background: #eef2ff; color: #0a4bfe; font-weight: 600; }
.order-list { padding: 0 28rpx; }
.order-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; }
.order-header { display: flex; justify-content: space-between; margin-bottom: 12rpx; }
.fault-type { font-size: 28rpx; font-weight: 600; }
.order-status { font-size: 22rpx; padding: 4rpx 12rpx; border-radius: 6rpx; }
.order-status.IN_PROGRESS { background: #dbeafe; color: #1d4ed8; }
.order-status.DONE { background: #dcfce7; color: #15803d; }
.order-status.NEW, .order-status.PENDING_ACCEPT { background: #fef3c7; color: #92400e; }
.order-desc { display: block; font-size: 24rpx; color: #6b7280; margin-bottom: 8rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.order-time { font-size: 22rpx; color: #9ca3af; }
</style>
