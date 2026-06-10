<template>
  <view class="page order-page">
    <view class="header order-header">
      <view class="order-title">
        <view class="back-button" @tap="back"><image class="back-icon" src="/static/icons/back.svg" mode="aspectFit" /></view>
        <text>确认订单</text>
      </view>
    </view>

    <template v-if="loading">
      <Skeleton variant="order-confirm" />
    </template>
    <template v-else>

    <view class="address-card card" @tap="chooseAddress">
      <image class="pin" src="/static/icons/pin.svg" mode="aspectFit" />
      <view class="address-body">
        <view class="person"><text>{{ recipient.name }}</text><text>{{ recipient.phone }}</text></view>
        <text class="address">{{ deliveryAddress || "请选择配送地址" }}</text>
      </view>
      <image class="chevron" src="/static/icons/chevron-right.svg" mode="aspectFit" />
    </view>

    <view class="promo-line">
      <text>宇树机器狗</text>
      <text>全新优惠</text>
      <text>高配优选</text>
    </view>

    <view class="order-card card">
      <view class="product-row">
        <image v-if="product?.image" class="order-image" :src="product.image" mode="aspectFit" />
        <view class="order-product">
          <text class="order-product-title">{{ product?.title || "机器人租赁服务" }}</text>
          <view class="order-tags"><text>{{ orderTypeLabel }}</text><text v-if="product?.brand">{{ product.brand }}</text></view>
          <view class="order-price-row"><text class="price">{{ product ? formatAmount(product.priceAmount) : "--" }}</text><text>/天起</text><text>x 1</text></view>
        </view>
      </view>
      <view v-if="orderType === 'RENT'" class="duration-block">
        <text class="field-label">时长</text>
        <view class="duration-options">
          <text v-for="duration in durations" :key="duration.label" :class="{ active: selectedDuration === duration.days }" @tap="selectedDuration = duration.days">{{ duration.label }}</text>
        </view>
      </view>
      <view v-for="item in orderFields" :key="item.label" class="field-row">
        <text>{{ item.label }}</text>
        <view><text :class="{ deposit: item.label === '押金' }">{{ item.value }}</text><text v-if="item.extra" class="field-extra">{{ item.extra }}</text></view>
      </view>
      <view class="service-field" @tap="showService('delivery')">
        <image class="service-icon" src="/static/icons/service.svg" mode="aspectFit" />
        <view>
          <text class="service-title">送货上门</text>
          <text class="service-desc">含上门安装、送货培训等，7x24小时售后</text>
        </view>
        <image class="chevron" src="/static/icons/chevron-right.svg" mode="aspectFit" />
      </view>
      <view class="service-field" @tap="showService('support')">
        <image class="service-icon" src="/static/icons/grid.svg" mode="aspectFit" />
        <view>
          <text class="service-title">服务项目</text>
          <text class="service-desc">含发票税、发送相关技术文档等，7x24小时销售</text>
        </view>
        <image class="chevron" src="/static/icons/chevron-right.svg" mode="aspectFit" />
      </view>
    </view>

    <view class="invoice card" @tap="configureInvoice">
      <text>发票信息</text>
      <view><text>{{ invoiceRequested ? "已选电子普通发票" : "可选择" }}</text><image class="chevron" src="/static/icons/chevron-right.svg" mode="aspectFit" /></view>
    </view>

    <view class="discount-card card">
      <view class="discount-row" @tap="toggleCoupon">
        <view>
          <text>优惠券</text>
          <text class="discount-desc">省 {{ formatAmount(availableCouponMinor) }}</text>
        </view>
        <text :class="{ active: useCoupon }">{{ useCoupon ? "已使用" : "未使用" }}</text>
      </view>
      <view class="discount-row" @tap="toggleLightYear">
        <view>
          <text>光年币</text>
          <text class="discount-desc">省 {{ formatAmount(availableLightYearMinor) }}</text>
        </view>
        <text :class="{ active: useLightYear }">{{ useLightYear ? "已使用" : "未使用" }}</text>
      </view>
    </view>

    <view class="fee-card card">
      <text class="fee-title">费用信息</text>
      <view v-for="fee in fees" :key="fee.label" class="fee-row">
        <view class="fee-left">
          <text>{{ fee.label }}</text>
          <text v-if="fee.used" class="used">已使用</text>
        </view>
        <text :class="{ discount: fee.amount.startsWith('-') }">{{ fee.amount }}</text>
      </view>
    </view>

    <view class="note card">
      <text>订单备注</text>
      <input v-model="remark" placeholder="选填，请填写订单备注（50字以内）" maxlength="50" />
    </view>

    <view class="bottom-action order-bottom">
      <view class="guard-row">
        <text>支付保障</text>
        <text>售后保障</text>
        <text>服务条款</text>
        <text>信用免押</text>
      </view>
      <view class="pay-row">
        <view class="total"><text>合计：</text><text>{{ preview ? formatAmount(preview.payableAmount) : "--" }}</text><text>以后端计价为准</text></view>
        <view class="green-pill" :class="{ disabled: !preview || paying }" @tap="pay">{{ paying ? "处理中..." : "微信支付" }}</view>
      </view>
    </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { getSkuDetail, type SkuItem } from "../../services/catalog";
import { createOrder, getWechatPayParams, previewOrder, type OrderPreviewParams, type OrderPreviewResult } from "../../services/order";
import { formatAmount } from "../../utils/format";
import Skeleton from "../../components/PageSkeleton.vue";
import { redirectToPage } from "../../utils/navigation";
import { buildPageUrl } from "../../utils/query";

const durations = [{ label: "30天", days: 30 }, { label: "60天", days: 60 }, { label: "180天", days: 180 }, { label: "365天", days: 365 }];
const selectedDuration = ref(30);
const product = ref<SkuItem | null>(null);
const preview = ref<OrderPreviewResult | null>(null);
const paying = ref(false);
const loading = ref(true);
const invoiceRequested = ref(false);
const remark = ref("");
const recipient = ref({ name: "收货人", phone: "" });
const deliveryAddress = ref("");
const selectedAddress = ref<Record<string, string> | null>(null);
const useCoupon = ref(false);
const useLightYear = ref(false);
const availableCouponMinor = 0;
const availableLightYearMinor = 0;
let skuId = 0;
const orderType = ref<OrderPreviewParams["orderType"]>("RENT");

const orderTypeLabel = computed(() => ({ RENT: "租赁", BUY: "购买", SOFTWARE: "软件服务" }[orderType.value]));
const startDate = computed(() => formatDate(new Date()));
const endDate = computed(() => formatDate(addDays(new Date(), selectedDuration.value)));
const orderFields = computed(() => [
  ...(orderType.value === "RENT" ? [
    { label: "开始时间", value: startDate.value },
    { label: "结束日期", value: endDate.value, extra: `${selectedDuration.value}天` },
  ] : []),
  { label: "押金", value: preview.value ? formatAmount(preview.value.depositAmount) : "--" },
]);
const fees = computed(() => (preview.value?.priceBreakdown ?? []).map((fee) => {
  let label = fee.label;
  if (label === 'Buyout price') label = '买断价';
  else if (label === 'Deposit') label = '押金';
  else if (label === 'Shipping') label = '运费';
  else if (label === 'Rent fee') label = '租金';
  
  return {
    label,
    amount: formatAmount(fee.amountMinor),
    used: fee.amountMinor < 0,
  };
}));

onLoad(async (options) => {
  loading.value = true;
  skuId = Number(options?.skuId || 0);
  const requestedType = String(options?.orderType || "RENT");
  orderType.value = ["RENT", "BUY", "SOFTWARE"].includes(requestedType)
    ? requestedType as OrderPreviewParams["orderType"]
    : "RENT";
  const requestedDays = Number(options?.rentDays || 30);
  selectedDuration.value = durations.some((duration) => duration.days === requestedDays) ? requestedDays : 30;
  if (!skuId) {
    uni.showToast({ title: "商品信息缺失", icon: "none" });
    loading.value = false;
    return;
  }
  try {
    product.value = await getSkuDetail(skuId);
    await loadPreview();
  } catch (error: any) {
    uni.showToast({ title: error.message || "订单加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
});

watch(selectedDuration, () => {
  if (skuId) loadPreview();
});
watch([useCoupon, useLightYear], () => {
  if (skuId) loadPreview();
});

function formatDate(date: Date) {
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${date.getFullYear()}-${month}-${day}`;
}

function addDays(date: Date, days: number) {
  const next = new Date(date);
  next.setDate(next.getDate() + days);
  return next;
}

function buildPayload(): OrderPreviewParams {
  const payload: OrderPreviewParams = {
    skuId,
    orderType: orderType.value,
    address: selectedAddress.value ? JSON.stringify(selectedAddress.value) : undefined,
  };
  if (orderType.value === "RENT") {
    payload.rentStartDate = startDate.value;
    payload.rentEndDate = endDate.value;
  }
  return payload;
}

async function loadPreview() {
  try {
    preview.value = await previewOrder(buildPayload());
  } catch (error: any) {
    preview.value = null;
    uni.showToast({ title: error.message || "计价失败", icon: "none" });
  }
}

function back() {
  uni.navigateBack({ delta: 1 });
}

function chooseAddress() {
  uni.chooseAddress({
    success: saveAddress,
    fail: handleAddressError,
  });
}

function saveAddress(result: any) {
  selectedAddress.value = {
    userName: result.userName,
    telNumber: result.telNumber,
    provinceName: result.provinceName,
    cityName: result.cityName,
    countyName: result.countyName,
    detailInfo: result.detailInfo,
    postalCode: result.postalCode || '',
  };
  recipient.value = { name: result.userName, phone: result.telNumber };
  deliveryAddress.value = `${result.provinceName}${result.cityName}${result.countyName}${result.detailInfo}`;
  loadPreview();
}

function handleAddressError(result: any) {
  if (String(result?.errMsg || '').includes('auth deny')) {
    uni.showModal({
      title: "需要地址权限",
      content: "请在设置中允许使用通讯地址后重新选择。",
      success(modal) {
        if (modal.confirm) uni.openSetting({});
      },
    });
    return;
  }
  uni.showToast({ title: "未选择配送地址", icon: "none" });
}

function configureInvoice() {
  invoiceRequested.value = !invoiceRequested.value;
  uni.showToast({ title: invoiceRequested.value ? "已选择电子普通发票" : "已取消发票", icon: "none" });
}

function toggleCoupon() {
  useCoupon.value = !useCoupon.value;
}

function toggleLightYear() {
  useLightYear.value = !useLightYear.value;
}

function showService(type: "delivery" | "support") {
  const content = type === "delivery"
    ? "服务包含上门配送、安装和基础操作培训，具体排期以订单确认结果为准。"
    : "支付完成后可在订单中获取技术资料，并通过消息入口联系售后。";
  uni.showModal({ title: type === "delivery" ? "送货上门" : "服务项目", content, showCancel: false });
}

async function pay() {
  if (!preview.value || paying.value) return;
  if (!selectedAddress.value && orderType.value !== "SOFTWARE") {
    uni.showToast({ title: "请先选择收货地址", icon: "none" });
    return;
  }
  paying.value = true;
  try {
    const key = `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
    const order = await createOrder(buildPayload(), key);
    const params = await getWechatPayParams(order.id);
    uni.requestPayment({
      provider: "wxpay",
      ...params,
      success() {
        redirectToPage(buildPageUrl("/pages/order/pay-result", { id: order.id, status: "success" }));
      },
      fail() {
        redirectToPage(buildPageUrl("/pages/order/pay-result", { id: order.id, status: "fail" }));
      },
      complete() {
        paying.value = false;
      },
    });
  } catch (error: any) {
    paying.value = false;
    uni.showToast({ title: error.message || "支付发起失败", icon: "none" });
  }
}
</script>

<style scoped lang="scss">
.order-page {
  padding-bottom: 160rpx;
}

.order-header {
  border-bottom: 1rpx solid #f3f4f6;
}

.order-title {
  height: 78rpx;
  padding: 0 28rpx 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.order-title .back-button {
  position: absolute;
  left: 28rpx;
}

.order-title > text {
  font-size: 32rpx;
  font-weight: 900;
}

.address-card {
  margin: 22rpx 22rpx 0;
  padding: 28rpx;
  display: flex;
  align-items: flex-start;
  gap: 18rpx;
}

.pin {
  width: 32rpx;
  height: 32rpx;
  margin-top: 6rpx;
  display: block;
}

.address-body {
  flex: 1;
  min-width: 0;
}

.person {
  display: flex;
  gap: 20rpx;
  margin-bottom: 10rpx;
}

.person text:first-child {
  font-size: 28rpx;
  font-weight: 900;
}

.person text:last-child,
.address {
  font-size: 26rpx;
  color: #4b5563;
  line-height: 1.6;
}

.promo-line {
  margin: 18rpx 22rpx 0;
  height: 54rpx;
  background: linear-gradient(90deg, #eff6ff, #eef2ff);
  border-radius: 14rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  gap: 22rpx;
}

.promo-line text {
  color: #4b5563;
  font-size: 22rpx;
}

.promo-line text:first-child {
  color: #0a4bfe;
  font-weight: 800;
}

.order-card,
.invoice,
.discount-card,
.fee-card,
.note {
  margin: 16rpx 22rpx 0;
  padding: 26rpx;
}

.product-row {
  display: flex;
  gap: 20rpx;
  padding-bottom: 24rpx;
}

.order-image {
  width: 140rpx;
  height: 140rpx;
  border-radius: 16rpx;
  background: #f5f6f8;
  flex-shrink: 0;
}

.order-product {
  flex: 1;
  min-width: 0;
}

.order-product-title {
  display: block;
  font-size: 26rpx;
  font-weight: 900;
  margin-bottom: 12rpx;
}

.order-tags {
  display: flex;
  gap: 10rpx;
}

.order-tags text {
  height: 32rpx;
  padding: 0 10rpx;
  background: #e8f3ff;
  color: #0a4bfe;
  border-radius: 6rpx;
  font-size: 20rpx;
}

.order-price-row {
  margin-top: 28rpx;
  display: flex;
  align-items: baseline;
  gap: 6rpx;
  color: #6b7280;
  font-size: 22rpx;
}

.order-price-row .price {
  font-size: 32rpx;
}

.duration-block {
  padding: 22rpx 0;
  border-top: 1rpx solid #f3f4f6;
}

.field-label {
  display: block;
  color: #374151;
  font-size: 24rpx;
  font-weight: 700;
  margin-bottom: 14rpx;
}

.duration-options {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12rpx;
}

.duration-options text {
  height: 56rpx;
  border: 3rpx solid #e5e7eb;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4b5563;
  font-size: 22rpx;
  font-weight: 700;
}

.duration-options .active {
  border-color: #0a4bfe;
  color: #0a4bfe;
  background: #f2f6ff;
}

.field-row,
.invoice,
.invoice view,
.fee-row,
.note,
.service-field {
  display: flex;
  align-items: center;
}

.field-row {
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f9fafb;
  color: #4b5563;
  font-size: 24rpx;
}

.field-row view {
  display: flex;
  gap: 12rpx;
  align-items: center;
  color: #111827;
}

.field-extra {
  color: #9ca3af;
}

.deposit {
  color: #f53f3f;
  font-weight: 900;
}

.service-field {
  gap: 18rpx;
  padding: 22rpx 0;
  border-bottom: 1rpx solid #f9fafb;
}

.service-field view {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.service-icon {
  width: 32rpx;
  height: 32rpx;
  display: block;
}

.service-title {
  font-size: 24rpx;
  color: #111827;
}

.service-desc {
  font-size: 22rpx;
  color: #9ca3af;
  line-height: 1.5;
}

.invoice {
  justify-content: space-between;
  font-size: 24rpx;
  color: #111827;
}

.invoice view {
  gap: 12rpx;
  color: #9ca3af;
  font-size: 22rpx;
}

.discount-card {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.discount-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #111827;
  font-size: 24rpx;
}

.discount-row view {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.discount-desc {
  color: #9ca3af;
  font-size: 22rpx;
}

.discount-row > text {
  color: #9ca3af;
  font-size: 22rpx;
}

.discount-row > text.active {
  color: #f53f3f;
  font-weight: 900;
}

.fee-title {
  display: block;
  font-size: 26rpx;
  font-weight: 900;
  margin-bottom: 20rpx;
}

.fee-row {
  justify-content: space-between;
  margin-bottom: 18rpx;
  color: #4b5563;
  font-size: 24rpx;
}

.fee-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.used {
  background: #f53f3f;
  color: #fff;
  font-size: 18rpx;
  padding: 2rpx 8rpx;
  border-radius: 6rpx;
}

.discount {
  color: #f53f3f;
  font-weight: 900;
}

.note {
  gap: 20rpx;
  color: #4b5563;
  font-size: 24rpx;
}

.note input {
  flex: 1;
  min-width: 0;
  font-size: 24rpx;
}

.order-bottom {
  display: block;
}

.guard-row {
  display: flex;
  gap: 22rpx;
  color: #6b7280;
  font-size: 18rpx;
  margin-bottom: 16rpx;
}

.pay-row {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.total {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
  flex-shrink: 0;
}

.total text:nth-child(1) {
  color: #4b5563;
  font-size: 22rpx;
}

.total text:nth-child(2) {
  color: #f53f3f;
  font-size: 40rpx;
  font-weight: 900;
}

.total text:nth-child(3) {
  color: #9ca3af;
  font-size: 20rpx;
}

.green-pill {
  flex: 1;
}

.green-pill.disabled {
  opacity: 0.5;
}
</style>
