<template>
  <view class="page">
    <Skeleton v-if="loading" :rows="6" />
    <view v-else-if="order" class="detail">
      <view class="status-banner" :class="order.status">
        <text class="status-text">{{ statusLabel(order.status) }}</text>
      </view>
      <view class="card">
        <view class="sku-row">
          <image class="sku-img" :src="skuImage" mode="aspectFill" />
          <view class="sku-info">
            <text class="sku-title">{{ skuTitle }}</text>
            <text class="sku-type">{{ typeLabel(order.orderType) }}</text>
          </view>
        </view>
      </view>
      <view class="card">
        <view class="info-row"><text class="label">订单编号</text><text class="value">{{ order.orderNo }}</text></view>
        <view class="info-row"><text class="label">下单时间</text><text class="value">{{ order.createdAt }}</text></view>
        <view class="info-row"><text class="label">应付金额</text><text class="value price">{{ formatAmount(order.payableMinor) }}</text></view>
        <view v-if="order.depositMinor" class="info-row"><text class="label">押金</text><text class="value">{{ formatAmount(order.depositMinor) }}</text></view>
      </view>
      <view v-if="allowedActions.length" class="actions">
        <view v-if="allowedActions.includes('CANCEL')" class="action-btn outline" @tap="handleCancel">取消订单</view>
        <view v-if="allowedActions.includes('PAY')" class="action-btn primary" @tap="handlePay">去支付</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import Skeleton from "../../components/Skeleton.vue";
import { formatAmount } from "../../utils/format";
import { getOrderDetail, cancelOrder, getWechatPayParams, type Order } from "../../services/order";
import { getSkuDetail } from "../../services/catalog";

const order = ref<Order | null>(null);
const loading = ref(true);
const skuTitle = ref("商品信息不可用");
const skuImage = ref("");
const allowedActions = computed(() => order.value?.status === "PENDING_PAY" ? ["CANCEL", "PAY"] : []);
let orderId = "";
let payOnReady = false;

function statusLabel(s: string) {
  const m: Record<string, string> = { PENDING_PAY: "待付款", PAID: "待履约", IN_SERVICE: "履约中", COMPLETED: "已完成", CANCELLED: "已取消" };
  return m[s] || s;
}
function typeLabel(t: string) {
  const m: Record<string, string> = { RENT: "租赁", BUY: "购买", SOFTWARE: "软件" };
  return m[t] || t;
}

onLoad((options) => {
  orderId = options?.id || "";
  payOnReady = options?.action === "pay";
  fetchDetail();
});

async function fetchDetail() {
  loading.value = true;
  try {
    order.value = await getOrderDetail(orderId);
    try {
      const sku = await getSkuDetail(order.value.skuId);
      skuTitle.value = sku.title;
      skuImage.value = sku.image;
    } catch {
      skuTitle.value = "商品信息不可用";
      skuImage.value = "";
    }
    if (payOnReady && allowedActions.value.includes("PAY")) {
      payOnReady = false;
      handlePay();
    }
  } catch {
    uni.showToast({ title: "加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

async function handleCancel() {
  uni.showModal({
    title: "确认取消",
    content: "确定要取消该订单吗？",
    success: async (result) => {
      if (!result.confirm) return;
      try { await cancelOrder(orderId); await fetchDetail(); uni.showToast({ title: "已取消", icon: "none" }); } catch (e: any) { uni.showToast({ title: e.message || "操作失败", icon: "none" }); }
    },
  });
}

async function handlePay() {
  try {
    const params = await getWechatPayParams(orderId);
    await uni.requestPayment({ provider: "wxpay", ...params });
    uni.redirectTo({ url: `/pages/order/pay-result?id=${orderId}&status=success` });
  } catch {
    uni.redirectTo({ url: `/pages/order/pay-result?id=${orderId}&status=fail` });
  }
}
</script>

<style scoped lang="scss">
.status-banner { padding: 40rpx 28rpx; color: #fff; font-size: 36rpx; font-weight: 700; }
.status-banner.PENDING_PAY { background: #f59e0b; }
.status-banner.PAID, .status-banner.IN_SERVICE { background: #0a4bfe; }
.status-banner.COMPLETED { background: #10b981; }
.status-banner.CANCELLED { background: #9ca3af; }
.card { background: #fff; margin: 20rpx 28rpx; border-radius: 20rpx; padding: 28rpx; }
.sku-row { display: flex; gap: 20rpx; }
.sku-img { width: 140rpx; height: 140rpx; border-radius: 12rpx; background: #f3f4f6; }
.sku-info { flex: 1; }
.sku-title { display: block; font-size: 28rpx; font-weight: 600; }
.sku-type { display: block; font-size: 24rpx; color: #6b7280; margin-top: 8rpx; }
.info-row { display: flex; justify-content: space-between; padding: 16rpx 0; border-bottom: 1rpx solid #f9fafb; }
.label { font-size: 26rpx; color: #6b7280; }
.value { font-size: 26rpx; color: #111827; }
.value.price { font-weight: 700; color: #ef4444; }
.actions { display: flex; justify-content: flex-end; gap: 16rpx; padding: 28rpx; }
.action-btn { padding: 16rpx 36rpx; border-radius: 999rpx; font-size: 26rpx; }
.action-btn.outline { border: 2rpx solid #d1d5db; color: #6b7280; }
.action-btn.primary { background: #0a4bfe; color: #fff; }
</style>
