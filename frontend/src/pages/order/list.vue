<template>
  <view class="page">
    <view class="tabs">
      <text v-for="tab in typeTabs" :key="tab.value" class="tab" :class="{ active: currentType === tab.value }" @tap="currentType = tab.value">{{ tab.label }}</text>
    </view>
    <scroll-view scroll-x class="status-tabs">
      <text v-for="s in statusTabs" :key="s.value" class="status-tab" :class="{ active: currentStatus === s.value }" @tap="currentStatus = s.value">{{ s.label }}</text>
    </scroll-view>

    <Skeleton v-if="loading && orders.length === 0" :rows="5" />
    <EmptyState v-else-if="!loading && orders.length === 0" text="暂无订单" />
    <scroll-view v-else scroll-y class="order-list" @scrolltolower="loadMore">
      <view v-for="order in orders" :key="order.id" class="order-item" @tap="goDetail(order.id)">
        <view class="order-item__header">
          <text class="order-no">{{ order.orderNo }}</text>
          <text class="order-status" :class="order.status">{{ statusLabel(order.status) }}</text>
        </view>
        <view class="order-item__body">
          <image class="order-img" :src="order.skuImage" mode="aspectFill" />
          <view class="order-info">
            <text class="order-title">{{ order.skuTitle }}</text>
            <text class="order-amount">{{ formatAmount(order.payableMinor) }}</text>
          </view>
        </view>
        <view v-if="order.allowedActions.length" class="order-item__actions">
          <view v-if="order.allowedActions.includes('CANCEL')" class="action-btn outline" @tap.stop="handleCancel(order.id)">取消订单</view>
          <view v-if="order.allowedActions.includes('PAY')" class="action-btn primary" @tap.stop="handlePay(order.id)">去支付</view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { onShow } from "@dcloudio/uni-app";
import Skeleton from "../../components/Skeleton.vue";
import EmptyState from "../../components/EmptyState.vue";
import { formatAmount } from "../../utils/format";
import { getOrderList, cancelOrder, type Order } from "../../services/order";
import { getSkuDetail } from "../../services/catalog";

type OrderCard = Order & { skuTitle: string; skuImage: string; allowedActions: string[] };

const typeTabs = [
  { label: "全部", value: "" },
  { label: "租赁", value: "RENT" },
  { label: "购买", value: "BUY" },
  { label: "软件", value: "SOFTWARE" },
];

const statusTabs = [
  { label: "全部", value: "" },
  { label: "待付款", value: "PENDING_PAY" },
  { label: "待履约", value: "PAID" },
  { label: "履约中", value: "IN_SERVICE" },
  { label: "已完成", value: "COMPLETED" },
  { label: "已取消", value: "CANCELLED" },
];

const currentType = ref("");
const currentStatus = ref("");
const orders = ref<OrderCard[]>([]);
const loading = ref(false);
const page = ref(1);
const hasMore = ref(true);

function statusLabel(status: string): string {
  const map: Record<string, string> = {
    PENDING_PAY: "待付款", PAID: "待履约", IN_SERVICE: "履约中",
    COMPLETED: "已完成", CANCELLED: "已取消", REFUNDING: "退款中",
  };
  return map[status] || status;
}

function actionsFor(status: string) {
  return status === "PENDING_PAY" ? ["CANCEL", "PAY"] : [];
}

async function fetchOrders(reset = false) {
  if (reset) { page.value = 1; hasMore.value = true; }
  if (!hasMore.value) return;
  loading.value = true;
  try {
    const res = await getOrderList({ type: currentType.value || undefined, status: currentStatus.value || undefined, page: page.value, pageSize: 20 });
    const cards = await Promise.all(res.items.map(async (order) => {
      try {
        const sku = await getSkuDetail(order.skuId);
        return { ...order, skuTitle: sku.title, skuImage: sku.image, allowedActions: actionsFor(order.status) };
      } catch {
        return { ...order, skuTitle: "商品信息不可用", skuImage: "", allowedActions: actionsFor(order.status) };
      }
    }));
    if (reset) { orders.value = cards; } else { orders.value.push(...cards); }
    hasMore.value = orders.value.length < res.total;
  } catch { /* handled by error state */ } finally { loading.value = false; }
}

function loadMore() { if (hasMore.value && !loading.value) { page.value++; fetchOrders(); } }

watch([currentType, currentStatus], () => fetchOrders(true));
onShow(() => fetchOrders(true));

function goDetail(id: string) { uni.navigateTo({ url: `/pages/order/detail?id=${id}` }); }

async function handleCancel(id: string) {
  const [err] = await uni.showModal({ title: "确认取消", content: "确定要取消该订单吗？" }) as unknown as [null, { confirm: boolean }];
  if (err) return;
  try { await cancelOrder(id); fetchOrders(true); uni.showToast({ title: "已取消", icon: "none" }); } catch (e: any) { uni.showToast({ title: e.message || "取消失败", icon: "none" }); }
}

function handlePay(id: string) { uni.navigateTo({ url: `/pages/order/detail?id=${id}&action=pay` }); }
</script>

<style scoped lang="scss">
.tabs { display: flex; padding: 24rpx 28rpx 0; gap: 32rpx; }
.tab { font-size: 28rpx; color: #6b7280; padding-bottom: 16rpx; }
.tab.active { color: #0a4bfe; font-weight: 700; border-bottom: 4rpx solid #0a4bfe; }
.status-tabs { white-space: nowrap; padding: 16rpx 28rpx; }
.status-tab { display: inline-block; font-size: 24rpx; color: #6b7280; padding: 8rpx 20rpx; margin-right: 12rpx; border-radius: 999rpx; background: #f3f4f6; }
.status-tab.active { background: #eef2ff; color: #0a4bfe; font-weight: 600; }
.order-list { height: calc(100vh - 200rpx); padding: 0 28rpx; }
.order-item { background: #fff; border-radius: 20rpx; padding: 28rpx; margin-bottom: 20rpx; }
.order-item__header { display: flex; justify-content: space-between; margin-bottom: 20rpx; }
.order-no { font-size: 24rpx; color: #6b7280; }
.order-status { font-size: 24rpx; font-weight: 600; }
.order-status.PENDING_PAY { color: #f59e0b; }
.order-status.PAID, .order-status.IN_SERVICE { color: #0a4bfe; }
.order-status.COMPLETED { color: #10b981; }
.order-status.CANCELLED { color: #9ca3af; }
.order-item__body { display: flex; gap: 20rpx; }
.order-img { width: 140rpx; height: 140rpx; border-radius: 12rpx; background: #f3f4f6; flex-shrink: 0; }
.order-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.order-title { font-size: 28rpx; font-weight: 600; color: #111827; }
.order-amount { font-size: 30rpx; font-weight: 700; color: #111827; }
.order-item__actions { display: flex; justify-content: flex-end; gap: 16rpx; margin-top: 20rpx; padding-top: 20rpx; border-top: 1rpx solid #f3f4f6; }
.action-btn { padding: 12rpx 28rpx; border-radius: 999rpx; font-size: 24rpx; }
.action-btn.outline { border: 2rpx solid #d1d5db; color: #6b7280; }
.action-btn.primary { background: #0a4bfe; color: #fff; }
</style>
