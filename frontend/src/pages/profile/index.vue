<template>
  <view class="page profile-page safe-bottom">
    <view class="profile-bg"><view class="curve"></view></view>
    <view class="profile-content">
      <StatusBar light time="9:43" />
      <view class="profile-tools">
        <view class="tool" @tap="openSettings"><image class="tool-icon" src="/static/icons/gear.svg" mode="aspectFit" /></view>
        <view class="tool" @tap="handleFunction('/pages/im/conversations')"><image class="tool-icon" src="/static/icons/bell.svg" mode="aspectFit" /></view>
      </view>

      <view class="user-card">
        <image class="user-avatar" :src="user?.avatarUrl || '/static/images/avatar.png'" mode="aspectFill" />
        <view class="user-info">
          <view class="user-name-row">
            <text class="user-name">{{ user?.nickname || '登录星球出机' }}</text>
            <text v-if="user" class="level-badge">Lv.{{ membership?.level || 0 }} 会员</text>
          </view>
          <text v-if="user" class="invite" @tap="copyInvite">ID: {{ user.id }} · 邀请码 {{ distribution?.inviteCode || '-' }}</text>
          <view v-else class="login-button" @tap="wechatLogin">微信一键登录</view>
        </view>
      </view>

      <view v-if="user" class="member-card">
        <view class="member-top">
          <view class="member-name"><image class="star" src="/static/icons/member.svg" mode="aspectFit" /><text>星球会员</text><text>有效期至：{{ membership?.planetCardExpiresAt?.slice(0, 10) || '未开通' }}</text></view>
          <text class="member-link" @tap="go('/pages/member/index')">查看会员权益</text>
        </view>
        <view class="progress-row">
          <view class="progress-main">
            <view class="progress-text"><text>Lv.{{ membership?.level || 0 }} 当前等级</text><text>累计消费 {{ formatAmount(membership?.lifetimeSpendMinor || 0) }}</text></view>
            <view class="progress-track"><view class="progress-bar" :style="{ width: growthPercent + '%' }"></view></view>
            <text class="progress-tip">{{ growthTip }}</text>
          </view>
          <view class="growth" @tap="go('/pages/member/index')">成长中心</view>
        </view>
      </view>
    </view>

    <Skeleton v-if="loading && user" :rows="4" />
    <ErrorRetry v-else-if="error && user" @retry="loadProfile" />
    <view v-if="user && !error" class="stats card">
      <view v-for="item in stats" :key="item.label" class="stat-item">
        <text>{{ item.value }}</text>
        <text>{{ item.label }}</text>
      </view>
    </view>

    <view class="function-grid card">
      <view v-for="item in functions" :key="item.label" class="function-item" @tap="handleFunction(item.path)">
        <image class="function-icon" :src="item.icon" mode="aspectFit" />
        <text>{{ item.label }}</text>
      </view>
    </view>

    <view v-if="user && !error" class="asset-card card">
      <view class="section-title-row">
        <text class="section-title">资产概览</text>
        <text class="section-more" @tap="go('/pages/wallet/index')">流水入口</text>
      </view>
      <view class="asset-grid">
        <view v-for="asset in assets" :key="asset.label" class="asset-item">
          <text>{{ asset.value }}</text>
          <text>{{ asset.label }}</text>
        </view>
      </view>
    </view>

    <view v-if="user && !error" class="robots-card card">
      <view class="section-title-row">
        <text class="section-title">我的房产资产</text>
        <text class="section-more" @tap="go('/pages/asset/list')">详情</text>
      </view>
      <view class="robot-grid">
        <view v-for="robot in robots" :key="robot.label" class="robot-tile" :class="robot.className" @tap="go('/pages/asset/list')">
          <view class="robot-top"><text>{{ robot.label }}</text><text>{{ robot.count }}台</text></view>
          <image v-if="robot.image" class="robot-img" :src="robot.image" mode="aspectFill" />
          <view v-else class="robot-img"></view>
        </view>
      </view>
    </view>

    <BottomNav current="profile" />
  </view>
</template>

<script setup lang="ts">
import BottomNav from "../../components/BottomNav.vue";
import StatusBar from "../../components/StatusBar.vue";
import Skeleton from "../../components/Skeleton.vue";
import ErrorRetry from "../../components/ErrorRetry.vue";
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { useSessionStore } from "../../stores/session";
import { getMembership, type MembershipSummary } from "../../services/member";
import { getWallet, type WalletInfo } from "../../services/wallet";
import { getAssets, getAssetDashboard, type Asset } from "../../services/asset";
import { getNotifications } from "../../services/notification";
import { getDistributionMe, type DistributionInfo } from "../../services/distribution";
import { formatAmount } from "../../utils/format";

const session = useSessionStore();
const user = computed(() => session.user);
const membership = ref<MembershipSummary | null>(null);
const wallet = ref<WalletInfo>({ balanceMinor: 0, frozenMinor: 0 });
const userAssets = ref<(Asset & { totalRevenueMinor: number })[]>([]);
const distribution = ref<DistributionInfo | null>(null);
const unreadCount = ref(0);
const loading = ref(false);
const error = ref(false);

const stats = computed(() => [
  { value: `Lv.${membership.value?.level || 0}`, label: "会员等级" },
  { value: formatAmount(wallet.value.balanceMinor), label: "钱包余额" },
  { value: `${userAssets.value.length}台`, label: "我的资产" },
  { value: `${unreadCount.value}条`, label: "未读消息" },
]);
const functions = [
  { label: "我的订单", path: "/pages/order/list", icon: "/static/icons/order.svg" },
  { label: "钱包", path: "/pages/wallet/index", icon: "/static/icons/wallet.svg" },
  { label: "会员中心", path: "/pages/member/index", icon: "/static/icons/member.svg" },
  { label: "我的资产", path: "/pages/asset/list", icon: "/static/icons/asset.svg" },
  { label: "托管收益", path: "/pages/asset/list", icon: "/static/icons/income.svg" },
  { label: "分销中心", path: "/pages/distribution/home", icon: "/static/icons/distribution.svg" },
  { label: "我的报修", path: "/pages/repair/mine", icon: "/static/icons/repair.svg" },
  { label: "消息", path: "/pages/im/conversations", icon: "/static/icons/message.svg" },
];
const totalRevenueMinor = computed(() => userAssets.value.reduce((total, asset) => total + asset.totalRevenueMinor, 0));
const assets = computed(() => [
  { value: formatAmount(wallet.value.balanceMinor), label: "可用余额" },
  { value: formatAmount(wallet.value.frozenMinor), label: "冻结金额" },
  { value: formatAmount(totalRevenueMinor.value), label: "托管收益" },
  { value: `${userAssets.value.length}台`, label: "设备总数" },
]);
const robots = computed(() => [
  { key: "IDLE", label: "空闲", className: "idle" },
  { key: "TRUSTEED", label: "托管中", className: "rented" },
  { key: "MAINTENANCE", label: "维护中", className: "maintain" },
].map((state) => {
  const matching = userAssets.value.filter((asset) => asset.status === state.key);
  return { ...state, count: String(matching.length), image: matching[0]?.imageUrl || "" };
}));
const growthPercent = computed(() => Math.min(100, ((membership.value?.lifetimeSpendMinor || 0) / 200000) * 100));
const growthTip = computed(() => {
  const spent = membership.value?.lifetimeSpendMinor || 0;
  if (spent < 10000) return `再消费 ${formatAmount(10000 - spent)} 晋升 Lv.1`;
  if (spent < 50000) return `再消费 ${formatAmount(50000 - spent)} 晋升 Lv.2`;
  if (spent < 200000) return `再消费 ${formatAmount(200000 - spent)} 晋升 Lv.3`;
  return "已达到最高成长等级";
});

async function loadProfile() {
  if (!session.token) return;
  loading.value = true;
  error.value = false;
  try {
    if (!session.user) await session.fetchUser();
    const [membershipData, walletData, rawAssets, noticePage, distributionData] = await Promise.all([
      getMembership(), getWallet(), getAssets(), getNotifications({ page: 1 }), getDistributionMe(),
    ]);
    membership.value = membershipData;
    wallet.value = walletData;
    distribution.value = distributionData;
    unreadCount.value = noticePage.items.filter((item) => !item.isRead).length;
    userAssets.value = await Promise.all(rawAssets.map(async (asset) => {
      const dashboard = await getAssetDashboard(asset.id);
      return { ...asset, totalRevenueMinor: dashboard.totalRevenueMinor };
    }));
  } catch {
    error.value = true;
  } finally {
    loading.value = false;
  }
}

async function wechatLogin() {
  try {
    await session.loginByWechat();
    await loadProfile();
  } catch (reason: any) {
    uni.showToast({ title: reason.message || "微信登录失败", icon: "none" });
  }
}

onShow(() => loadProfile());

function go(url: string) {
  uni.navigateTo({ url });
}

function handleFunction(path: string) {
  if (!session.token) {
    wechatLogin();
    return;
  }
  go(path);
}

function copyInvite() {
  if (!distribution.value?.inviteCode) return;
  uni.setClipboardData({ data: distribution.value.inviteCode, success: () => uni.showToast({ title: "邀请码已复制", icon: "none" }) });
}

function openSettings() {
  uni.showActionSheet({
    itemList: ["会员权益", "消息中心", "我的报修"],
    success(result) {
      const paths = ["/pages/member/index", "/pages/im/conversations", "/pages/repair/mine"];
      go(paths[result.tapIndex]);
    },
  });
}
</script>

<style scoped lang="scss">
.profile-page {
  background: #f2f4f7;
  position: relative;
}

.profile-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 680rpx;
  background: linear-gradient(180deg, #1e6fff, #3a82ff, #5a9bff);
  overflow: hidden;
}

.curve {
  position: absolute;
  right: -70rpx;
  top: 160rpx;
  width: 360rpx;
  height: 220rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.25);
  border-radius: 50%;
  transform: rotate(-18deg);
}

.profile-content {
  position: relative;
  z-index: 1;
}

.profile-tools {
  height: 80rpx;
  padding: 0 30rpx;
  display: flex;
  justify-content: flex-end;
  gap: 18rpx;
}

.tool {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.tool-icon { width: 30rpx; height: 30rpx; display: block; }

.user-card {
  display: flex;
  align-items: center;
  gap: 22rpx;
  padding: 16rpx 38rpx 0;
}

.user-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.45);
  background: rgba(255, 255, 255, 0.2);
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.user-name {
  color: #fff;
  font-size: 36rpx;
  font-weight: 900;
}

.level-badge {
  color: #fff;
  background: linear-gradient(90deg, #ffb84d, #ff8a00);
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  font-weight: 800;
}

.invite {
  color: rgba(255, 255, 255, 0.86);
  font-size: 24rpx;
}

.login-button {
  display: inline-flex;
  align-items: center;
  height: 58rpx;
  padding: 0 28rpx;
  border-radius: 999rpx;
  background: #fff;
  color: #1677ff;
  font-size: 25rpx;
  font-weight: 800;
}

.member-card {
  margin: 32rpx 30rpx 0;
  padding: 26rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #e8f1ff, #f0f5ff, #dbe8ff);
  box-shadow: 0 16rpx 36rpx rgba(0, 0, 0, 0.12);
}

.member-top,
.member-name,
.progress-row,
.progress-text {
  display: flex;
  align-items: center;
}

.member-top {
  justify-content: space-between;
}

.member-name {
  gap: 10rpx;
}

.member-name text:nth-child(2) {
  font-size: 26rpx;
  font-weight: 900;
}

.member-name text:nth-child(3),
.member-link {
  color: #6b7280;
  font-size: 20rpx;
}

.member-link {
  color: #1e6fff;
}

.star {
  width: 28rpx;
  height: 28rpx;
  display: block;
}

.progress-row {
  gap: 20rpx;
  margin-top: 20rpx;
}

.progress-main {
  flex: 1;
}

.progress-text {
  justify-content: space-between;
  font-size: 20rpx;
  color: #6b7280;
  margin-bottom: 10rpx;
}

.progress-text text:first-child {
  color: #111827;
  font-weight: 800;
}

.progress-track {
  height: 12rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.7);
  overflow: hidden;
}

.progress-bar {
  width: 32%;
  height: 100%;
  background: linear-gradient(90deg, #ffb84d, #ff8a00);
}

.progress-tip {
  display: block;
  color: #9ca3af;
  font-size: 20rpx;
  margin-top: 8rpx;
}

.growth {
  height: 56rpx;
  padding: 0 26rpx;
  border-radius: 999rpx;
  background: linear-gradient(90deg, #ffb84d, #ff8a00);
  color: #fff;
  font-size: 22rpx;
  font-weight: 900;
  display: flex;
  align-items: center;
}

.stats,
.function-grid,
.asset-card,
.robots-card {
  position: relative;
  z-index: 2;
  margin: 24rpx 30rpx 0;
}

.stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  padding: 30rpx 0;
}

.stat-item,
.asset-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
}

.stat-item text:first-child,
.asset-item text:first-child {
  font-size: 32rpx;
  font-weight: 900;
  color: #111827;
}

.stat-item text:last-child,
.asset-item text:last-child {
  font-size: 22rpx;
  color: #9ca3af;
}

.function-grid {
  padding: 32rpx;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  row-gap: 34rpx;
}

.function-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  color: #333;
  font-size: 22rpx;
}

.function-icon {
  width: 88rpx;
  height: 88rpx;
  border-radius: 24rpx;
  background: #eef4ff;
  padding: 22rpx;
}

.asset-card,
.robots-card {
  padding: 28rpx;
}

.asset-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
}

.robot-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18rpx;
}

.robot-tile {
  border-radius: 20rpx;
  padding: 18rpx;
}

.robot-tile.idle { background: linear-gradient(135deg, #e8f1ff, #f0f5ff); }
.robot-tile.rented { background: linear-gradient(135deg, #fff7e6, #fffbe6); }
.robot-tile.maintain { background: linear-gradient(135deg, #f0fff4, #f6ffed); }

.robot-top {
  display: flex;
  justify-content: space-between;
  color: #4b5563;
  font-size: 22rpx;
  margin-bottom: 12rpx;
}

.robot-top text:last-child {
  color: #111827;
  font-weight: 900;
}

.robot-img {
  width: 100%;
  height: 140rpx;
  border-radius: 16rpx;
}
</style>
