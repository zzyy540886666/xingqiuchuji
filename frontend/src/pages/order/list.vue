<template>
  <view class="page">
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" @tap="goBack">
          <image class="back-icon" src="/static/icons/back.svg" mode="aspectFit" />
        </view>
        <text class="nav-title">订单中心</text>
        <view class="nav-placeholder" style="width: 28px;"></view>
      </view>
    </view>

    <view class="top-section">
      <!-- 订单类型 Tabs -->
      <view class="type-tabs">
        <view
          v-for="tab in typeTabs"
          :key="tab.value"
          class="type-tab"
          :class="{ active: currentType === tab.value }"
          @tap="currentType = tab.value"
        >
          <text class="type-label">{{ tab.label }}</text>
          <text class="type-sub">{{ tab.subLabel }}</text>
        </view>
      </view>

      <!-- 订单状态 Tabs 和筛选 -->
      <view class="status-filter-bar">
        <scroll-view scroll-x class="status-tabs" :show-scrollbar="false">
          <view class="status-tabs-inner">
            <view
              v-for="s in statusTabs"
              :key="s.value"
              class="status-tab"
              :class="{ active: currentStatus === s.value }"
              @tap="currentStatus = s.value"
            >
              <text>{{ s.label }}</text>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>

    <Skeleton v-if="loading && orders.length === 0" variant="order-list" />
    <ErrorRetry v-else-if="error && orders.length === 0" @retry="refreshOrders" />
    <EmptyState
      v-else-if="orders.length === 0"
      text="暂无符合条件的订单"
      action-text="去逛逛"
      @action="goCategory"
    />

    <!-- 订单列表 -->
    <scroll-view
      v-else
      scroll-y
      class="order-list"
      enable-back-to-top
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="refreshOrders"
      @scrolltolower="loadMore"
    >
      <view v-for="order in orders" :key="order.id" class="order-card" @tap="goDetail(order.id)">
        <!-- 订单头部 -->
        <view class="card-header">
          <view class="order-no-row">
            <text class="label">订单号：</text>
            <text class="value">{{ order.orderNo }}</text>
            <image class="copy-icon" src="/static/icons/copy.svg" mode="aspectFit" @tap.stop="copy(order.orderNo)" />
          </view>
          <view class="status-col">
            <text class="status-text" :class="getStatusColor(order.status)">{{ getStatusLabel(order.status) }}</text>
            <text class="status-desc">{{ getStatusHint(order.status) }}</text>
          </view>
        </view>

        <!-- 订单商品主体 -->
        <view class="card-body">
          <image class="product-img" :src="order.skuImage" mode="aspectFill" />
          <view class="product-info">
            <text class="product-title">{{ order.skuTitle }}</text>
            <view class="tag-row">
              <text class="type-tag" :class="getTypeTagClass(order.orderType)">{{ getTypeTagLabel(order.orderType) }}</text>
              <text v-if="order.skuTags" class="sku-tag">{{ order.skuTags }}</text>
            </view>

            <!-- 租赁特有字段 -->
            <template v-if="order.orderType === 'RENT'">
              <text v-if="order.rentStartDate && order.rentEndDate" class="info-line">
                租期：{{ formatDate(order.rentStartDate) }} 至 {{ formatDate(order.rentEndDate) }}
              </text>
              <view class="info-line-flex">
                <text>下单时间：{{ formatDateTime(order.createdAt) }}</text>
                <text class="price-text">¥ {{ formatAmount(order.payableMinor).replace('¥', '') }}</text>
              </view>
            </template>

            <!-- 购买特有字段 -->
            <template v-else-if="order.orderType === 'BUY'">
              <text class="info-line">下单时间：{{ formatDateTime(order.createdAt) }}</text>
              <view class="info-line-flex">
                <text>{{ getStatusHint(order.status) }}</text>
                <text class="price-text">¥ {{ formatAmount(order.payableMinor).replace('¥', '') }}</text>
              </view>
            </template>

            <!-- 软件特有字段 -->
            <template v-else-if="order.orderType === 'SOFTWARE'">
              <text class="info-line">下单时间：{{ formatDateTime(order.createdAt) }}</text>
              <view class="info-line-flex">
                <text>{{ getStatusHint(order.status) }}</text>
                <text class="price-text">¥ {{ formatAmount(order.payableMinor).replace('¥', '') }}</text>
              </view>
            </template>

          </view>
        </view>

        <view class="action-bar">
          <button
            v-if="order.status === 'PENDING_PAY'"
            class="action-btn"
            :disabled="operatingOrderId === order.id"
            @tap.stop="handleCancel(order.id)"
          >
            取消订单
          </button>
          <button
            v-if="order.status === 'PENDING_PAY'"
            class="action-btn primary"
            @tap.stop="goPay(order.id)"
          >
            去支付
          </button>
          <button v-else class="action-btn" @tap.stop="goDetail(order.id)">查看详情</button>
        </view>

      </view>
      <view class="no-more">
        {{ loading ? "正在加载..." : hasMore ? "上拉加载更多" : "已经到底了" }}
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { onShow } from "@dcloudio/uni-app";
import EmptyState from "../../components/EmptyState.vue";
import ErrorRetry from "../../components/ErrorRetry.vue";
import Skeleton from "../../components/PageSkeleton.vue";
import { formatAmount } from "../../utils/format";
import { cancelOrder, getOrderList } from "../../services/order";
import type { Order } from "../../services/order";
import { getSkuDetail } from "../../services/catalog";
import { navigateToPage } from "../../utils/navigation";
import { createLatestTask } from "../../utils/latestTask";

const statusBarHeight = ref(0);
onMounted(() => {
  const systemInfo = uni.getWindowInfo();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;
});

function goBack() { uni.navigateBack({ delta: 1 }); }

const typeTabs = [
  { label: "租", subLabel: "设备租赁", value: "RENT" },
  { label: "买", subLabel: "设备购买", value: "BUY" },
  { label: "软件", subLabel: "软件服务", value: "SOFTWARE" },
];

const statusTabs = [
  { label: "全部", value: "" },
  { label: "待付款", value: "PENDING_PAY" },
  { label: "待履约", value: "PAID" },
  { label: "履约中", value: "FULFILLING" },
  { label: "已完成", value: "COMPLETED" },
  { label: "已取消", value: "CANCELLED" },
];

const currentType = ref("RENT");
const currentStatus = ref("");
type OrderCard = Order & { skuTitle: string; skuImage: string; skuTags: string };
const orders = ref<OrderCard[]>([]);
const loading = ref(false);
const refreshing = ref(false);
const error = ref(false);
const page = ref(1);
const hasMore = ref(true);
const operatingOrderId = ref("");
const orderTask = createLatestTask();

function getStatusLabel(status: string) {
  switch (status) {
    case 'PENDING_PAY': return '待付款';
    case 'PAID': return '待履约';
    case 'FULFILLING': return '履约中';
    case 'COMPLETED': return '已完成';
    case 'CANCELLED': return '已取消';
    case 'REFUNDING': return '退款中';
    case 'REFUNDED': return '已退款';
    default: return status;
  }
}

function getStatusHint(status: string) {
  const hints: Record<string, string> = {
    PENDING_PAY: "等待支付",
    PAID: "等待商家履约",
    FULFILLING: "订单履约中",
    COMPLETED: "订单已完成",
    CANCELLED: "订单已取消",
    REFUNDING: "退款处理中",
    REFUNDED: "订单已退款",
  };
  return hints[status] || "";
}

function getStatusColor(status: string) {
  switch (status) {
    case 'PENDING_PAY': return 'text-orange';
    case 'PAID':
    case 'FULFILLING': return 'text-blue';
    case 'COMPLETED': return 'text-green';
    default: return 'text-gray';
  }
}

function getTypeTagLabel(type: string) {
  switch (type) {
    case 'RENT': return '租赁';
    case 'BUY': return '购买';
    case 'SOFTWARE': return '软件';
    default: return '';
  }
}

function getTypeTagClass(type: string) {
  switch (type) {
    case 'RENT': return 'tag-rent';
    case 'BUY': return 'tag-buy';
    case 'SOFTWARE': return 'tag-software';
    default: return '';
  }
}

function formatDate(value?: string) {
  return value?.slice(0, 10) || "";
}

function formatDateTime(value?: string) {
  return value ? value.replace("T", " ").slice(0, 16) : "";
}

function copy(text?: string) {
  if (!text) return;
  uni.setClipboardData({
    data: text,
    success: () => uni.showToast({ title: '复制成功', icon: 'none' })
  });
}

async function enrichOrder(order: Order): Promise<OrderCard> {
  try {
    const sku = await getSkuDetail(order.skuId);
    return {
      ...order,
      skuTitle: sku.title,
      skuImage: sku.image || "/static/images/product-placeholder.svg",
      skuTags: [sku.brand, sku.model].filter(Boolean).join(" · "),
    };
  } catch {
    return {
      ...order,
      skuTitle: "商品信息暂不可用",
      skuImage: "/static/images/product-placeholder.svg",
      skuTags: "",
    };
  }
}

async function fetchOrders(reset = false) {
  if (!reset && (loading.value || !hasMore.value)) return;
  const targetPage = reset ? 1 : page.value + 1;
  const requestId = orderTask.begin();
  loading.value = true;
  error.value = false;
  try {
    const result = await getOrderList({
      type: currentType.value,
      status: currentStatus.value || undefined,
      page: targetPage,
      pageSize: 10,
    });
    const cards = await Promise.all(result.items.map(enrichOrder));
    if (!orderTask.isCurrent(requestId)) return;
    orders.value = reset ? cards : [...orders.value, ...cards];
    page.value = targetPage;
    hasMore.value = orders.value.length < result.total;
  } catch (requestError) {
    if (orderTask.isCurrent(requestId)) {
      error.value = true;
      if (reset) orders.value = [];
      console.error("Failed to load orders", requestError);
    }
  } finally {
    if (orderTask.isCurrent(requestId)) {
      loading.value = false;
      refreshing.value = false;
    }
  }
}

function refreshOrders() {
  refreshing.value = orders.value.length > 0;
  return fetchOrders(true);
}

function loadMore() {
  fetchOrders(false);
}

function goDetail(id: string) {
  navigateToPage(`/pages/order/detail?id=${id}`);
}

function goPay(id: string) {
  navigateToPage(`/pages/order/detail?id=${id}&action=pay`);
}

function goCategory() {
  navigateToPage("/pages/category/index");
}

async function handleCancel(id: string) {
  const result = await uni.showModal({ title: "确认取消", content: "确定要取消该订单吗？" });
  if (!result.confirm || operatingOrderId.value) return;
  operatingOrderId.value = id;
  try {
    await cancelOrder(id);
    uni.showToast({ title: "订单已取消", icon: "none" });
    await refreshOrders();
  } catch (cancelError) {
    const message = cancelError instanceof Error ? cancelError.message : "取消失败";
    uni.showToast({ title: message, icon: "none" });
  } finally {
    operatingOrderId.value = "";
  }
}

watch([currentType, currentStatus], () => refreshOrders());
onShow(() => refreshOrders());
</script>

<style scoped lang="scss">
.page {
  background-color: #f7f8fa;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.nav-bar {
  background: #fff;
  z-index: 100;
}
.nav-content {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}
.back-btn { width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; }
.back-icon { width: 20px; height: 20px; }
.nav-title {
  font-size: 17px;
  font-weight: 500;
  color: #000;
}

.top-section {
  background: #fff;
  padding-bottom: 8px;
}
.type-tabs {
  display: flex;
  padding: 12px 16px;
  gap: 8px;
}
.type-tab {
  flex: 1;
  background: #f7f9fc;
  border-radius: 8px;
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.type-tab.active {
  background: #0a4bfe;
  box-shadow: 0 4px 12px rgba(10, 75, 254, 0.2);
}
.type-label { font-size: 18px; font-weight: bold; color: #333; margin-bottom: 4px; }
.type-sub { font-size: 11px; color: #666; }
.type-tab.active .type-label, .type-tab.active .type-sub { color: #fff; }

.status-filter-bar {
  display: flex;
  align-items: center;
  padding: 0 16px;
  margin-top: 4px;
}
.status-tabs {
  flex: 1;
  width: 0; // for scroll-view flex bug
}
.status-tabs-inner {
  display: flex;
  gap: 8px;
  padding: 4px 0;
}
.status-tab {
  padding: 6px 16px;
  border-radius: 16px;
  background: #f7f9fc;
  font-size: 13px;
  color: #666;
  white-space: nowrap;
}
.status-tab.active {
  background: #eef2ff;
  color: #0a4bfe;
  font-weight: 500;
}
.filter-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
.filter-icon { width: 18px; height: 18px; }

.order-list {
  flex: 1;
  height: 0;
  padding: 12px 16px;
  box-sizing: border-box;
}
.order-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}
.order-no-row { display: flex; align-items: center; font-size: 12px; }
.order-no-row .label { color: #666; }
.order-no-row .value { color: #333; font-family: din-alternate, sans-serif; }
.copy-icon { width: 14px; height: 14px; margin-left: 4px; opacity: 0.6; }
.copy-icon-sm { width: 12px; height: 12px; margin-left: 4px; opacity: 0.6; }

.status-col { display: flex; flex-direction: column; align-items: flex-end; }
.status-text { font-size: 14px; font-weight: 600; margin-bottom: 4px; }
.status-desc { font-size: 10px; color: #666; }

.card-body {
  display: flex;
  margin-bottom: 16px;
}
.product-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  background: #f9f9f9;
  margin-right: 12px;
  flex-shrink: 0;
}
.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.product-title { font-size: 14px; font-weight: 600; color: #333; margin-bottom: 6px; }
.tag-row { display: flex; align-items: center; margin-bottom: 8px; }
.type-tag { font-size: 10px; padding: 2px 6px; border-radius: 4px; margin-right: 6px; }
.tag-rent { background: #eef2ff; color: #0a4bfe; }
.tag-buy { background: #eef2ff; color: #0a4bfe; } // all use blue in design
.tag-software { background: #eef2ff; color: #0a4bfe; }
.sku-tag { font-size: 11px; color: #666; }

.info-line { font-size: 11px; color: #999; margin-bottom: 4px; }
.info-line-flex { display: flex; justify-content: space-between; align-items: flex-end; font-size: 11px; color: #999; margin-top: auto; }
.logistics-row { display: flex; align-items: center; }
.price-text { font-size: 16px; font-weight: bold; color: #e11d48; font-family: din-alternate, sans-serif; }

/* 租赁续租/取消块 */
.promo-box {
  background: #f7f9fc;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.promo-left { display: flex; flex-direction: column; gap: 4px; }
.promo-title { display: flex; align-items: center; font-size: 12px; font-weight: 500; color: #0a4bfe; }
.promo-icon { width: 14px; height: 14px; margin-right: 4px; }
.promo-desc { font-size: 11px; color: #666; }
.promo-btn { margin: 0; height: 28px; line-height: 26px; font-size: 12px; padding: 0 16px; border-radius: 14px; }
.outline-blue { border: 1px solid #0a4bfe; color: #0a4bfe; background: transparent; }
.outline-blue::after { border: none; }

.rule-notice {
  background: #fff8f1;
  border-radius: 8px;
  padding: 10px 12px;
  display: flex;
  align-items: flex-start;
}
.warn-icon { width: 14px; height: 14px; margin-right: 6px; margin-top: 2px; flex-shrink: 0; }
.rule-text { flex: 1; font-size: 11px; color: #d97706; line-height: 1.4; }
.fw-bold { font-weight: bold; }
.rule-notice .arrow-right { width: 14px; height: 14px; margin-left: 6px; margin-top: 2px; opacity: 0.6; }

/* 购买物流块 */
.logistics-progress {
  display: flex;
  align-items: flex-start;
  padding: 16px 0;
  border-top: 1px dashed #eee;
  border-bottom: 1px dashed #eee;
  margin-bottom: 16px;
}
.progress-node {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  opacity: 0.4;
}
.progress-node.current, .progress-node.done { opacity: 1; }
.node-icon-wrapper {
  width: 24px; height: 24px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 6px;
  z-index: 2;
}
.progress-node.current .node-icon-wrapper { background: #0a4bfe; }
.progress-node.done .node-icon-wrapper { background: #0a4bfe; }
.node-icon { width: 14px; height: 14px; filter: grayscale(1); }
.progress-node.current .node-icon, .progress-node.done .node-icon { filter: brightness(0) invert(1); }

.node-text { display: flex; flex-direction: column; align-items: center; }
.node-status { font-size: 11px; color: #333; margin-bottom: 2px; }
.progress-node.current .node-status { color: #0a4bfe; font-weight: 500; }
.node-time { font-size: 9px; color: #999; }
.node-line {
  position: absolute;
  top: 12px; left: 50%; width: 100%;
  border-top: 2px dashed #e5e7eb;
  z-index: 1;
}
.progress-node.done .node-line { border-top: 2px solid #0a4bfe; }

/* 软件交付块 */
.software-delivery {
  background: #f7f9fc;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.delivery-row { display: flex; align-items: center; font-size: 12px; }
.delivery-row .label { color: #666; width: 64px; }
.delivery-row .value { color: #333; font-family: din-alternate, sans-serif; }
.delivery-row .value.link { color: #0a4bfe; text-decoration: underline; font-family: inherit; }
.text-ellipsis { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 160px; }
.eye-icon { width: 16px; height: 16px; margin-left: 8px; opacity: 0.5; }
.spacer { flex: 1; }
.copy-btn { margin: 0; padding: 0 12px; height: 24px; line-height: 22px; font-size: 11px; color: #0a4bfe; background: #eef2ff; border-radius: 4px; }
.copy-btn::after { border: none; }

/* 操作按钮组 */
.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.action-btn {
  margin: 0;
  height: 32px;
  line-height: 30px;
  font-size: 13px;
  padding: 0 16px;
  border-radius: 16px;
  background: #fff;
  color: #333;
  border: 1px solid #ddd;
}
.action-btn::after { border: none; }
.action-btn.primary {
  background: #0a4bfe;
  color: #fff;
  border: 1px solid #0a4bfe;
}

.no-more { text-align: center; font-size: 12px; color: #999; padding: 16px 0; }

.text-orange { color: #f59e0b; }
.text-blue { color: #0a4bfe; }
.text-green { color: #10b981; }
.text-gray { color: #9ca3af; }
</style>
