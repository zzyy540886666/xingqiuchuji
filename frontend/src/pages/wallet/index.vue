<template>
  <view class="page">
    <!-- 自定义导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="back-btn" @tap="goBack">
        <image class="back-icon" src="/static/icons/back.svg" mode="aspectFit" />
      </view>
      <text class="nav-title">光年钱包</text>
      <view class="nav-actions">
        <image class="nav-icon" src="/static/icons/more.svg" mode="aspectFit" />
        <image
          class="nav-icon"
          :src="hideBalance ? '/static/icons/eye-off.svg' : '/static/icons/eye.svg'"
          mode="aspectFit"
          @tap="toggleHide"
        />
      </view>
    </view>

    <template v-if="loading && ledger.length === 0">
      <Skeleton variant="wallet" />
    </template>
    <template v-else>

    <!-- 余额卡片 -->
    <view class="balance-card">
      <view class="balance-header">
        <view class="balance-title-row">
          <text class="balance-label">光年币余额</text>
          <image
            class="eye-icon"
            :src="hideBalance ? '/static/icons/eye-off.svg' : '/static/icons/eye.svg'"
            mode="aspectFit"
            @tap="toggleHide"
          />
        </view>
        <text class="balance-amount">
          {{ hideBalance ? '****' : formatAmount(wallet.balanceMinor).replace('¥', '') }}
        </text>
        <text class="balance-sub">
          {{ hideBalance ? '****' : formatAmount(wallet.balanceMinor) }} 可用余额
        </text>
      </view>

      <view class="balance-decor">
        <view class="decor-ring"></view>
        <view class="withdraw-btn-top" @tap="goWithdraw">提现</view>
      </view>

      <view class="record-link" @tap="goWithdrawRecords">
        <text>充值 / 兑换记录</text>
        <text class="arrow">></text>
      </view>

      <view class="stats-row">
        <view class="stat-item">
          <text class="stat-label">累计收益</text>
          <text class="stat-value">{{ hideBalance ? '****' : formatAmount(totalEarnings) }}</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-label">待结算</text>
          <text class="stat-value">{{ hideBalance ? '****' : formatAmount(pendingAmount) }}</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <view class="stat-label-row">
            <text class="stat-label">冻结金额</text>
            <text class="info-mark" @tap="showFrozenTip">i</text>
          </view>
          <text class="stat-value">{{ hideBalance ? '****' : formatAmount(wallet.frozenMinor) }}</text>
        </view>
      </view>
    </view>

    <!-- 筛选标签 -->
    <view class="filter-bar">
      <view
        v-for="tab in typeTabs"
        :key="tab.value"
        class="filter-tab"
        :class="{ active: currentType === tab.value }"
        @tap="currentType = tab.value"
      >
        {{ tab.label }}
      </view>
    </view>

    <!-- 交易列表 -->
    <view class="ledger-card">
      <ErrorRetry v-if="error && ledger.length === 0" @retry="reload" />
      <EmptyState v-else-if="!loading && filteredLedger.length === 0" text="暂无记录" />
      <view v-else class="ledger-list">
        <view
          v-for="item in filteredLedger"
          :key="item.id"
          class="ledger-item"
          @tap="showDetail(item)"
        >
          <view class="ledger-icon" :style="{ background: iconMeta(item.refType).bg }">
            <text class="icon-text" :style="{ color: iconMeta(item.refType).color }">
              {{ iconMeta(item.refType).icon }}
            </text>
          </view>
          <view class="ledger-main">
            <view class="ledger-top">
              <text class="ledger-title">{{ entryTitle(item) }}</text>
              <text class="ledger-amount" :class="amountClass(item)">
                {{ amountPrefix(item) }}{{ hideBalance ? '****' : formatAmount(item.amountMinor) }}
              </text>
            </view>
            <text class="ledger-meta">{{ entryMeta(item) }}</text>
            <view class="ledger-bottom">
              <text class="ledger-time">{{ item.createdAt?.slice(0, 16).replace('T', ' ') }}</text>
              <text class="ledger-status" :class="statusClass(item)">{{ entryStatus(item) }}</text>
            </view>
          </view>
        </view>
      </view>
      <view v-if="hasMore" class="load-more" @tap="fetchLedger">加载更多</view>
    </view>

    <!-- 资金安全保障 -->
    <view class="safety-bar">
      <view class="safety-left">
        <view class="safety-icon">
          <view class="shield-body"></view>
        </view>
        <view class="safety-text">
          <text class="safety-title">资金安全保障中</text>
          <text class="safety-desc">星球科技银行级加密保护，资金安全无忧</text>
        </view>
      </view>
      <view class="safety-rules">
        <text class="rule-line">· 提现规则：单笔最低10元</text>
        <text class="rule-line">· 手续费：提现金额的 0.5%</text>
        <text class="rule-line">· 到账时间：1-3 个工作日</text>
      </view>
    </view>

    <!-- 交易详情弹窗 -->
    <view v-if="selectedEntry" class="modal-mask" @tap="selectedEntry = null">
      <view class="modal-card" @tap.stop>
        <text class="modal-title">交易详情</text>
        <view class="modal-divider"></view>
        <view class="modal-row">
          <text class="modal-label">类型</text>
          <text class="modal-value">{{ sourceLabel(selectedEntry.refType ?? '') }}</text>
        </view>
        <view class="modal-row">
          <text class="modal-label">金额</text>
          <text class="modal-value amount">
            {{ selectedEntry.type === 'CREDIT' || selectedEntry.type === 'UNFREEZE' ? '+' : '-' }}
            {{ formatAmount(selectedEntry.amountMinor) }}
          </text>
        </view>
        <view class="modal-row">
          <text class="modal-label">余额</text>
          <text class="modal-value">{{ formatAmount(selectedEntry.balanceAfterMinor) }}</text>
        </view>
        <view class="modal-row">
          <text class="modal-label">时间</text>
          <text class="modal-value">{{ selectedEntry.createdAt }}</text>
        </view>
        <view class="modal-row">
          <text class="modal-label">说明</text>
          <text class="modal-value">{{ selectedEntry.description }}</text>
        </view>
        <view class="modal-close" @tap="selectedEntry = null">关闭</view>
      </view>
    </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import Skeleton from "../../components/PageSkeleton.vue";
import EmptyState from "../../components/EmptyState.vue";
import ErrorRetry from "../../components/ErrorRetry.vue";
import { formatAmount } from "../../utils/format";
import { getWallet, getLedger, type WalletInfo, type LedgerEntry } from "../../services/wallet";
import { useSessionStore } from "../../stores/session";
import { createLatestTask } from "../../utils/latestTask";

const systemInfo = uni.getWindowInfo();
const statusBarHeight = ref(systemInfo.statusBarHeight || 0);

const session = useSessionStore();

const wallet = ref<WalletInfo>({ balanceMinor: 0, frozenMinor: 0 });
const ledger = ref<LedgerEntry[]>([]);
const loading = ref(false);
const error = ref(false);
const nextCursor = ref(1);
const hasMore = ref(false);
const currentType = ref("");
const selectedEntry = ref<LedgerEntry | null>(null);
const hideBalance = ref(false);
const ledgerTask = createLatestTask();

const typeTabs = [
  { label: "全部", value: "" },
  { label: "订单", value: "ORDER_DISCOUNT" },
  { label: "佣金", value: "COMMISSION" },
  { label: "积分兑换", value: "ACTIVITY" },
  { label: "提现", value: "WITHDRAW" },
];

const filteredLedger = computed(() => {
  if (!currentType.value) {
    return ledger.value;
  }
  return ledger.value.filter((entry) => entry.refType === currentType.value);
});

const totalEarnings = computed(() => {
  return ledger.value
    .filter((entry) => entry.type === "CREDIT" || entry.type === "UNFREEZE")
    .reduce((sum, entry) => sum + entry.amountMinor, 0);
});

const pendingAmount = computed(() => 0);

function toggleHide() {
  hideBalance.value = !hideBalance.value;
}

function iconMeta(refType?: string) {
  const map: Record<string, { icon: string; bg: string; color: string }> = {
    ORDER_DISCOUNT: { icon: "券", bg: "#e0f2fe", color: "#0284c7" },
    COMMISSION: { icon: "佣", bg: "#dcfce7", color: "#16a34a" },
    ACTIVITY: { icon: "积", bg: "#f3e8ff", color: "#9333ea" },
    WITHDRAW: { icon: "提", bg: "#fff7ed", color: "#ea580c" },
    RECHARGE: { icon: "充", bg: "#e0e7ff", color: "#4f46e5" },
  };
  return map[refType || ""] || { icon: "账", bg: "#f3f4f6", color: "#6b7280" };
}

function entryTitle(item: LedgerEntry): string {
  const map: Record<string, string> = {
    ORDER_DISCOUNT: "订单收益",
    COMMISSION: "分销佣金",
    ACTIVITY: "积分兑换",
    WITHDRAW: "提现到微信",
    RECHARGE: "账户充值",
  };
  return map[item.refType || ""] || item.description || "系统操作";
}

function entryMeta(item: LedgerEntry): string {
  if (item.refType === "WITHDRAW") {
    return `提现金额 ${formatAmount(item.amountMinor)}`;
  }
  if ((item.refType === "ORDER_DISCOUNT" || item.refType === "COMMISSION") && item.refId) {
    return `订单号：${item.refId}`;
  }
  if (item.refType === "ACTIVITY") {
    return `兑换 ${Math.round(item.amountMinor / 100)} 积分`;
  }
  return item.description || "";
}

function entryStatus(item: LedgerEntry): string {
  if (item.type === "CREDIT" || item.type === "UNFREEZE") {
    return "已到账";
  }
  if (item.type === "FREEZE") {
    return "冻结中";
  }
  if (item.refType === "WITHDRAW") {
    return "已完成";
  }
  return "已完成";
}

function statusClass(item: LedgerEntry): string {
  const status = entryStatus(item);
  if (status === "已到账") {
    return "status-done";
  }
  if (status === "处理中" || status === "冻结中") {
    return "status-pending";
  }
  if (status === "失败") {
    return "status-fail";
  }
  return "status-normal";
}

function amountClass(item: LedgerEntry): string {
  return item.type === "CREDIT" || item.type === "UNFREEZE" ? "amount-income" : "amount-expense";
}

function amountPrefix(item: LedgerEntry): string {
  return item.type === "CREDIT" || item.type === "UNFREEZE" ? "+" : "-";
}

function sourceLabel(refType?: string): string {
  const map: Record<string, string> = {
    ORDER_DISCOUNT: "订单抵扣",
    COMMISSION: "佣金收入",
    ACTIVITY: "活动奖励",
    WITHDRAW: "提现",
    RECHARGE: "充值",
  };
  return map[refType || ""] || refType || "系统操作";
}

function showFrozenTip() {
  uni.showToast({ title: "冻结金额将在交易完成后释放", icon: "none" });
}

async function fetchWallet() {
  try {
    wallet.value = await getWallet();
  } catch {
    error.value = true;
  }
}

async function fetchLedger() {
  if (loading.value || (!hasMore.value && ledger.value.length > 0)) return;
  const targetCursor = nextCursor.value;
  const requestId = ledgerTask.begin();
  loading.value = true;
  error.value = false;
  try {
    const res = await getLedger({ cursor: targetCursor, limit: 20 });
    if (!ledgerTask.isCurrent(requestId)) return;
    ledger.value.push(...res.items);
    hasMore.value = ledger.value.length < res.total;
    nextCursor.value = targetCursor + 1;
  } catch {
    if (ledgerTask.isCurrent(requestId)) error.value = true;
  } finally {
    if (ledgerTask.isCurrent(requestId)) loading.value = false;
  }
}

function goWithdraw() {
  uni.navigateTo({ url: "/pages/wallet/withdraw" });
}

function goWithdrawRecords() {
  uni.navigateTo({ url: "/pages/wallet/withdraw?showHistory=1" });
}

function showDetail(entry: LedgerEntry) {
  selectedEntry.value = entry;
}

function goBack() {
  uni.navigateBack({ delta: 1 });
}

function reload() {
  if (!session.token) {
    uni.showToast({ title: "请先登录", icon: "none" });
    setTimeout(() => uni.navigateBack(), 1200);
    return;
  }
  ledger.value = [];
  nextCursor.value = 1;
  hasMore.value = true;
  loading.value = false;
  ledgerTask.cancel();
  fetchWallet();
  fetchLedger();
}

onShow(() => reload());
</script>

<style scoped lang="scss">
.page {
  background: #f5f6fa;
  min-height: 100vh;
  padding-bottom: 40rpx;
}

/* 导航栏 */
.nav-bar {
  height: 88rpx;
  padding: 0 28rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
}

.back-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  width: 32rpx;
  height: 32rpx;
  display: block;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #111827;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.nav-icon {
  width: 36rpx;
  height: 36rpx;
  display: block;
}

/* 余额卡片 */
.balance-card {
  position: relative;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  margin: 24rpx 28rpx 0;
  border-radius: 28rpx;
  padding: 40rpx 36rpx 32rpx;
  color: #fff;
  overflow: hidden;
}

.balance-header {
  position: relative;
  z-index: 2;
}

.balance-title-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.balance-label {
  font-size: 24rpx;
  opacity: 0.85;
}

.eye-icon {
  width: 28rpx;
  height: 28rpx;
  opacity: 0.9;
}

.balance-amount {
  display: block;
  font-size: 64rpx;
  font-weight: 800;
  margin-top: 16rpx;
  letter-spacing: -1rpx;
}

.balance-sub {
  display: block;
  font-size: 24rpx;
  opacity: 0.75;
  margin-top: 10rpx;
}

.balance-decor {
  position: absolute;
  top: 36rpx;
  right: 36rpx;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
}

.decor-ring {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.15);
  background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.25), transparent 60%);
}

.withdraw-btn-top {
  background: #fff;
  color: #2563eb;
  font-size: 26rpx;
  font-weight: 600;
  padding: 10rpx 36rpx;
  border-radius: 999rpx;
}

.record-link {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 28rpx;
  font-size: 24rpx;
  opacity: 0.8;
  position: relative;
  z-index: 2;
}

.record-link .arrow {
  font-size: 22rpx;
  opacity: 0.7;
}

.stats-row {
  display: flex;
  align-items: center;
  margin-top: 28rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.15);
  position: relative;
  z-index: 2;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.stat-label {
  font-size: 22rpx;
  opacity: 0.75;
}

.stat-label-row {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.info-mark {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  border: 1rpx solid rgba(255, 255, 255, 0.5);
  font-size: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0.8;
}

.stat-value {
  font-size: 30rpx;
  font-weight: 700;
}

.stat-divider {
  width: 1rpx;
  height: 40rpx;
  background: rgba(255, 255, 255, 0.2);
}

/* 筛选标签 */
.filter-bar {
  display: flex;
  gap: 16rpx;
  margin: 24rpx 28rpx 0;
}

.filter-tab {
  font-size: 26rpx;
  color: #6b7280;
  padding: 10rpx 28rpx;
  border-radius: 999rpx;
  background: #fff;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.filter-tab.active {
  background: #2563eb;
  color: #fff;
  font-weight: 600;
}

/* 交易列表卡片 */
.ledger-card {
  background: #fff;
  margin: 20rpx 28rpx 0;
  border-radius: 24rpx;
  padding: 8rpx 28rpx 20rpx;
}

.ledger-item {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f3f4f6;
}

.ledger-item:last-child {
  border-bottom: none;
}

.ledger-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-text {
  font-size: 26rpx;
  font-weight: 700;
}

.ledger-main {
  flex: 1;
  min-width: 0;
}

.ledger-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.ledger-title {
  font-size: 28rpx;
  color: #111827;
  font-weight: 600;
}

.ledger-amount {
  font-size: 28rpx;
  font-weight: 700;
}

.amount-income {
  color: #2563eb;
}

.amount-expense {
  color: #111827;
}

.ledger-meta {
  display: block;
  font-size: 22rpx;
  color: #9ca3af;
  margin-top: 8rpx;
}

.ledger-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8rpx;
}

.ledger-time {
  font-size: 22rpx;
  color: #9ca3af;
}

.ledger-status {
  font-size: 22rpx;
}

.status-done {
  color: #2563eb;
}

.status-pending {
  color: #f59e0b;
}

.status-fail {
  color: #ef4444;
}

.status-normal {
  color: #6b7280;
}

.load-more {
  text-align: center;
  padding: 24rpx;
  font-size: 24rpx;
  color: #2563eb;
}

/* 资金安全保障 */
.safety-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  margin: 20rpx 28rpx 0;
  border-radius: 24rpx;
  padding: 28rpx;
}

.safety-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.safety-icon {
  position: relative;
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.shield-body {
  width: 40rpx;
  height: 48rpx;
  background: #dbeafe;
  border-radius: 0 0 20rpx 20rpx;
  position: relative;
}

.shield-body::before {
  content: "";
  position: absolute;
  top: -20rpx;
  left: 0;
  width: 40rpx;
  height: 40rpx;
  background: #dbeafe;
  border-radius: 20rpx 20rpx 0 0;
}

.safety-text {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.safety-title {
  font-size: 26rpx;
  font-weight: 700;
  color: #111827;
}

.safety-desc {
  font-size: 22rpx;
  color: #9ca3af;
}

.safety-rules {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.rule-line {
  font-size: 20rpx;
  color: #9ca3af;
}

/* 弹窗 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
  padding: 60rpx;
}

.modal-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 36rpx;
  width: 100%;
}

.modal-title {
  font-size: 30rpx;
  font-weight: 700;
  display: block;
}

.modal-divider {
  height: 1rpx;
  background: #f3f4f6;
  margin: 20rpx 0;
}

.modal-row {
  display: flex;
  justify-content: space-between;
  padding: 12rpx 0;
}

.modal-label {
  font-size: 26rpx;
  color: #6b7280;
}

.modal-value {
  font-size: 26rpx;
  color: #111827;
}

.modal-value.amount {
  font-weight: 700;
}

.modal-close {
  margin-top: 24rpx;
  height: 72rpx;
  border-radius: 999rpx;
  background: #f3f4f6;
  color: #374151;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
}
</style>
