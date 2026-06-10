<template>
  <view class="page">
    <!-- 背景区域 -->
    <view class="bg-header"></view>
    
    <!-- 自定义导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px', height: navBarHeight + 'px' }">
      <view class="back-btn" @tap="goBack">
        <image class="back-icon" src="/static/icons/back-light.svg" mode="aspectFit" />
      </view>
      <text class="nav-title">提现</text>
      <!-- 为右侧胶囊留白 -->
      <view class="nav-placeholder"></view>
    </view>

    <scroll-view scroll-y class="content" :style="{ top: (statusBarHeight + navBarHeight) + 'px' }">
      <!-- 余额区域 -->
      <view class="balance-section">
        <text class="balance-label">可提现金额（元）</text>
        <view class="balance-value-row">
          <text class="balance-symbol">¥</text>
          <text class="balance-value">{{ formatAmount(available) }}</text>
        </view>
      </view>

      <!-- 提现操作卡片 -->
      <view class="withdraw-card">
        <text class="section-title">提现金额</text>
        <view class="input-row">
          <text class="currency-symbol">¥</text>
          <input 
            class="amount-input" 
            type="digit" 
            :placeholder="placeholder" 
            v-model="inputValue" 
            placeholder-class="input-placeholder"
          />
          <text class="withdraw-all-btn" @tap="withdrawAll">全部提现</text>
        </view>
        
        <view class="fee-info" v-if="inputValue && amountMinor > 0">
          <view class="fee-row">
            <text class="fee-label">手续费 (0.6%)</text>
            <text class="fee-value">{{ formatAmount(fee) }} 元</text>
          </view>
          <view class="fee-row">
            <text class="fee-label">实际到账</text>
            <text class="fee-value actual">{{ formatAmount(actualAmount) }} 元</text>
          </view>
        </view>

        <!-- 提现方式选择 -->
        <view class="method-section">
          <text class="section-title">提现至</text>
          <view class="method-list">
            <view class="method-item" :class="{ active: withdrawMethod === 'wechat' }" @tap="withdrawMethod = 'wechat'">
              <view class="method-left">
                <image class="method-icon" src="/static/icons/wechat-pay.svg" mode="aspectFit" />
                <view class="method-text">
                  <text class="method-name">微信零钱</text>
                  <text class="method-desc">实时到账</text>
                </view>
              </view>
              <image v-if="withdrawMethod === 'wechat'" class="check-icon" src="/static/icons/check-blue.svg" mode="aspectFit" />
            </view>
            
            <view class="method-item" :class="{ active: withdrawMethod === 'bank' }" @tap="withdrawMethod = 'bank'">
              <view class="method-left">
                <image class="method-icon" src="/static/icons/bank-card.svg" mode="aspectFit" />
                <view class="method-text">
                  <text class="method-name">银行卡</text>
                  <text class="method-desc">预计1-3个工作日到账</text>
                </view>
              </view>
              <image v-if="withdrawMethod === 'bank'" class="check-icon" src="/static/icons/check-blue.svg" mode="aspectFit" />
            </view>
          </view>
        </view>
      </view>

      <!-- 提交按钮 -->
      <button 
        class="submit-btn" 
        :class="{ disabled: !canSubmit || submitting }" 
        @tap="handleSubmit"
        :loading="submitting"
      >
        确认提现
      </button>

      <!-- 提现规则提示 -->
      <view class="rules-section">
        <text class="rules-title">提现说明</text>
        <text class="rule-text">1. 最低提现金额为 {{ formatAmount(minWithdraw) }} 元。</text>
        <text class="rule-text">2. 微信提现一般实时到账，单笔限额 20,000 元。</text>
        <text class="rule-text">3. 提现收取 0.6% 手续费，由第三方支付平台收取。</text>
      </view>
      
      <view class="bottom-placeholder"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { formatAmount } from "../../utils/format";
import { getWallet, withdraw } from "../../services/wallet";

const statusBarHeight = ref(0);
const navBarHeight = ref(44);
{
  const systemInfo = uni.getWindowInfo();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;
}

const available = ref(0);
const inputValue = ref("");
const submitting = ref(false);
const minWithdraw = 100;
const withdrawMethod = ref("wechat"); // 'wechat' or 'bank'

const placeholder = computed(() => `最低提现 ${formatAmount(minWithdraw)}`);
const amountMinor = computed(() => Math.round(parseFloat(inputValue.value || "0") * 100));
const fee = computed(() => Math.max(0, Math.round(amountMinor.value * 0.006)));
const actualAmount = computed(() => amountMinor.value - fee.value);
const canSubmit = computed(() => amountMinor.value >= minWithdraw && amountMinor.value <= available.value);

function withdrawAll() {
  inputValue.value = (available.value / 100).toString();
}

async function handleSubmit() {
  if (!canSubmit.value || submitting.value) return;
  const res = await uni.showModal({ 
    title: "确认提现", 
    content: `提现 ${formatAmount(amountMinor.value)}元，手续费 ${formatAmount(fee.value)}元，实际到账 ${formatAmount(actualAmount.value)}元。` 
  });
  if (!res.confirm) return;
  
  submitting.value = true;
  const key = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  try {
    const result = await withdraw(amountMinor.value, key);
    if (result.status === "FAILED") {
      throw new Error(result.failReason || "提现转账失败");
    }
    uni.showToast({ title: result.status === "SUCCESS" ? "提现成功" : "提现申请已提交", icon: "success" });
    setTimeout(() => {
      const pages = getCurrentPages();
      if (pages.length > 1) {
        uni.navigateBack();
      } else {
        uni.redirectTo({ url: "/pages/wallet/index" });
      }
    }, 1500);
  } catch (e: any) {
    uni.showToast({ title: e.message || "提现失败", icon: "none" });
  } finally { 
    submitting.value = false; 
  }
}

onLoad(async () => {
  try { 
    const w = await getWallet(); 
    available.value = w.balanceMinor; 
  } catch { 
    uni.showToast({ title: "加载钱包余额失败", icon: "none" }); 
  }
});

function goBack() {
  uni.navigateBack({ delta: 1 });
}
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background-color: #F5F6F8;
  position: relative;
}

.bg-header {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 400rpx;
  background: linear-gradient(180deg, #0a4bfe 0%, #3b82f6 60%, #F5F6F8 100%);
  z-index: 0;
}

.nav-bar {
  position: fixed;
  top: 0; left: 0; right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28rpx;
  z-index: 100;
}
.back-btn {
  width: 56rpx; height: 56rpx;
  display: flex; align-items: center; justify-content: flex-start;
}
.back-icon { width: 40rpx; height: 40rpx; }
.nav-title { font-size: 34rpx; font-weight: 500; color: #fff; }
.nav-placeholder { width: 56rpx; }

.content {
  position: absolute;
  left: 0; right: 0; bottom: 0;
  z-index: 10;
}

/* 余额区域 */
.balance-section {
  padding: 20rpx 40rpx 40rpx;
  color: #fff;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.balance-label {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}
.balance-value-row {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}
.balance-symbol {
  font-size: 36rpx;
  font-weight: 600;
}
.balance-value {
  font-size: 72rpx;
  font-weight: 700;
  line-height: 1;
}

/* 提现卡片 */
.withdraw-card {
  background: #fff;
  border-radius: 24rpx;
  margin: 0 32rpx;
  padding: 40rpx 32rpx;
  box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.03);
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #111827;
  margin-bottom: 24rpx;
  display: block;
}

/* 输入框 */
.input-row {
  display: flex;
  align-items: center;
  border-bottom: 1rpx solid #f3f4f6;
  padding-bottom: 20rpx;
  margin-bottom: 24rpx;
}
.currency-symbol {
  font-size: 48rpx;
  font-weight: 600;
  color: #111827;
  margin-right: 16rpx;
}
.amount-input {
  flex: 1;
  font-size: 56rpx;
  font-weight: 700;
  color: #111827;
  height: 80rpx;
}
.input-placeholder {
  font-size: 32rpx;
  font-weight: 400;
  color: #9ca3af;
}
.withdraw-all-btn {
  font-size: 28rpx;
  color: #0a4bfe;
  font-weight: 500;
  padding: 10rpx 0 10rpx 20rpx;
}

/* 手续费信息 */
.fee-info {
  background: #f8fafc;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  margin-bottom: 40rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.fee-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.fee-label {
  font-size: 24rpx;
  color: #6b7280;
}
.fee-value {
  font-size: 24rpx;
  color: #374151;
  font-weight: 500;
}
.fee-value.actual {
  color: #ea580c;
  font-weight: 700;
}

/* 提现方式 */
.method-section {
  margin-top: 40rpx;
}
.method-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
.method-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  border-radius: 16rpx;
  border: 2rpx solid transparent;
  background: #f9fafb;
  transition: all 0.2s;
}
.method-item.active {
  background: #eef2ff;
  border-color: #c7d2fe;
}
.method-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}
.method-icon {
  width: 48rpx;
  height: 48rpx;
}
.method-text {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}
.method-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #111827;
}
.method-desc {
  font-size: 22rpx;
  color: #6b7280;
}
.check-icon {
  width: 36rpx;
  height: 36rpx;
}

/* 提交按钮 */
.submit-btn {
  margin: 60rpx 32rpx 40rpx;
  background: #0a4bfe;
  color: #fff;
  border-radius: 999rpx;
  height: 96rpx;
  line-height: 96rpx;
  font-size: 32rpx;
  font-weight: 600;
  box-shadow: 0 8rpx 24rpx rgba(10, 75, 254, 0.25);
}
.submit-btn::after {
  display: none;
}
.submit-btn.disabled {
  background: #93b5ff;
  box-shadow: none;
  color: rgba(255, 255, 255, 0.8);
}

/* 提现说明 */
.rules-section {
  padding: 0 40rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.rules-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #4b5563;
  margin-bottom: 8rpx;
}
.rule-text {
  font-size: 24rpx;
  color: #9ca3af;
  line-height: 1.5;
}

.bottom-placeholder {
  height: 80rpx;
}
</style>
