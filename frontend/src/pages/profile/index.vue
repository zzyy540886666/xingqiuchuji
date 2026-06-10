<template>
  <view class="page profile-page safe-bottom">
    <template v-if="pageLoading">
      <Skeleton variant="profile" />
    </template>
    <template v-else>
      <view class="bg-blue-header"></view>

      <view class="content-container">
        <view class="user-section" :style="{ paddingTop: (statusBarHeight + 20) + 'px' }">
          <image class="avatar" :src="user?.avatarUrl || '/static/images/default-avatar.svg'" mode="aspectFill" />
          <view class="user-details">
            <view v-if="user" class="name-row">
              <text class="name">{{ user.nickname || '星球玩家' }}</text>
              <view class="level-badge">
                <text>Lv.{{ membership?.level || 0 }} 会员</text>
              </view>
            </view>
            <view v-else class="name-row" @tap="wechatLogin">
              <text class="name login-hint">登录 / 注册</text>
            </view>
            <view v-if="user" class="id-row" @tap="copyInvite">
              <text>ID: {{ user.id }}</text>
              <image class="copy-icon" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='rgba(255,255,255,0.7)' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Crect x='9' y='9' width='13' height='13' rx='2' ry='2'/%3E%3Cpath d='M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1'/%3E%3C/svg%3E" mode="aspectFit" />
            </view>
          </view>
          <view v-if="user" class="edit-arrow" @tap="goEditProfile">
            <text class="arrow-icon">></text>
          </view>
        </view>

        <view class="planet-card">
          <view class="planet-card-top">
            <view class="planet-card-title">
              <image class="planet-icon" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23FFFFFF' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Ccircle cx='12' cy='12' r='6'/%3E%3Cellipse cx='12' cy='12' rx='10' ry='3' transform='rotate(-30 12 12)'/%3E%3C/svg%3E" mode="aspectFit" />
              <text>星球卡 · 会员</text>
            </view>
            <view class="planet-card-right" @tap="go('/pages/membership-center/index')">
              <text>查看会员权益 ></text>
            </view>
          </view>
          <view class="planet-card-date">
            <text>{{ planetCardExpiryText }}</text>
          </view>
          <view class="planet-card-bottom">
            <view class="level-info">
              <text class="level-text">Lv.{{ membership?.level || 0 }}</text>
              <text class="level-sub">当前等级</text>
            </view>
            <view class="progress-info">
              <view class="progress-bar-wrap">
                <view class="progress-bar-inner" :style="{ width: levelProgressPercent + '%' }"></view>
              </view>
              <view class="progress-text-row">
                <text class="progress-desc">{{ levelProgressText }}</text>
                <text class="progress-num">{{ levelProgressCurrent }}/{{ levelProgressTarget }}</text>
              </view>
            </view>
            <view class="growth-btn" @tap="go('/pages/membership-center/index')">
              <text>成长中心</text>
            </view>
          </view>
        </view>

        <view class="privileges-card">
          <view class="privilege-item" v-for="(item, index) in privileges" :key="index">
            <image :src="item.icon" class="privilege-icon" mode="aspectFit" />
            <view class="privilege-value-wrap">
              <text class="privilege-value">{{ item.value }}</text>
              <text v-if="item.unit" class="privilege-unit">{{ item.unit }}</text>
            </view>
            <text class="privilege-label">{{ item.label }}</text>
          </view>
        </view>

        <view class="functions-card">
          <view class="function-item" v-for="(item, index) in functions" :key="index" @tap="handleFunction(item.path)">
            <image :src="item.icon" class="function-icon" mode="aspectFit" />
            <text class="function-label">{{ item.label }}</text>
          </view>
        </view>

        <view class="overview-card">
          <view class="card-header">
            <view class="card-title-wrap">
              <text class="card-title">资产概览</text>
              <image class="info-icon" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%2394A3B8' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Ccircle cx='12' cy='12' r='10'/%3E%3Cline x1='12' y1='16' x2='12' y2='12'/%3E%3Cline x1='12' y1='8' x2='12.01' y2='8'/%3E%3C/svg%3E" mode="aspectFit" />
            </view>
            <view class="card-more" @tap="go('/pages/wallet/index')">
              <text>进入钱包 ></text>
            </view>
          </view>
          <view class="overview-grid">
            <view class="overview-item">
              <text class="overview-value">{{ formatAmount(wallet.balanceMinor) }}</text>
              <text class="overview-label">光年币余额</text>
            </view>
            <view class="overview-item">
              <text class="overview-value">{{ formatAmount(totalRevenueMinor) }}</text>
              <text class="overview-label">累计收益</text>
            </view>
            <view class="overview-item">
              <text class="overview-value">{{ formatAmount(distribution?.overview?.pendingCommission || 0) }}</text>
              <text class="overview-label">待结算佣金</text>
            </view>
            <view class="overview-item">
              <view class="overview-value-wrap">
                <text class="overview-value">{{ userAssets.length }}</text>
                <text class="overview-unit"> 台</text>
              </view>
              <text class="overview-label">已购机器人</text>
            </view>
          </view>
        </view>

        <view class="clones-card">
          <view class="card-header">
            <text class="card-title">我的分享资产</text>
            <view class="card-more" @tap="go('/pages/asset/list')">
              <text>查看全部 ></text>
            </view>
          </view>
          <view class="clones-grid">
            <view class="clone-item idle" @tap="go('/pages/asset/list')">
              <view class="clone-info">
                <text class="clone-label">空闲</text>
                <text class="clone-count">{{ cloneStats.idle }} 台</text>
              </view>
              <image src="/static/images/Go2.png" class="clone-image" mode="aspectFit" />
            </view>
            <view class="clone-item renting" @tap="go('/pages/asset/list')">
              <view class="clone-info">
                <text class="clone-label">托管中</text>
                <text class="clone-count">{{ cloneStats.renting }} 台</text>
              </view>
              <image src="/static/images/G1-U2.png" class="clone-image" mode="aspectFit" />
            </view>
            <view class="clone-item maintenance" @tap="go('/pages/asset/list')">
              <view class="clone-info">
                <text class="clone-label">维护中</text>
                <text class="clone-count">{{ cloneStats.maintenance }} 台</text>
              </view>
              <image src="/static/images/G1-U2.png" class="clone-image" mode="aspectFit" />
            </view>
          </view>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { useSessionStore } from "../../stores/session";
import { getMembership, type MembershipSummary } from "../../services/member";
import { getWallet, type WalletInfo } from "../../services/wallet";
import { getAssetEarnings, getAssets, type Asset, type AssetEarnings } from "../../services/asset";
import { getDistributionMe, type DistributionInfo } from "../../services/distribution";
import { formatAmount } from "../../utils/format";
import { navigateToPage } from "../../utils/navigation";
import Skeleton from "../../components/PageSkeleton.vue";

const statusBarHeight = ref(0);
const session = useSessionStore();
const user = computed(() => session.user);
const membership = ref<MembershipSummary | null>(null);
const wallet = ref<WalletInfo>({ balanceMinor: 0, frozenMinor: 0 });
const distribution = ref<DistributionInfo | null>(null);
const userAssets = ref<(Asset & { totalRevenueMinor?: number })[]>([]);
const assetEarnings = ref<AssetEarnings | null>(null);
const pageLoading = ref(true);

const functions = [
  { label: "我的订单", path: "/pages/order/list", icon: "/static/icons/order.svg" },
  { label: "钱包", path: "/pages/wallet/index", icon: "/static/icons/wallet.svg" },
  { label: "会员中心", path: "/pages/membership-center/index", icon: "/static/icons/member.svg" },
  { label: "分享资产", path: "/pages/asset/list", icon: "/static/icons/asset.svg" },
  { label: "托管收益", path: "/pages/asset/earnings", icon: "/static/icons/income.svg" },
  { label: "分销中心", path: "/pages/distribution/home", icon: "/static/icons/distribution.svg" },
  { label: "邀请好友", path: "/pages/distribution/invite", icon: "/static/icons/share.svg" },
  { label: "客服设置", path: "/pages/im/conversations", icon: "/static/icons/service.svg" },
];

const privileges = computed(() => [
  { value: String(remainingBenefitCount("FREE_DEPOSIT")), unit: " 次", label: "剩余免押", icon: "/static/icons/check-blue.svg" },
  { value: String(remainingBenefitCount("MAINTENANCE")), unit: " 次", label: "维保次数", icon: "/static/icons/repair.svg" },
  { value: discountText.value, unit: "", label: "配件折扣", icon: "/static/icons/cat-activity.svg" },
  { value: formatAmount(wallet.value.balanceMinor), unit: "", label: "光年币余额", icon: "/static/icons/wallet.svg" },
]);

const totalRevenueMinor = computed(() => assetEarnings.value?.totalRevenueMinor || 0);
const cloneStats = computed(() => ({
  idle: userAssets.value.filter(a => a.status === "IDLE").length,
  renting: userAssets.value.filter(a => a.status === "TRUSTEED" || a.status === "RENTING").length,
  maintenance: userAssets.value.filter(a => a.status === "MAINTENANCE").length,
}));

const levelThresholds = [0, 10000, 50000, 200000];
const levelProgressTarget = computed(() => {
  const level = membership.value?.level ?? 0;
  return levelThresholds[Math.min(level + 1, levelThresholds.length - 1)] || levelThresholds[levelThresholds.length - 1];
});
const levelProgressCurrent = computed(() => membership.value?.lifetimeSpendMinor ?? 0);
const levelProgressPercent = computed(() => {
  if (levelProgressTarget.value <= 0) return 100;
  return Math.min(100, Math.round((levelProgressCurrent.value / levelProgressTarget.value) * 100));
});
const levelProgressText = computed(() => {
  const level = membership.value?.level ?? 0;
  if (level >= levelThresholds.length - 1) return "已达最高等级";
  const remaining = Math.max(0, levelProgressTarget.value - levelProgressCurrent.value);
  return `还差 ${formatAmount(remaining)} 升级 Lv.${level + 1}`;
});
const planetCardExpiryText = computed(() => {
  const expiresAt = membership.value?.planetCardExpiresAt;
  if (!expiresAt) return "暂未开通星球卡";
  return `有效至 ${expiresAt.slice(0, 10)}`;
});
const discountText = computed(() => {
  const level = membership.value?.level ?? 0;
  if (level >= 3) return "8.5 折";
  if (level >= 2) return "9 折";
  if (level >= 1) return "9.5 折";
  return "暂无";
});

onMounted(() => {
  const systemInfo = uni.getWindowInfo();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;
});

onShow(() => {
  loadProfile();
});

async function loadProfile() {
  if (!uni.getStorageSync("xq_access_token")) {
    session.clearSession();
    membership.value = null;
    wallet.value = { balanceMinor: 0, frozenMinor: 0 };
    userAssets.value = [];
    assetEarnings.value = null;
    distribution.value = null;
    pageLoading.value = false;
    return;
  }

  pageLoading.value = true;
  try {
    const [membershipRes, walletRes, assetsRes, earningsRes, distributionRes] = await Promise.allSettled([
      getMembership(),
      getWallet(),
      getAssets(),
      getAssetEarnings(),
      getDistributionMe(),
    ]);

    if (membershipRes.status === "fulfilled") membership.value = membershipRes.value;
    if (walletRes.status === "fulfilled") wallet.value = walletRes.value;
    if (assetsRes.status === "fulfilled") userAssets.value = assetsRes.value || [];
    if (earningsRes.status === "fulfilled") assetEarnings.value = earningsRes.value;
    if (distributionRes.status === "fulfilled") distribution.value = distributionRes.value;
  } finally {
    pageLoading.value = false;
  }
}

function goEditProfile() {
  navigateToPage("/pages/profile/edit", { reason: "profile_edit" });
}

async function wechatLogin() {
  try {
    await session.loginByWechat();
    uni.showToast({ title: "登录成功", icon: "none" });
  } catch (e: any) {
    uni.showToast({ title: e.message || "登录失败", icon: "none" });
  }
}

function handleFunction(path: string) {
  go(path);
}

function go(path: string) {
  navigateToPage(path, { reason: "profile_action" });
}

function copyInvite() {
  const inviteCode = distribution.value?.inviteCode || "";
  if (!inviteCode) {
    uni.showToast({ title: "暂无邀请码", icon: "none" });
    return;
  }
  uni.setClipboardData({ data: String(inviteCode) });
}

function remainingBenefitCount(type: string) {
  const benefit = membership.value?.benefits?.find(item => item.benefitType === type);
  if (!benefit) return 0;
  return Math.max(0, (benefit.totalCount || 0) - (benefit.usedCount || 0));
}
</script>

<style scoped lang="scss">
.profile-page { min-height: 100vh; background-color: #F5F6F8; position: relative; padding-bottom: calc(120rpx + env(safe-area-inset-bottom)); }
.bg-blue-header { position: absolute; top: 0; left: 0; width: 100%; height: 600rpx; background: linear-gradient(180deg, #1D63FF 0%, #3B7FFF 60%, #F5F6F8 100%); z-index: 0; }
.content-container { position: relative; z-index: 1; padding: 0 24rpx; }
.user-section { display: flex; align-items: center; gap: 24rpx; padding: 12rpx 12rpx 36rpx; }
.avatar { width: 120rpx; height: 120rpx; border-radius: 50%; border: 4rpx solid rgba(255, 255, 255, 0.4); }
.user-details { flex: 1; display: flex; flex-direction: column; gap: 10rpx; }
.name-row { display: flex; align-items: center; gap: 16rpx; }
.name, .login-hint { font-size: 36rpx; font-weight: bold; color: #FFFFFF; }
.edit-arrow { width: 56rpx; height: 56rpx; display: flex; align-items: center; justify-content: center; border-radius: 50%; background: rgba(255, 255, 255, 0.15); }
.arrow-icon { font-size: 32rpx; color: #FFFFFF; font-weight: 700; }
.level-badge { display: flex; align-items: center; gap: 6rpx; background: rgba(0, 0, 0, 0.25); padding: 4rpx 16rpx; border-radius: 999rpx; }
.level-badge text, .id-row text { font-size: 22rpx; color: rgba(255, 255, 255, 0.9); }
.id-row { display: flex; align-items: center; gap: 8rpx; }
.copy-icon { width: 24rpx; height: 24rpx; }
.planet-card { background: linear-gradient(135deg, #1C274C 0%, #0F172A 100%); border-radius: 32rpx; padding: 32rpx; margin-bottom: 24rpx; color: #FFFFFF; box-shadow: 0 16rpx 32rpx rgba(15, 23, 42, 0.15); }
.planet-card-top, .planet-card-bottom, .progress-text-row, .card-header { display: flex; justify-content: space-between; align-items: center; }
.planet-card-title, .card-title-wrap { display: flex; align-items: center; gap: 12rpx; }
.planet-icon { width: 40rpx; height: 40rpx; }
.planet-card-title text { font-size: 28rpx; font-weight: 600; }
.planet-card-right text, .planet-card-date text { font-size: 22rpx; color: rgba(255, 255, 255, 0.65); }
.planet-card-date { margin-top: 4rpx; padding-left: 52rpx; }
.planet-card-bottom { margin-top: 40rpx; gap: 32rpx; }
.level-info { display: flex; flex-direction: column; }
.level-text { font-size: 40rpx; font-weight: bold; }
.level-sub, .progress-desc, .progress-num { font-size: 20rpx; color: rgba(255, 255, 255, 0.55); }
.progress-info { flex: 1; display: flex; flex-direction: column; gap: 12rpx; }
.progress-bar-wrap { height: 8rpx; background: rgba(255, 255, 255, 0.15); border-radius: 4rpx; overflow: hidden; }
.progress-bar-inner { height: 100%; background: #1D63FF; border-radius: 4rpx; }
.growth-btn { background: #FFFFFF; padding: 14rpx 28rpx; border-radius: 999rpx; }
.growth-btn text { font-size: 24rpx; color: #1D63FF; font-weight: 600; }
.card-base { background: #FFFFFF; border-radius: 28rpx; padding: 32rpx; margin-bottom: 24rpx; }
.privileges-card { background: #FFFFFF; border-radius: 28rpx; padding: 32rpx 24rpx; margin-bottom: 24rpx; display: flex; justify-content: space-between; }
.privilege-item { display: flex; flex-direction: column; align-items: center; gap: 12rpx; flex: 1; }
.privilege-icon, .function-icon { width: 56rpx; height: 56rpx; }
.privilege-value-wrap, .overview-value-wrap { display: flex; align-items: baseline; gap: 4rpx; }
.privilege-value { font-size: 32rpx; font-weight: bold; color: #0F172A; }
.privilege-unit, .overview-unit { font-size: 20rpx; color: #0F172A; font-weight: bold; }
.privilege-label, .overview-label { font-size: 22rpx; color: #64748B; }
.functions-card { background: #FFFFFF; border-radius: 28rpx; padding: 32rpx; margin-bottom: 24rpx; display: grid; grid-template-columns: repeat(4, 1fr); gap: 40rpx 0; }
.function-item { display: flex; flex-direction: column; align-items: center; gap: 16rpx; }
.function-label { font-size: 24rpx; color: #334155; }
.overview-card, .clones-card { background: #FFFFFF; border-radius: 28rpx; padding: 32rpx; margin-bottom: 24rpx; }
.card-header { margin-bottom: 32rpx; }
.card-title { font-size: 30rpx; font-weight: bold; color: #0F172A; }
.info-icon { width: 28rpx; height: 28rpx; }
.card-more text { font-size: 24rpx; color: #64748B; }
.overview-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16rpx; }
.overview-item { display: flex; flex-direction: column; align-items: center; gap: 12rpx; }
.overview-value { font-size: 30rpx; font-weight: bold; color: #1D63FF; }
.clones-grid { display: flex; gap: 16rpx; }
.clone-item { flex: 1; border-radius: 16rpx; padding: 20rpx; display: flex; flex-direction: column; gap: 8rpx; position: relative; overflow: hidden; height: 180rpx; }
.clone-info { position: relative; z-index: 1; display: flex; flex-direction: column; gap: 8rpx; }
.clone-label { font-size: 24rpx; font-weight: bold; }
.clone-count { font-size: 20rpx; }
.clone-image { position: absolute; right: -20rpx; bottom: -20rpx; width: 140rpx; height: 140rpx; z-index: 0; }
.clone-item.idle { background: #ECFDF5; }
.clone-item.idle .clone-label { color: #059669; }
.clone-item.idle .clone-count { color: #34D399; }
.clone-item.renting { background: #EFF6FF; }
.clone-item.renting .clone-label { color: #2563EB; }
.clone-item.renting .clone-count { color: #93C5FD; }
.clone-item.maintenance { background: #FFF7ED; }
.clone-item.maintenance .clone-label { color: #EA580C; }
.clone-item.maintenance .clone-count { color: #FDBA74; }
</style>