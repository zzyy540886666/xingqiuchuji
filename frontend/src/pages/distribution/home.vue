<template>
  <view class="page">
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="back-btn" @tap="goBack">
        <image class="back-icon" src="/static/icons/back.svg" mode="aspectFit" />
      </view>
      <text class="nav-title">分销中心</text>
      <view class="nav-placeholder"></view>
    </view>

    <template v-if="loading">
      <Skeleton variant="distribution" />
    </template>
    <scroll-view
      v-else
      scroll-y
      class="scroll-container"
      :style="{ height: `calc(100vh - ${statusBarHeight + 44}px)` }"
    >
      <view class="hero-section">
        <view class="hero-bg">
          <view class="planet-circle"></view>
          <view class="planet-ring"></view>
        </view>
        <view class="hero-content">
          <view class="hero-header">
            <text class="hero-title">推广收益概览</text>
            <image class="eye-icon" src="/static/icons/eye.svg" mode="aspectFit" />
            <view class="withdraw-action">
              <button class="btn-withdraw" @tap="go('/pages/wallet/withdraw')">提现</button>
              <view class="withdraw-record" @tap="go('/pages/wallet/index')">
                <text>提现记录</text>
                <image class="arrow-right" src="/static/icons/chevron-right.svg" mode="aspectFit" />
              </view>
            </view>
          </view>

          <view class="hero-stats">
            <view class="stat-col">
              <text class="stat-label">待结算佣金(元)</text>
              <text class="stat-value">{{ amountText(info.overview.pendingCommission) }}</text>
              <text class="stat-sub">≈{{ Math.floor(info.overview.pendingCommission / 100) }} 光年币</text>
            </view>
            <view class="stat-col">
              <text class="stat-label">可提现佣金(元)</text>
              <text class="stat-value">{{ amountText(info.overview.withdrawableCommission) }}</text>
              <text class="stat-sub">≈{{ Math.floor(info.overview.withdrawableCommission / 100) }} 光年币</text>
            </view>
            <view class="stat-col">
              <text class="stat-label">已提现金额(元)</text>
              <text class="stat-value">{{ amountText(info.overview.withdrawnCommission) }}</text>
              <text class="stat-sub">累计提现</text>
            </view>
          </view>

          <view class="hero-footer">
            <image class="speaker-icon" src="/static/icons/bell.svg" mode="aspectFit" />
            <text class="footer-text">佣金在订单完成后进入结算，保护期结束后可提现。</text>
            <view class="rule-link" @tap="go('/pages/distribution/commission')">
              <text>查看明细</text>
              <image class="arrow-right" src="/static/icons/chevron-right.svg" mode="aspectFit" />
            </view>
          </view>
        </view>
      </view>

      <view class="section tools-section">
        <view class="section-header">
          <text class="section-title">推广工具</text>
          <view class="section-more" @tap="go('/pages/distribution/invite')">
            <text>邀请好友</text>
            <image class="arrow-right" src="/static/icons/chevron-right.svg" mode="aspectFit" />
          </view>
        </view>
        <view class="tools-grid">
          <view class="tool-card">
            <view class="tool-icon-wrapper">
              <image class="tool-icon" src="/static/icons/share.svg" mode="aspectFit" />
            </view>
            <view class="tool-info">
              <text class="tool-name">生成邀请海报</text>
              <text class="tool-desc">用于分享传播</text>
            </view>
            <button class="tool-btn" @tap="go('/pages/distribution/poster')">去生成</button>
          </view>
          <view class="tool-card">
            <view class="tool-icon-wrapper">
              <image class="tool-icon" src="/static/icons/share.svg" mode="aspectFit" />
            </view>
            <view class="tool-info">
              <text class="tool-name">复制邀请链接</text>
              <text class="tool-desc text-ellipsis">{{ info.inviteUrl || "暂无邀请链接" }}</text>
            </view>
            <button class="tool-btn" @tap="copy(info.inviteUrl)">复制链接</button>
          </view>
          <view class="tool-card">
            <view class="tool-icon-wrapper">
              <image class="tool-icon" src="/static/icons/grid.svg" mode="aspectFit" />
            </view>
            <view class="tool-info">
              <text class="tool-name">小程序邀请路径</text>
              <text class="tool-desc text-ellipsis">{{ info.invitePath || "暂无邀请路径" }}</text>
            </view>
            <button class="tool-btn" @tap="copy(info.invitePath)">复制路径</button>
          </view>
        </view>
      </view>

      <view class="section team-section">
        <view class="section-header">
          <view class="title-with-icon">
            <text class="section-title">团队看板</text>
            <image class="help-icon" src="/static/icons/help-dark.svg" mode="aspectFit" />
          </view>
          <view class="section-more" @tap="go('/pages/distribution/team')">
            <text>团队管理</text>
            <image class="arrow-right" src="/static/icons/chevron-right.svg" mode="aspectFit" />
          </view>
        </view>
        <view class="team-content">
          <view class="team-summary-card">
            <view class="team-summary-node">
              <image class="tree-avatar" src="/static/images/default-avatar.svg" mode="aspectFill" />
              <text class="tree-name">我</text>
            </view>
            <view class="team-summary-divider"></view>
            <view class="team-summary-children">
              <view class="team-summary-node child">
                <image class="tree-avatar sm green-border" src="/static/images/default-avatar.svg" mode="aspectFill" />
                <text class="tree-name">一级成员</text>
                <text class="tree-count">{{ info.teamStats.level1Count }}</text>
              </view>
              <view class="team-summary-node child">
                <image class="tree-avatar sm orange-border" src="/static/images/default-avatar.svg" mode="aspectFill" />
                <text class="tree-name">二级成员</text>
                <text class="tree-count">{{ info.teamStats.level2Count }}</text>
              </view>
            </view>
            <text class="team-summary-tip">团队人数根据当前邀请关系实时统计</text>
          </view>

          <view class="team-stats-grid team-stats-grid--full">
            <view class="stat-item">
              <text class="stat-label">一级人数</text>
              <view class="stat-val-row"><text class="stat-value">{{ info.teamStats.level1Count }}</text><text class="stat-unit"> 人</text></view>
            </view>
            <view class="stat-item">
              <text class="stat-label">二级人数</text>
              <view class="stat-val-row"><text class="stat-value">{{ info.teamStats.level2Count }}</text><text class="stat-unit"> 人</text></view>
            </view>
            <view class="stat-item">
              <text class="stat-label">今日新增</text>
              <view class="stat-val-row"><text class="stat-value">{{ info.teamStats.todayNew }}</text><text class="stat-unit"> 人</text></view>
            </view>
            <view class="stat-item">
              <text class="stat-label">累计邀请</text>
              <view class="stat-val-row"><text class="stat-value">{{ info.teamStats.totalInvite }}</text><text class="stat-unit"> 人</text></view>
            </view>
          </view>
        </view>
      </view>

      <view class="section bill-section">
        <view class="section-header">
          <view class="title-with-icon">
            <text class="section-title">佣金账单</text>
            <image class="help-icon" src="/static/icons/help-dark.svg" mode="aspectFit" />
          </view>
          <view class="section-more" @tap="go('/pages/distribution/commission')">
            <text>全部明细</text>
            <image class="arrow-right" src="/static/icons/chevron-right.svg" mode="aspectFit" />
          </view>
        </view>

        <EmptyState v-if="commissions.length === 0" text="暂无佣金记录" />
        <view v-else class="bill-list">
          <view class="bill-item" v-for="item in commissions" :key="item.id">
            <image class="product-img" :src="item.productImage" mode="aspectFill" />
            <view class="bill-info">
              <view class="bill-title-row">
                <text class="bill-title">{{ item.productName }}</text>
                <text class="tag" :class="getSourceTagClass(item.sourceType)">
                  {{ getSourceTagText(item.sourceType) }}
                </text>
              </view>
              <text class="bill-detail">订单号：{{ item.orderNo }}</text>
              <text class="bill-detail">下单时间：{{ item.createdAt }}</text>
            </view>
            <view class="bill-status">
              <text class="bill-amount" :class="getAmountColorClass(item.status)">
                {{ item.amountMinor > 0 ? "+" : "" }}{{ amountText(item.amountMinor) }}
              </text>
              <text class="status-text" :class="getStatusColorClass(item.status)">{{ getStatusText(item.status) }}</text>
              <text class="settle-time">{{ item.status === "DISPUTE" ? "风控状态" : "结算时间" }}：{{ item.settleTime }}</text>
            </view>
          </view>
          <view class="no-more">仅展示最近一页记录</view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import EmptyState from "../../components/EmptyState.vue";
import Skeleton from "../../components/PageSkeleton.vue";
import { getCommissions, getDistributionMe } from "../../services/distribution";
import type { Commission, DistributionInfo } from "../../services/distribution";
import { formatAmount } from "../../utils/format";

const statusBarHeight = ref(0);
const loading = ref(true);
const info = ref<DistributionInfo>(createEmptyDistributionInfo());
const commissions = ref<Commission[]>([]);

onMounted(() => {
  const systemInfo = uni.getWindowInfo();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;
});

onShow(() => {
  void loadDistributionData();
});

async function loadDistributionData() {
  loading.value = true;
  try {
    const [distributionInfo, commissionPage] = await Promise.all([
      getDistributionMe(),
      getCommissions({ status: "ALL", page: 1 }),
    ]);
    info.value = distributionInfo;
    commissions.value = commissionPage.items || [];
  } catch {
    info.value = createEmptyDistributionInfo();
    commissions.value = [];
    uni.showToast({ title: "分销数据加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function createEmptyDistributionInfo(): DistributionInfo {
  return {
    overview: {
      pendingCommission: 0,
      withdrawableCommission: 0,
      withdrawnCommission: 0,
    },
    inviteCode: "",
    inviteUrl: "",
    invitePath: "",
    teamStats: {
      level1Count: 0,
      level2Count: 0,
      todayNew: 0,
      totalInvite: 0,
    },
  };
}

function amountText(amountMinor: number) {
  return formatAmount(amountMinor).replace("¥", "");
}

function go(url: string) {
  uni.navigateTo({ url });
}

function goBack() {
  uni.navigateBack({ delta: 1 });
}

function copy(text: string) {
  if (!text) {
    uni.showToast({ title: "暂无可复制内容", icon: "none" });
    return;
  }
  uni.setClipboardData({
    data: text,
    success: () => {
      uni.showToast({ title: "复制成功", icon: "none" });
    },
  });
}

function getSourceTagText(source: string) {
  switch (source) {
    case "RENT":
      return "租赁";
    case "BUY":
      return "购买";
    case "ACCESSORY":
      return "配件";
    default:
      return source;
  }
}

function getSourceTagClass(source: string) {
  switch (source) {
    case "RENT":
      return "tag-rent";
    case "BUY":
      return "tag-buy";
    case "ACCESSORY":
      return "tag-accessory";
    default:
      return "";
  }
}

function getStatusText(status: string) {
  switch (status) {
    case "PENDING_PROTECT":
      return "保护期中";
    case "SETTLEABLE":
      return "可提现";
    case "SETTLED":
      return "已结算";
    case "DISPUTE":
      return "风控中";
    default:
      return status;
  }
}

function getStatusColorClass(status: string) {
  switch (status) {
    case "PENDING_PROTECT":
      return "text-orange";
    case "SETTLEABLE":
      return "text-blue";
    case "SETTLED":
      return "text-green";
    case "DISPUTE":
      return "text-purple";
    default:
      return "";
  }
}

function getAmountColorClass(status: string) {
  switch (status) {
    case "PENDING_PROTECT":
      return "text-blue";
    case "SETTLEABLE":
      return "text-blue";
    case "SETTLED":
      return "text-green";
    case "DISPUTE":
      return "text-purple";
    default:
      return "";
  }
}
</script>

<style scoped lang="scss">
.page {
  background-color: #f7f8fa;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.nav-bar {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  padding: 0 16px;
  position: relative;
  z-index: 10;
}

.back-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  width: 20px;
  height: 20px;
}

.nav-title {
  font-size: 17px;
  font-weight: 500;
  color: #000;
}

.nav-placeholder {
  width: 28px;
}

.scroll-container {
  flex: 1;
}

.hero-section {
  position: relative;
  margin: 12px 16px;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(135deg, #1b63ff, #0940ff);
  color: #fff;
  padding: 20px;
}

.hero-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.planet-circle {
  position: absolute;
  top: -20px;
  right: 20px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.4), rgba(255, 255, 255, 0.1));
}

.planet-ring {
  position: absolute;
  top: 25px;
  right: -10px;
  width: 160px;
  height: 40px;
  border: 2px solid rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  transform: rotate(-15deg);
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.hero-title {
  font-size: 16px;
  font-weight: 500;
  margin-right: 6px;
}

.eye-icon {
  width: 16px;
  height: 16px;
  opacity: 0.8;
  filter: brightness(0) invert(1);
}

.withdraw-action {
  margin-left: auto;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.btn-withdraw {
  background: linear-gradient(90deg, #ffe082, #ffc107);
  color: #8c6000;
  font-size: 14px;
  font-weight: 600;
  height: 32px;
  line-height: 32px;
  border-radius: 16px;
  padding: 0 20px;
  margin: 0 0 6px 0;
}

.btn-withdraw::after {
  border: none;
}

.withdraw-record {
  display: flex;
  align-items: center;
  font-size: 12px;
  opacity: 0.8;
}

.withdraw-record .arrow-right {
  width: 12px;
  height: 12px;
  margin-left: 2px;
  filter: brightness(0) invert(1);
}

.hero-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 24px;
}

.stat-col {
  flex: 1;
}

.stat-label {
  display: block;
  font-size: 12px;
  opacity: 0.8;
  margin-bottom: 6px;
}

.stat-value {
  display: block;
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 4px;
  font-family: din-alternate, sans-serif;
}

.stat-sub {
  display: block;
  font-size: 11px;
  opacity: 0.6;
}

.hero-footer {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  padding: 8px 12px;
}

.speaker-icon {
  width: 14px;
  height: 14px;
  margin-right: 6px;
  filter: brightness(0) invert(1);
}

.footer-text {
  flex: 1;
  font-size: 11px;
  opacity: 0.9;
}

.rule-link {
  display: flex;
  align-items: center;
  font-size: 11px;
  opacity: 0.9;
  margin-left: 8px;
}

.rule-link .arrow-right {
  width: 12px;
  height: 12px;
  filter: brightness(0) invert(1);
}

.section {
  background: #fff;
  border-radius: 12px;
  margin: 0 16px 12px;
  padding: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.title-with-icon {
  display: flex;
  align-items: center;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.help-icon {
  width: 14px;
  height: 14px;
  margin-left: 6px;
  opacity: 0.5;
}

.section-more {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #999;
}

.section-more .arrow-right {
  width: 14px;
  height: 14px;
}

.tools-grid {
  display: flex;
  gap: 12px;
}

.tool-card {
  flex: 1;
  background: #f7f9ff;
  border-radius: 8px;
  padding: 16px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.tool-icon-wrapper {
  width: 40px;
  height: 40px;
  background: #0052ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.tool-icon {
  width: 24px;
  height: 24px;
  filter: brightness(0) invert(1);
}

.tool-info {
  margin-bottom: 12px;
  min-height: 50px;
}

.tool-name {
  display: block;
  font-size: 12px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.tool-desc {
  display: block;
  font-size: 10px;
  color: #666;
  line-height: 1.4;
}

.text-ellipsis {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
}

.tool-btn {
  background: #fff;
  color: #0052ff;
  border: 1px solid #0052ff;
  border-radius: 14px;
  font-size: 12px;
  height: 28px;
  line-height: 26px;
  padding: 0 16px;
  width: 100%;
}

.tool-btn::after {
  border: none;
}

.team-content {
  display: flex;
  gap: 12px;
}

.team-summary-card {
  flex: 1.2;
  border-radius: 12px;
  background: linear-gradient(180deg, #f7faff 0%, #eef4ff 100%);
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.team-summary-node {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.team-summary-node.child {
  min-width: 72px;
}

.team-summary-divider {
  width: 2px;
  height: 20px;
  background: linear-gradient(180deg, #c7d7ff 0%, #e5ecff 100%);
  margin: 10px 0;
}

.team-summary-children {
  width: 100%;
  display: flex;
  justify-content: space-around;
  gap: 12px;
}

.team-summary-tip {
  margin-top: 12px;
  font-size: 10px;
  color: #7c8aa5;
  text-align: center;
}

.tree-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #eee;
}

.tree-avatar.sm {
  width: 28px;
  height: 28px;
}

.green-border {
  border: 2px solid #10b981;
}

.orange-border {
  border: 2px solid #f59e0b;
}

.tree-name {
  font-size: 10px;
  color: #333;
  margin-top: 4px;
  font-weight: 500;
}

.tree-count {
  font-size: 12px;
  color: #0052ff;
  font-weight: 700;
  margin-top: 2px;
}

.team-stats-grid {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 12px;
}

.team-stats-grid--full {
  align-content: center;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-item .stat-label {
  font-size: 11px;
  color: #666;
  margin-bottom: 4px;
  opacity: 1;
}

.stat-val-row {
  display: flex;
  align-items: baseline;
}

.stat-item .stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  font-family: din-alternate, sans-serif;
}

.stat-item .stat-unit {
  font-size: 10px;
  color: #333;
  margin-left: 2px;
}

.bill-list {
  display: flex;
  flex-direction: column;
}

.bill-item {
  display: flex;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
}

.bill-item:last-child {
  border-bottom: none;
}

.product-img {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  background: #f9f9f9;
  margin-right: 12px;
}

.bill-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.bill-title-row {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
}

.bill-title {
  font-size: 13px;
  font-weight: 500;
  color: #333;
  margin-right: 6px;
}

.tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
}

.tag-rent {
  background: #eef2ff;
  color: #0052ff;
}

.tag-buy {
  background: #ecfdf5;
  color: #10b981;
}

.tag-accessory {
  background: #fffbeb;
  color: #f59e0b;
}

.bill-detail {
  font-size: 11px;
  color: #999;
  margin-bottom: 2px;
}

.bill-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: center;
}

.bill-amount {
  font-size: 15px;
  font-weight: bold;
  margin-bottom: 4px;
  font-family: din-alternate, sans-serif;
}

.status-text {
  font-size: 11px;
  margin-bottom: 4px;
  font-weight: 500;
}

.settle-time {
  font-size: 10px;
  color: #999;
  text-align: right;
}

.text-blue {
  color: #0052ff;
}

.text-green {
  color: #10b981;
}

.text-orange {
  color: #f59e0b;
}

.text-purple {
  color: #8b5cf6;
}

.no-more {
  text-align: center;
  font-size: 12px;
  color: #999;
  padding: 16px 0 4px;
}
</style>
