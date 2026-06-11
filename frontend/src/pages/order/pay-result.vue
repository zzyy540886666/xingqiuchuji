<template>
  <view class="page pay-result">
    <view class="result-icon" :class="isSuccess ? 'success' : isPending ? 'pending' : 'fail'">
      <image class="icon-image" :src="resultIcon" mode="aspectFit" />
    </view>
    <text class="result-title">{{ title }}</text>
    <text class="result-desc">{{ description }}</text>
    <view class="result-actions">
      <view class="action-btn primary" @tap="goOrderDetail">查看订单</view>
      <view v-if="!isSuccess && !isPending" class="action-btn outline" @tap="retryPay">重新支付</view>
      <view class="action-btn outline" @tap="goHome">返回首页</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { getOrderDetail } from "../../services/order";
import { reLaunchToPage, redirectToPage } from "../../utils/navigation";
import { buildPageUrl } from "../../utils/query";

const isSuccess = ref(false);
const checking = ref(false);
const returnedSuccess = ref(false);
const callbackPending = ref(false);
let orderId = "";
const isPending = computed(() => checking.value || callbackPending.value);
const title = computed(() => isSuccess.value ? "支付成功" : isPending.value ? "支付确认中" : "支付失败");
const resultIcon = computed(() => isSuccess.value ? "/static/icons/success.svg" : isPending.value ? "/static/icons/pending.svg" : "/static/icons/fail.svg");
const description = computed(() => isSuccess.value
  ? "订单已支付，请等待处理"
  : isPending.value
    ? "正在等待支付回调确认，可稍后在订单详情中查看状态"
    : "支付未完成，您可以重新支付");

onLoad(async (options) => {
  orderId = options?.id || "";
  returnedSuccess.value = options?.status === "success";
  if (!returnedSuccess.value) return;
  checking.value = true;
  for (let attempt = 0; attempt < 3; attempt++) {
    try {
      const order = await getOrderDetail(orderId);
      if (["PAID", "FULFILLING", "COMPLETED"].includes(order.status)) {
        isSuccess.value = true;
        checking.value = false;
        return;
      }
    } catch {
      break;
    }
    await new Promise((resolve) => setTimeout(resolve, 1000));
  }
  callbackPending.value = true;
  checking.value = false;
});

function goOrderDetail() { redirectToPage(buildPageUrl("/pages/order/detail", { id: orderId })); }
function retryPay() { redirectToPage(buildPageUrl("/pages/order/detail", { id: orderId, action: "pay" })); }
function goHome() { reLaunchToPage("/pages/home/index"); }
</script>

<style scoped lang="scss">
.pay-result { display: flex; flex-direction: column; align-items: center; padding-top: 160rpx; }
.result-icon { width: 120rpx; height: 120rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin-bottom: 32rpx; }
.result-icon.success { background: #dcfce7; }
.result-icon.fail { background: #fee2e2; }
.result-icon.pending { background: #fef3c7; }
.icon-image { width: 72rpx; height: 72rpx; display: block; }
.result-title { font-size: 36rpx; font-weight: 700; color: #111827; margin-bottom: 16rpx; }
.result-desc { font-size: 26rpx; color: #6b7280; margin-bottom: 60rpx; }
.result-actions { display: flex; flex-direction: column; gap: 20rpx; width: 400rpx; }
.action-btn { height: 80rpx; border-radius: 999rpx; display: flex; align-items: center; justify-content: center; font-size: 28rpx; }
.action-btn.primary { background: #0a4bfe; color: #fff; }
.action-btn.outline { border: 2rpx solid #d1d5db; color: #374151; }
</style>
