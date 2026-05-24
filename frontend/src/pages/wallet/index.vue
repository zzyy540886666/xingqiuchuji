<template>
  <view class="page">
    <view class="balance-card">
      <text class="balance-label">账户余额（元）</text>
      <text class="balance-amount">{{ formatAmount(wallet.balanceMinor) }}</text>
      <view v-if="wallet.frozenMinor" class="frozen">
        <text>冻结金额：{{ formatAmount(wallet.frozenMinor) }}</text>
      </view>
      <view class="withdraw-btn" @tap="goWithdraw">提现</view>
    </view>

    <view class="section">
      <text class="section-title">收支明细</text>
      <Skeleton v-if="loading && ledger.length === 0" :rows="4" />
      <ErrorRetry v-else-if="error && ledger.length === 0" @retry="reload" />
      <EmptyState v-else-if="!loading && ledger.length === 0" text="暂无记录" />
      <view v-else class="ledger-list">
        <view v-for="item in ledger" :key="item.id" class="ledger-item">
          <view class="ledger-left">
            <text class="ledger-desc">{{ item.description }}</text>
            <text class="ledger-time">{{ item.createdAt }}</text>
          </view>
          <text class="ledger-amount" :class="item.type === 'CREDIT' || item.type === 'UNFREEZE' ? 'income' : 'expense'">
            {{ item.type === 'CREDIT' || item.type === 'UNFREEZE' ? "+" : "-" }}{{ formatAmount(item.amountMinor) }}
          </text>
        </view>
      </view>
      <view v-if="hasMore" class="load-more" @tap="fetchLedger">加载更多</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import Skeleton from "../../components/Skeleton.vue";
import EmptyState from "../../components/EmptyState.vue";
import ErrorRetry from "../../components/ErrorRetry.vue";
import { formatAmount } from "../../utils/format";
import { getWallet, getLedger, type WalletInfo, type LedgerEntry } from "../../services/wallet";

const wallet = ref<WalletInfo>({ balanceMinor: 0, frozenMinor: 0 });
const ledger = ref<LedgerEntry[]>([]);
const loading = ref(false);
const error = ref(false);
const nextCursor = ref(1);
const hasMore = ref(false);

async function fetchWallet() {
  try { wallet.value = await getWallet(); } catch { error.value = true; }
}

async function fetchLedger() {
  loading.value = true;
  error.value = false;
  try {
    const res = await getLedger({ cursor: nextCursor.value, limit: 20 });
    ledger.value.push(...res.items);
    hasMore.value = ledger.value.length < res.total;
    nextCursor.value += 1;
  } catch { error.value = true; } finally { loading.value = false; }
}

function goWithdraw() { uni.navigateTo({ url: "/pages/wallet/withdraw" }); }

function reload() { ledger.value = []; nextCursor.value = 1; fetchWallet(); fetchLedger(); }

onShow(() => reload());
</script>

<style scoped lang="scss">
.balance-card { background: linear-gradient(135deg, #0a4bfe, #3b82f6); margin: 28rpx; border-radius: 24rpx; padding: 48rpx 36rpx; color: #fff; }
.balance-label { font-size: 24rpx; opacity: 0.8; display: block; }
.balance-amount { font-size: 56rpx; font-weight: 800; display: block; margin-top: 12rpx; }
.frozen { font-size: 22rpx; opacity: 0.7; margin-top: 12rpx; }
.withdraw-btn { margin-top: 32rpx; width: 160rpx; height: 60rpx; border-radius: 999rpx; background: rgba(255,255,255,0.2); display: flex; align-items: center; justify-content: center; font-size: 26rpx; }
.section { background: #fff; margin: 0 28rpx; border-radius: 20rpx; padding: 28rpx; }
.section-title { font-size: 30rpx; font-weight: 700; display: block; margin-bottom: 24rpx; }
.ledger-item { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 0; border-bottom: 1rpx solid #f9fafb; }
.ledger-left { flex: 1; }
.ledger-desc { display: block; font-size: 26rpx; color: #111827; }
.ledger-time { display: block; font-size: 22rpx; color: #9ca3af; margin-top: 6rpx; }
.ledger-amount { font-size: 28rpx; font-weight: 700; }
.ledger-amount.income { color: #10b981; }
.ledger-amount.expense { color: #111827; }
.load-more { text-align: center; padding: 24rpx; font-size: 24rpx; color: #0a4bfe; }
</style>
