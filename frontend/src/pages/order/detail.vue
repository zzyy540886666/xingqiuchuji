<template>
  <view class="page">
    <template v-if="loading">
      <Skeleton variant="order-detail" />
    </template>
    <view v-else-if="order" class="detail">
      <!-- 状态横幅 -->
      <view class="status-banner" :class="order.status">
        <text class="status-text">{{ statusLabel(order.status) }}</text>
        <text class="status-hint">{{ statusHint(order.status) }}</text>
      </view>

      <!-- 订单状态时间线 -->
      <view class="card timeline-card">
        <text class="card-title">订单进度</text>
        <view class="timeline">
          <view v-for="(step, idx) in timelineSteps" :key="idx" class="timeline-step" :class="{ active: step.active, current: step.current }">
            <view class="step-dot"></view>
            <view class="step-info">
              <text class="step-title">{{ step.title }}</text>
              <text v-if="step.time" class="step-time">{{ step.time }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 商品信息 -->
      <view class="card">
        <view class="sku-row">
          <image v-if="skuImage" class="sku-img" :src="skuImage" mode="aspectFit" />
          <view v-else class="sku-img placeholder"></view>
          <view class="sku-info">
            <text class="sku-title">{{ skuTitle }}</text>
            <text class="sku-type">{{ typeLabel(order.orderType) }}</text>
          </view>
        </view>
      </view>

      <!-- 订单信息 -->
      <view class="card">
        <view class="info-row"><text class="label">订单编号</text><text class="value">{{ order.orderNo }}</text></view>
        <view class="info-row"><text class="label">下单时间</text><text class="value">{{ order.createdAt }}</text></view>
        <view class="info-row"><text class="label">订单类型</text><text class="value">{{ typeLabel(order.orderType) }}</text></view>
        <view v-if="order.rentStartDate" class="info-row"><text class="label">租期</text><text class="value">{{ order.rentStartDate?.slice(0, 10) }} 至 {{ order.rentEndDate?.slice(0, 10) }}</text></view>
        <view class="info-row"><text class="label">应付金额</text><text class="value price">{{ formatAmount(order.payableMinor) }}</text></view>
        <view v-if="order.depositMinor" class="info-row"><text class="label">押金</text><text class="value">{{ formatAmount(order.depositMinor) }}</text></view>
        <view v-if="order.discountMinor" class="info-row"><text class="label">优惠</text><text class="value discount">-{{ formatAmount(order.discountMinor) }}</text></view>
        <view v-if="showContract" class="info-row contract-row">
          <text class="label">电子合同</text>
          <text class="contract-btn" @tap="handleDownloadContract">下载合同</text>
        </view>
      </view>

      <!-- 履约信息（按订单类型） -->
      <!-- 租赁：物流/续租 -->
      <view v-if="order.orderType === 'RENT' && order.status === 'FULFILLING'" class="card">
        <text class="card-title">履约信息</text>
        <view class="info-row"><text class="label">设备状态</text><text class="value status-active">履约中</text></view>
        <view v-if="order.rentEndDate" class="info-row"><text class="label">到期时间</text><text class="value">{{ order.rentEndDate.slice(0, 10) }}</text></view>
      </view>

      <!-- 购买：物流信息 -->
      <view v-if="order.orderType === 'BUY' && ['PAID', 'FULFILLING'].includes(order.status)" class="card">
        <text class="card-title">物流信息</text>
        <view class="empty-shipping">
          <text>物流信息待更新，请稍后查看</text>
        </view>
      </view>

      <!-- 软件：激活码/下载链接 -->
      <view v-if="order.orderType === 'SOFTWARE' && ['PAID', 'FULFILLING', 'COMPLETED'].includes(order.status)" class="card">
        <text class="card-title">软件交付</text>
        <view class="empty-shipping">
          <text>软件交付信息待生成，请稍后查看</text>
        </view>
      </view>

      <!-- 操作按钮区 -->
      <view v-if="actionButtons.length" class="actions">
        <view v-for="btn in actionButtons" :key="btn.key" class="action-btn" :class="btn.style" @tap="btn.handler">{{ btn.label }}</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import Skeleton from "../../components/PageSkeleton.vue";
import { formatAmount } from "../../utils/format";
import { getOrderDetail, cancelOrder, getWechatPayParams, getContractUrl, type Order } from "../../services/order";
import { getSkuDetail } from "../../services/catalog";
import { redirectToPage } from "../../utils/navigation";
import { buildPageUrl } from "../../utils/query";

interface ActionButton { key: string; label: string; style: string; handler: () => void }

const order = ref<Order | null>(null);
const loading = ref(true);
const skuTitle = ref("加载中...");
const skuImage = ref("");
let orderId = "";
let payOnReady = false;

const showContract = computed(() => order.value ? ["PAID", "FULFILLING", "COMPLETED"].includes(order.value.status) : false);

const statusTimeline: Record<string, number> = {
  PENDING_PAY: 0, PAID: 1, FULFILLING: 2, COMPLETED: 3, CANCELLED: -1,
  REFUNDING: 1, REFUNDED: 3,
};

const timelineSteps = computed(() => {
  if (!order.value) return [];
  const currentIdx = statusTimeline[order.value.status] ?? -1;
  const isCancelled = order.value.status === "CANCELLED";
  const steps = [
    { key: "created", title: "订单创建", time: order.value.createdAt },
    { key: "paid", title: "支付完成", time: isCancelled ? undefined : order.value.createdAt },
    { key: "service", title: "履约中", time: undefined },
    { key: "completed", title: "已完成", time: undefined },
  ];
  return steps.map((s, i) => ({
    ...s,
    active: isCancelled ? i === 0 : i <= currentIdx,
    current: i === currentIdx,
  }));
});

const actionButtons = computed(() => {
  if (!order.value) return [];
  const btns: ActionButton[] = [];
  const status = order.value.status;
  const type = order.value.orderType;

  if (status === "PENDING_PAY") {
    btns.push({ key: "cancel", label: "取消订单", style: "outline", handler: handleCancel });
    btns.push({ key: "pay", label: "去支付", style: "primary", handler: handlePay });
  }
  if (status === "FULFILLING" && type === "RENT") {
    btns.push({ key: "renew", label: "续租", style: "primary", handler: handleRenew });
  }
  if (["PAID", "FULFILLING"].includes(status)) {
    if (type === "BUY") {
      btns.push({ key: "confirm", label: "确认收货", style: "primary", handler: handleConfirmReceipt });
      btns.push({ key: "aftersale", label: "申请售后", style: "outline", handler: handleAfterSale });
    }
  }
  if (showContract.value) {
    btns.push({ key: "contract", label: "查看合同", style: "outline", handler: handleDownloadContract });
  }
  return btns;
});

function statusLabel(s: string) {
  const m: Record<string, string> = {
    PENDING_PAY: "待付款", PAID: "待履约", FULFILLING: "履约中",
    COMPLETED: "已完成", CANCELLED: "已取消", REFUNDING: "退款中", REFUNDED: "已退款",
  };
  return m[s] || s;
}

function statusHint(s: string) {
  const m: Record<string, string> = {
    PENDING_PAY: "请在有效时间内完成支付",
    PAID: "商家正在准备发货/履约",
    FULFILLING: "订单履约中，请等待商家更新进度",
    COMPLETED: "订单已完成，感谢您的使用",
    CANCELLED: "该订单已取消",
    REFUNDING: "退款处理中，请等待商家更新",
    REFUNDED: "该订单已退款",
  };
  return m[s] || "";
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
    if (payOnReady && actionButtons.value.some(b => b.key === "pay")) {
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
    redirectToPage(buildPageUrl("/pages/order/pay-result", { id: orderId, status: "success" }));
  } catch {
    redirectToPage(buildPageUrl("/pages/order/pay-result", { id: orderId, status: "fail" }));
  }
}

function handleRenew() {
  uni.showToast({ title: "续租功能暂未开放", icon: "none" });
}

function handleConfirmReceipt() {
  uni.showToast({ title: "确认收货功能暂未开放", icon: "none" });
}

function handleAfterSale() {
  uni.showToast({ title: "售后申请功能暂未开放", icon: "none" });
}

async function handleDownloadContract() {
  try {
    const { url } = await getContractUrl(orderId);
    uni.downloadFile({
      url,
      success(res) {
        if (res.statusCode === 200) {
          uni.openDocument({ filePath: res.tempFilePath, showMenu: true });
        }
      },
      fail() {
        uni.showToast({ title: "下载失败，请重试", icon: "none" });
      },
    });
  } catch (e: any) {
    if (e.message?.includes("404")) {
      uni.showToast({ title: "合同生成中，请稍后查看", icon: "none" });
    } else {
      uni.showToast({ title: e.message || "获取合同失败", icon: "none" });
    }
  }
}

</script>

<style scoped lang="scss">
.status-banner {
  padding: 48rpx 28rpx 28rpx;
  color: #fff;
}
.status-banner.PENDING_PAY { background: linear-gradient(135deg, #f59e0b, #f97316); }
.status-banner.PAID, .status-banner.FULFILLING { background: linear-gradient(135deg, #0a4bfe, #3b82f6); }
.status-banner.COMPLETED { background: linear-gradient(135deg, #10b981, #059669); }
.status-banner.CANCELLED { background: linear-gradient(135deg, #9ca3af, #6b7280); }
.status-text { font-size: 40rpx; font-weight: 800; display: block; }
.status-hint { font-size: 24rpx; opacity: 0.85; display: block; margin-top: 8rpx; }

.card { background: #fff; margin: 20rpx 28rpx; border-radius: 20rpx; padding: 28rpx; }
.card-title { font-size: 28rpx; font-weight: 700; display: block; margin-bottom: 20rpx; color: #111827; }

.timeline { padding-left: 16rpx; }
.timeline-step { display: flex; gap: 16rpx; padding-bottom: 24rpx; position: relative; }
.timeline-step::after { content: ""; position: absolute; left: 5rpx; top: 18rpx; bottom: 0; width: 2rpx; background: #e5e7eb; }
.timeline-step:last-child::after { display: none; }
.timeline-step.active::after { background: #0a4bfe; }
.step-dot { width: 12rpx; height: 12rpx; border-radius: 50%; background: #e5e7eb; margin-top: 6rpx; flex-shrink: 0; }
.timeline-step.active .step-dot { background: #0a4bfe; }
.timeline-step.current .step-dot { background: #0a4bfe; box-shadow: 0 0 0 6rpx rgba(10, 75, 254, 0.2); }
.step-info { flex: 1; }
.step-title { font-size: 26rpx; color: #9ca3af; display: block; }
.timeline-step.active .step-title { color: #374151; font-weight: 600; }
.timeline-step.current .step-title { color: #111827; font-weight: 700; }
.step-time { font-size: 22rpx; color: #9ca3af; display: block; margin-top: 4rpx; }

.sku-row { display: flex; gap: 20rpx; }
.sku-img { width: 140rpx; height: 140rpx; border-radius: 12rpx; background: #f3f4f6; flex-shrink: 0; }
.sku-img.placeholder { background: linear-gradient(135deg, #e5e7eb, #f3f4f6); }
.sku-info { flex: 1; display: flex; flex-direction: column; justify-content: center; }
.sku-title { display: block; font-size: 28rpx; font-weight: 600; color: #111827; }
.sku-type { display: block; font-size: 24rpx; color: #6b7280; margin-top: 8rpx; }

.info-row { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 0; border-bottom: 1rpx solid #f9fafb; }
.info-row:last-child { border-bottom: none; }
.label { font-size: 26rpx; color: #6b7280; }
.value { font-size: 26rpx; color: #111827; text-align: right; max-width: 50%; }
.value.price { font-weight: 700; color: #ef4444; font-size: 30rpx; }
.value.discount { color: #10b981; }
.value.status-active { color: #10b981; font-weight: 600; }

.contract-btn { font-size: 26rpx; color: #0a4bfe; font-weight: 600; }

.code-text { color: #0a4bfe; font-size: 22rpx; word-break: break-all; }
.link-text { color: #0a4bfe; font-weight: 600; }

.ship-info, .delivery-info { margin-top: 0; }
.empty-shipping { padding: 28rpx 0; text-align: center; font-size: 24rpx; color: #9ca3af; }

.actions { display: flex; flex-wrap: wrap; justify-content: flex-end; gap: 16rpx; padding: 28rpx; padding-bottom: env(safe-area-inset-bottom, 28rpx); }
.action-btn { padding: 18rpx 36rpx; border-radius: 999rpx; font-size: 26rpx; font-weight: 600; }
.action-btn.outline { border: 2rpx solid #d1d5db; color: #6b7280; }
.action-btn.primary { background: #0a4bfe; color: #fff; }
</style>
