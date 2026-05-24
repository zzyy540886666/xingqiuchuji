<template>
  <view class="page">
    <view class="tabs">
      <text v-for="tab in tabs" :key="tab.value" class="tab" :class="{ active: currentTab === tab.value }" @tap="currentTab = tab.value">{{ tab.label }}</text>
    </view>
    <Skeleton v-if="loading" :rows="4" />
    <EmptyState v-else-if="list.length === 0" text="暂无巡检任务" />
    <view v-else class="task-list">
      <view v-for="item in list" :key="item.id" class="task-card">
        <view class="task-header">
          <text class="task-name">{{ item.planName }}</text>
          <text class="task-status" :class="item.status">{{ statusLabel(item.status) }}</text>
        </view>
        <text class="task-location">{{ item.locationName }}</text>
        <text class="task-due">截止：{{ item.dueAt }}</text>
        <view v-if="item.status === 'PENDING'" class="task-action" @tap="handleStart(item.id)">开始巡检</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { onShow } from "@dcloudio/uni-app";
import Skeleton from "../../components/Skeleton.vue";
import EmptyState from "../../components/EmptyState.vue";
import { getInspectionTasks, startInspection, type InspectionTask } from "../../services/repair";

const tabs = [
  { label: "待巡检", value: "PENDING" },
  { label: "巡检中", value: "IN_PROGRESS" },
  { label: "已完成", value: "COMPLETED" },
  { label: "逾期", value: "OVERDUE" },
];
const currentTab = ref("PENDING");
const list = ref<InspectionTask[]>([]);
const loading = ref(false);

const statusMap: Record<string, string> = { PENDING: "待巡检", IN_PROGRESS: "巡检中", COMPLETED: "已完成", OVERDUE: "逾期" };
function statusLabel(s: string) { return statusMap[s] || s; }

async function fetchList() {
  loading.value = true;
  try { const res = await getInspectionTasks({ status: currentTab.value }); list.value = res.items; } catch {} finally { loading.value = false; }
}

watch(currentTab, () => fetchList());
onShow(() => fetchList());

async function handleStart(id: string) {
  try { await startInspection(id); uni.showToast({ title: "已开始巡检", icon: "none" }); fetchList(); } catch (e: any) { uni.showToast({ title: e.message || "操作失败", icon: "none" }); }
}
</script>

<style scoped lang="scss">
.tabs { display: flex; padding: 24rpx 28rpx; gap: 16rpx; flex-wrap: wrap; }
.tab { font-size: 24rpx; color: #6b7280; padding: 10rpx 20rpx; border-radius: 999rpx; background: #f3f4f6; }
.tab.active { background: #eef2ff; color: #0a4bfe; font-weight: 600; }
.task-list { padding: 0 28rpx; }
.task-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; }
.task-header { display: flex; justify-content: space-between; margin-bottom: 10rpx; }
.task-name { font-size: 28rpx; font-weight: 600; }
.task-status { font-size: 22rpx; padding: 4rpx 12rpx; border-radius: 6rpx; }
.task-status.PENDING { background: #fef3c7; color: #92400e; }
.task-status.IN_PROGRESS { background: #dbeafe; color: #1d4ed8; }
.task-status.COMPLETED { background: #dcfce7; color: #15803d; }
.task-status.OVERDUE { background: #fee2e2; color: #dc2626; }
.task-location { display: block; font-size: 24rpx; color: #6b7280; margin-bottom: 6rpx; }
.task-due { display: block; font-size: 22rpx; color: #9ca3af; }
.task-action { margin-top: 16rpx; padding: 12rpx 24rpx; background: #0a4bfe; color: #fff; border-radius: 999rpx; font-size: 24rpx; text-align: center; width: fit-content; }
</style>
