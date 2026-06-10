<template>
  <view class="page safe-bottom">
    <!-- Nav Bar -->
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-title">消息</view>
    </view>

    <!-- Tabs -->
    <view class="tabs">
      <view v-for="tab in typeTabs" :key="tab.value" class="tab-item" :class="{ active: currentType === tab.value }" @tap="currentType = tab.value">
        <text class="tab-label">{{ tab.label }}</text>
        <view v-if="currentType === tab.value" class="tab-indicator"></view>
      </view>
    </view>

    <!-- Content -->
    <view class="content-area">
      <template v-if="loading">
        <Skeleton variant="notifications" />
      </template>
      <ErrorRetry v-else-if="error" @retry="loadMessages" />
      <EmptyState v-else-if="filteredMessages.length === 0" text="暂无消息" />
      <view v-else class="message-list-container">
        <view class="message-list">
          <view v-for="msg in filteredMessages" :key="msg.id" class="message-item" @tap="openMessage(msg)">
            <view v-if="!msg.isRead" class="unread-left-dot"></view>
            <view class="msg-icon-wrap" :style="{ backgroundColor: getIconStyle(msg).bg }">
              <image class="msg-icon-img" :src="getIconStyle(msg).icon" mode="aspectFit" />
            </view>
            <view class="msg-body">
              <view class="msg-header">
                <text class="msg-title">{{ msg.title }}</text>
                <text class="msg-time">{{ formatMsgTime(msg.createdAt) }}</text>
              </view>
              <view class="msg-content-row">
                <text class="msg-content">{{ msg.content }}</text>
                <view v-if="!msg.isRead" class="unread-badge">
                  <!-- Some have count, some are just dots. We check msg property, otherwise use fallback logic -->
                  <text v-if="(msg as any).unreadCount">{{ (msg as any).unreadCount }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
        <view v-if="hasMore && !loading" class="load-more" @tap="loadNextPage">加载更多</view>
      </view>
    </view>

    <!-- Bottom Fixed Bar -->
    <view class="bottom-bar">
      <view class="bottom-action" @tap="markAllRead">
        <image class="bottom-icon" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%231D63FF' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Ccircle cx='12' cy='12' r='10'/%3E%3Cpath d='M9 12l2 2 4-4'/%3E%3C/svg%3E" mode="aspectFit" />
        <text>全部已读</text>
      </view>
      <view class="bottom-divider"></view>
      <view class="bottom-action" @tap="goSettings">
        <image class="bottom-icon" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%231D63FF' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Ccircle cx='12' cy='12' r='3'/%3E%3Cpath d='M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z'/%3E%3C/svg%3E" mode="aspectFit" />
        <text>消息设置</text>
      </view>
    </view>

    <!-- 消息详情弹窗 -->
    <view v-if="selectedMessage" class="modal-mask" @tap="selectedMessage = null">
      <view class="modal-card" @tap.stop>
        <text class="modal-title">{{ selectedMessage.title }}</text>
        <text class="modal-time">{{ formatMsgTime(selectedMessage.createdAt) }}</text>
        <view class="modal-divider"></view>
        <text class="modal-content">{{ selectedMessage.content }}</text>
        <view class="modal-actions">
          <view v-if="selectedMessage.refId" class="modal-btn primary" @tap="goRefPage">查看详情</view>
          <view class="modal-btn outline" @tap="selectedMessage = null">关闭</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { onShow } from "@dcloudio/uni-app";
import Skeleton from "../../components/PageSkeleton.vue";
import EmptyState from "../../components/EmptyState.vue";
import ErrorRetry from "../../components/ErrorRetry.vue";
import { getNotifications, markNotificationsRead } from "../../services/notification";
import type { Notification } from "../../types/notification";
import { createLatestTask } from "../../utils/latestTask";

const statusBarHeight = ref(0);
{
  const systemInfo = uni.getWindowInfo();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;
}

const typeTabs = [
  { label: "全部", value: "" },
  { label: "系统通知", value: "SYSTEM" },
  { label: "订单通知", value: "ORDER" },
  { label: "收益通知", value: "REVENUE" },
  { label: "社区消息", value: "SOCIAL" },
];

const SVGS = {
  wallet: "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23FFFFFF' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M21 12V7H5a2 2 0 0 1 0-4h14v4'/%3E%3Cpath d='M3 5v14a2 2 0 0 0 2 2h16v-5'/%3E%3Cpath d='M18 12a2 2 0 0 0 0 4h4v-4Z'/%3E%3C/svg%3E",
  repair: "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23FFFFFF' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2'/%3E%3Crect x='8' y='2' width='8' height='4' rx='1' ry='1'/%3E%3Cpath d='M9 14l2 2 4-4'/%3E%3C/svg%3E",
  shield: "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23FFFFFF' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z'/%3E%3Cpath d='M9 12l2 2 4-4'/%3E%3C/svg%3E",
  income: "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23FFFFFF' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Ccircle cx='12' cy='12' r='10'/%3E%3Cpath d='M8 12h8'/%3E%3Cpath d='M12 8l-4 8'/%3E%3Cpath d='M12 8l4 8'/%3E%3C/svg%3E",
  message: "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23FFFFFF' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z'/%3E%3Cpath d='M8 10h.01M12 10h.01M16 10h.01'/%3E%3C/svg%3E",
  bell: "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23FFFFFF' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9'/%3E%3Cpath d='M13.73 21a2 2 0 0 1-3.46 0'/%3E%3C/svg%3E",
  order: "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23FFFFFF' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z'/%3E%3Cpath d='M14 2v6h6M16 13H8M16 17H8M10 9H8'/%3E%3C/svg%3E",
  ticket: "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23FFFFFF' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Crect x='2' y='4' width='20' height='16' rx='2' ry='2'/%3E%3Cpath d='M2 12h.01'/%3E%3Cpath d='M22 12h.01'/%3E%3Cpath d='M7 8h10M7 12h10M7 16h10'/%3E%3C/svg%3E",
  service: "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23FFFFFF' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M3 18v-6a9 9 0 0 1 18 0v6'/%3E%3Cpath d='M21 19a2 2 0 0 1-2 2h-1a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2h3zM3 19a2 2 0 0 0 2 2h1a2 2 0 0 0 2-2v-3a2 2 0 0 0-2-2H3z'/%3E%3C/svg%3E"
};

function getIconStyle(msg: Notification) {
  const t = msg.title || "";
  const type = msg.type || "";
  if (t.includes("提现") || t.includes("钱包")) return { bg: "#1D63FF", icon: SVGS.wallet };
  if (t.includes("报修") || t.includes("工单")) return { bg: "#10B981", icon: SVGS.repair };
  if (t.includes("审核")) return { bg: "#F59E0B", icon: SVGS.shield };
  if (t.includes("收益") || t.includes("结算")) return { bg: "#3B82F6", icon: SVGS.income };
  if (type === "SOCIAL" || t.includes("互动") || t.includes("评论") || t.includes("社区")) return { bg: "#8B5CF6", icon: SVGS.message };
  if (t.includes("维护") || t.includes("系统")) return { bg: "#60A5FA", icon: SVGS.bell };
  if (type === "ORDER" || t.includes("订单")) return { bg: "#FBBF24", icon: SVGS.order };
  if (t.includes("活动")) return { bg: "#34D399", icon: SVGS.ticket };
  if (t.includes("客服")) return { bg: "#3B82F6", icon: SVGS.service };
  
  if (type === "SYSTEM") return { bg: "#60A5FA", icon: SVGS.bell };
  if (type === "ORDER") return { bg: "#FBBF24", icon: SVGS.order };
  if (type === "REVENUE") return { bg: "#3B82F6", icon: SVGS.income };
  if (type === "SOCIAL") return { bg: "#8B5CF6", icon: SVGS.message };
  return { bg: "#9CA3AF", icon: SVGS.bell };
}

function formatMsgTime(createdAt?: string) {
  if (!createdAt) return "";
  const dateStr = createdAt.slice(0, 16);
  const timePart = dateStr.slice(11, 16);
  const datePart = dateStr.slice(5, 10);
  
  const msgDate = new Date(createdAt.replace(/-/g, '/'));
  const today = new Date();
  
  const isToday = msgDate.getDate() === today.getDate() && msgDate.getMonth() === today.getMonth() && msgDate.getFullYear() === today.getFullYear();
  
  const yesterday = new Date(today);
  yesterday.setDate(today.getDate() - 1);
  const isYesterday = msgDate.getDate() === yesterday.getDate() && msgDate.getMonth() === yesterday.getMonth() && msgDate.getFullYear() === yesterday.getFullYear();

  if (isToday) return timePart;
  if (isYesterday) return `昨天 ${timePart}`;
  return `${datePart} ${timePart}`;
}

const currentType = ref("");
const messages = ref<Notification[]>([]);
const loading = ref(true);
const error = ref(false);
const page = ref(1);
const hasMore = ref(false);
const selectedMessage = ref<Notification | null>(null);
const messageTask = createLatestTask();

const filteredMessages = computed(() => {
  if (!currentType.value) return messages.value;
  return messages.value.filter((m) => m.type === currentType.value);
});

async function loadMessages(reset = false) {
  if (!reset && loading.value) return;
  if (!reset && messages.value.length > 0 && !hasMore.value) return;
  const targetPage = reset || messages.value.length === 0 ? 1 : page.value + 1;
  const requestId = messageTask.begin();
  loading.value = true;
  error.value = false;
  try {
    const res = await getNotifications({ page: targetPage });
    if (!messageTask.isCurrent(requestId)) return;
    messages.value = reset ? res.items : [...messages.value, ...res.items];
    page.value = targetPage;
    hasMore.value = messages.value.length < (res.total || 0);
  } catch {
    if (messageTask.isCurrent(requestId) && messages.value.length === 0) error.value = true;
  } finally {
    if (messageTask.isCurrent(requestId)) loading.value = false;
  }
}

function loadNextPage() {
  loadMessages();
}

function markAllRead() {
  const unreadIds = messages.value.filter(m => !m.isRead).map(m => m.id);
  if (unreadIds.length === 0) {
    uni.showToast({ title: "没有未读消息", icon: "none" });
    return;
  }
  markNotificationsRead(unreadIds).then(() => {
    messages.value.forEach(m => { m.isRead = true; });
    uni.showToast({ title: "已全部标记为已读", icon: "none" });
  }).catch(() => {
    uni.showToast({ title: "操作失败", icon: "none" });
  });
}

function goSettings() {
  uni.showToast({ title: "消息设置未接入", icon: "none" });
}

async function openMessage(msg: Notification) {
  selectedMessage.value = msg;
  if (!msg.isRead) {
    try {
      await markNotificationsRead([msg.id]);
      msg.isRead = true;
    } catch { /* silently continue */ }
  }
}

function goRefPage() {
  const msg = selectedMessage.value;
  if (!msg?.refId) return;
  selectedMessage.value = null;
  const routeMap: Record<string, string> = {
    ORDER: `/pages/order/detail?id=${msg.refId}`,
    SYSTEM: `/pages/notifications/index`,
    SOCIAL: `/pages/im/chat?id=${msg.refId}`,
  };
  const url = routeMap[msg.type] || `/pages/notifications/index`;
  uni.navigateTo({ url });
}

watch(currentType, () => loadMessages(true));
onShow(() => loadMessages(true));
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: #F5F6F8;
  padding-bottom: 120rpx; /* Space for bottom bar */
  box-sizing: border-box;
}

.nav-bar {
  background: #FFFFFF;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.nav-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #0F172A;
}

.tabs {
  display: flex;
  background: #FFFFFF;
  padding: 0 32rpx;
  justify-content: space-between;
}
.tab-item {
  position: relative;
  padding: 24rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.tab-label {
  font-size: 28rpx;
  color: #64748B;
}
.tab-item.active .tab-label {
  color: #1D63FF;
  font-weight: bold;
}
.tab-indicator {
  position: absolute;
  bottom: 0;
  width: 40rpx;
  height: 6rpx;
  background: #1D63FF;
  border-radius: 4rpx;
}

.message-list-container {
  margin: 24rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.03);
}

.message-item {
  display: flex;
  padding: 32rpx 24rpx;
  border-bottom: 1rpx solid #F3F4F6;
  position: relative;
}
.message-item:last-child {
  border-bottom: none;
}

.unread-left-dot {
  position: absolute;
  left: 8rpx;
  top: 72rpx;
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: #1D63FF;
}

.msg-icon-wrap {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 16rpx;
  margin-right: 24rpx;
  flex-shrink: 0;
}
.msg-icon-img {
  width: 40rpx;
  height: 40rpx;
}

.msg-body {
  flex: 1;
  min-width: 0;
}
.msg-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}
.msg-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #0F172A;
}
.msg-time {
  font-size: 22rpx;
  color: #94A3B8;
}
.msg-content-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16rpx;
}
.msg-content {
  font-size: 26rpx;
  color: #64748B;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.unread-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #EF4444;
  color: #FFFFFF;
  font-size: 20rpx;
  min-width: 32rpx;
  height: 32rpx;
  border-radius: 16rpx;
  padding: 0 8rpx;
  flex-shrink: 0;
  margin-top: 4rpx;
}
.unread-badge:empty {
  min-width: 16rpx;
  width: 16rpx;
  height: 16rpx;
  padding: 0;
  border-radius: 50%;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background: #FFFFFF;
  display: flex;
  align-items: center;
  padding-bottom: env(safe-area-inset-bottom);
  border-top: 1rpx solid #F3F4F6;
  z-index: 100;
}
.bottom-action {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}
.bottom-action text {
  font-size: 28rpx;
  color: #1D63FF;
  font-weight: 500;
}
.bottom-icon {
  width: 36rpx;
  height: 36rpx;
}
.bottom-divider {
  width: 2rpx;
  height: 32rpx;
  background: #E2E8F0;
}

.load-more {
  text-align: center;
  padding: 24rpx;
  font-size: 24rpx;
  color: #64748B;
}

/* Modal styles (retained) */
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
  max-height: 80vh;
  overflow-y: auto;
}
.modal-title { font-size: 32rpx; font-weight: 700; display: block; margin-bottom: 8rpx; }
.modal-time { font-size: 22rpx; color: #9ca3af; display: block; }
.modal-divider { height: 1rpx; background: #f3f4f6; margin: 20rpx 0; }
.modal-content { font-size: 28rpx; color: #374151; line-height: 1.7; display: block; }
.modal-actions { display: flex; gap: 16rpx; margin-top: 28rpx; }
.modal-btn { flex: 1; height: 72rpx; border-radius: 999rpx; display: flex; align-items: center; justify-content: center; font-size: 26rpx; }
.modal-btn.primary { background: #0a4bfe; color: #fff; }
.modal-btn.outline { border: 2rpx solid #d1d5db; color: #6b7280; }
</style>
