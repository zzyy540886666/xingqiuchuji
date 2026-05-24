<template>
  <view class="page">
    <view class="card">
      <text class="label">可提现金额</text>
      <text class="available">{{ formatAmount(available) }}</text>
    </view>
    <view class="card">
      <text class="label">提现金额（元）</text>
      <input class="amount-input" type="digit" :placeholder="placeholder" v-model="inputValue" @input="onInput" />
      <view class="fee-row" v-if="fee > 0">
        <text>手续费：{{ formatAmount(fee) }}</text>
        <text>实际到账：{{ formatAmount(actualAmount) }}</text>
      </view>
    </view>
    <view class="submit-btn" :class="{ disabled: !canSubmit || submitting }" @tap="handleSubmit">
      <text>{{ submitting ? "提交中..." : "确认提现" }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { formatAmount } from "../../utils/format";
import { getWallet, withdraw } from "../../services/wallet";

const available = ref(0);
const inputValue = ref("");
const submitting = ref(false);
const minWithdraw = 100;

const placeholder = computed(() => `最低提现 ${formatAmount(minWithdraw)}`);
const amountMinor = computed(() => Math.round(parseFloat(inputValue.value || "0") * 100));
const fee = computed(() => Math.max(0, Math.round(amountMinor.value * 0.006)));
const actualAmount = computed(() => amountMinor.value - fee.value);
const canSubmit = computed(() => amountMinor.value >= minWithdraw && amountMinor.value <= available.value);

function onInput() {}

async function handleSubmit() {
  if (!canSubmit.value || submitting.value) return;
  const [modalErr, modalRes] = await uni.showModal({ title: "确认提现", content: `提现 ${formatAmount(amountMinor.value)}，手续费 ${formatAmount(fee.value)}，实际到账 ${formatAmount(actualAmount.value)}` }) as unknown as [null, { confirm: boolean }];
  if (modalErr || !modalRes?.confirm) return;
  submitting.value = true;
  const key = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  try {
    const result = await withdraw(amountMinor.value, key);
    if (result.status === "FAILED") {
      throw new Error(result.failReason || "提现转账失败");
    }
    uni.showToast({ title: result.status === "SUCCESS" ? "提现成功" : "提现申请已提交", icon: "none" });
    setTimeout(() => uni.navigateBack(), 1500);
  } catch (e: any) {
    uni.showToast({ title: e.message || "提现失败", icon: "none" });
  } finally { submitting.value = false; }
}

onLoad(async () => {
  try { const w = await getWallet(); available.value = w.balanceMinor; } catch {}
});
</script>

<style scoped lang="scss">
.card { background: #fff; margin: 28rpx; border-radius: 20rpx; padding: 32rpx; }
.label { display: block; font-size: 24rpx; color: #6b7280; margin-bottom: 12rpx; }
.available { font-size: 48rpx; font-weight: 800; color: #111827; }
.amount-input { font-size: 40rpx; font-weight: 700; padding: 16rpx 0; border-bottom: 2rpx solid #e5e7eb; }
.fee-row { display: flex; justify-content: space-between; margin-top: 16rpx; font-size: 22rpx; color: #6b7280; }
.submit-btn { margin: 48rpx 28rpx; height: 88rpx; border-radius: 999rpx; background: #0a4bfe; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 30rpx; font-weight: 700; }
.submit-btn.disabled { opacity: 0.5; }
</style>
