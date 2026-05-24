<template>
  <view class="page">
    <Skeleton v-if="loading" :rows="5" />
    <view v-else-if="order" class="progress-page">
      <view class="status-card" :class="order.status">
        <text class="status-text">{{ statusLabel(order.status) }}</text>
        <text class="priority-badge">{{ order.priority }}</text>
      </view>
      <view class="card">
        <text class="section-title">报修信息</text>
        <view class="info-row"><text class="label">故障类型</text><text class="value">{{ order.faultType }}</text></view>
        <view class="info-row"><text class="label">描述</text><text class="value">{{ order.description }}</text></view>
        <view class="info-row"><text class="label">提交时间</text><text class="value">{{ order.createdAt }}</text></view>
        <view v-if="order.assigneeName" class="info-row"><text class="label">维修员</text><text class="value">{{ order.assigneeName }}</text></view>
      </view>
      <view v-if="order.images.length" class="card">
        <text class="section-title">现场照片</text>
        <view class="image-grid">
          <image v-for="(img, idx) in order.images" :key="idx" :src="img" mode="aspectFill" class="grid-img" @tap="previewImage(idx)" />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import Skeleton from "../../components/Skeleton.vue";
import { getWorkOrderDetail, type WorkOrder } from "../../services/repair";

const order = ref<WorkOrder | null>(null);
const loading = ref(true);

const statusMap: Record<string, string> = { NEW: "待处理", ASSIGNED: "已派单", IN_PROGRESS: "处理中", PENDING_ACCEPT: "待接单", DONE: "已完成", REJECTED: "已驳回", CLOSED: "已关闭" };
function statusLabel(s: string) { return statusMap[s] || s; }

onLoad(async (options) => {
  try { order.value = await getWorkOrderDetail(options?.id || ""); } catch {} finally { loading.value = false; }
});

function previewImage(idx: number) {
  if (!order.value) return;
  uni.previewImage({ urls: order.value.images, current: idx });
}
</script>

<style scoped lang="scss">
.status-card { padding: 36rpx 28rpx; color: #fff; display: flex; justify-content: space-between; align-items: center; }
.status-card.NEW, .status-card.PENDING_ACCEPT { background: #f59e0b; }
.status-card.ASSIGNED, .status-card.IN_PROGRESS { background: #0a4bfe; }
.status-card.DONE { background: #10b981; }
.status-card.REJECTED, .status-card.CLOSED { background: #6b7280; }
.status-text { font-size: 34rpx; font-weight: 700; }
.priority-badge { font-size: 22rpx; padding: 6rpx 16rpx; border-radius: 999rpx; background: rgba(255,255,255,0.2); }
.card { background: #fff; margin: 20rpx 28rpx; border-radius: 20rpx; padding: 28rpx; }
.section-title { display: block; font-size: 28rpx; font-weight: 700; margin-bottom: 20rpx; }
.info-row { display: flex; justify-content: space-between; padding: 12rpx 0; }
.label { font-size: 26rpx; color: #6b7280; }
.value { font-size: 26rpx; color: #111827; max-width: 60%; text-align: right; }
.image-grid { display: flex; flex-wrap: wrap; gap: 12rpx; }
.grid-img { width: 150rpx; height: 150rpx; border-radius: 12rpx; }
</style>
