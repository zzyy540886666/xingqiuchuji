<template>
  <view class="page">
    <template v-if="loading">
      <Skeleton variant="workorder-process" />
    </template>
    <view v-else-if="order" class="process-page">
      <view class="card">
        <text class="section-title">工单信息</text>
        <view class="info-row">
          <text class="label">故障类型</text>
          <text class="value">{{ order.faultType }}</text>
        </view>
        <view class="info-row">
          <text class="label">描述</text>
          <text class="value">{{ order.description }}</text>
        </view>
        <view class="info-row">
          <text class="label">优先级</text>
          <text class="value">{{ order.priority }}</text>
        </view>
        <view class="info-row">
          <text class="label">状态</text>
          <text class="value">{{ order.status }}</text>
        </view>
      </view>

      <view v-if="order.status === 'PENDING_ACCEPT'" class="actions">
        <view class="action-btn danger" @tap="handleReject">拒单</view>
        <view class="action-btn primary" @tap="handleAccept">接单</view>
      </view>

      <view v-if="order.status === 'IN_PROGRESS'" class="card">
        <text class="section-title">完工提交</text>
        <view class="form-item">
          <text class="label">维修方案</text>
          <textarea
            v-model="solution"
            class="textarea"
            placeholder="请输入维修方案和处理结果"
          />
        </view>
        <view class="form-item">
          <text class="label">维修照片</text>
          <view class="image-list">
            <view v-for="(img, idx) in images" :key="idx" class="img-wrap">
              <image :src="img" mode="aspectFill" class="preview-img" />
            </view>
            <view class="img-add" @tap="chooseImage">
              <image class="img-add-icon" src="/static/icons/plus.svg" mode="aspectFit" />
            </view>
          </view>
        </view>
        <view class="action-btn primary" @tap="handleComplete">提交完工</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import Skeleton from "../../components/PageSkeleton.vue";
import {
  acceptWorkOrder,
  completeWorkOrder,
  getWorkOrderDetail,
  rejectWorkOrder,
  type WorkOrder,
} from "../../services/repair";

const order = ref<WorkOrder | null>(null);
const loading = ref(true);
const solution = ref("");
const images = ref<string[]>([]);
let orderId = "";

onLoad(async (options) => {
  orderId = options?.id || "";
  await fetchOrder();
});

async function fetchOrder() {
  loading.value = true;
  try {
    order.value = await getWorkOrderDetail(orderId);
  } catch {}
  finally {
    loading.value = false;
  }
}

async function handleAccept() {
  try {
    await acceptWorkOrder(orderId);
    uni.showToast({ title: "已接单", icon: "none" });
    await fetchOrder();
  } catch (e: any) {
    uni.showToast({ title: e.message || "操作失败", icon: "none" });
  }
}

async function handleReject() {
  const [, res] = await uni.showModal({
    title: "拒单原因",
    editable: true,
    placeholderText: "请输入拒单原因",
  }) as unknown as [null, { confirm: boolean; content: string }];

  if (!res?.confirm || !res.content) return;

  try {
    await rejectWorkOrder(orderId, res.content);
    uni.showToast({ title: "已拒单", icon: "none" });
    await fetchOrder();
  } catch (e: any) {
    uni.showToast({ title: e.message || "操作失败", icon: "none" });
  }
}

function chooseImage() {
  uni.chooseImage({
    count: 9 - images.value.length,
    success: (res) => {
      images.value.push(...res.tempFilePaths);
    },
  });
}

async function handleComplete() {
  if (!solution.value) {
    uni.showToast({ title: "请填写维修方案", icon: "none" });
    return;
  }

  try {
    await completeWorkOrder(orderId, {
      solution: solution.value,
      images: images.value,
    });
    uni.showToast({ title: "已提交完工", icon: "none" });
    await fetchOrder();
  } catch (e: any) {
    uni.showToast({ title: e.message || "提交失败", icon: "none" });
  }
}
</script>

<style scoped lang="scss">
.card {
  background: #fff;
  margin: 20rpx 28rpx;
  border-radius: 20rpx;
  padding: 28rpx;
}

.section-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  margin-bottom: 20rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #f9fafb;
}

.label {
  font-size: 26rpx;
  color: #6b7280;
}

.value {
  font-size: 26rpx;
  color: #111827;
}

.actions {
  display: flex;
  gap: 20rpx;
  padding: 28rpx;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 700;
}

.action-btn.primary {
  background: #0a4bfe;
  color: #fff;
}

.action-btn.danger {
  background: #fee2e2;
  color: #dc2626;
}

.form-item {
  margin-bottom: 24rpx;
}

.textarea {
  width: 100%;
  height: 160rpx;
  font-size: 26rpx;
  padding: 16rpx;
  background: #f9fafb;
  border-radius: 12rpx;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.img-wrap {
  width: 140rpx;
  height: 140rpx;
}

.preview-img {
  width: 100%;
  height: 100%;
  border-radius: 12rpx;
}

.img-add {
  width: 140rpx;
  height: 140rpx;
  border: 2rpx dashed #d1d5db;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
}

.img-add-icon {
  width: 42rpx;
  height: 42rpx;
  display: block;
  background: #94a3b8;
  border-radius: 50%;
  padding: 8rpx;
}
</style>
