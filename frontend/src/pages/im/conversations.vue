<template>
  <view class="page">
    <Skeleton v-if="loading" :rows="4" />
    <ErrorRetry v-else-if="error" @retry="loadMessages" />
    <EmptyState v-else-if="conversations.length === 0" text="暂无消息" />
    <view v-else class="conversation-list">
      <view v-for="conv in conversations" :key="conv.id" class="conv-item" @tap="openNotification(conv)">
        <view class="conv-avatar"></view>
        <view class="conv-info">
          <view class="conv-header">
            <text class="conv-name">{{ conv.title }}</text>
            <text class="conv-time">{{ conv.createdAt?.slice(0, 16) }}</text>
          </view>
          <text class="conv-last">{{ conv.content }}</text>
        </view>
        <view v-if="!conv.isRead" class="conv-badge"></view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import Skeleton from "../../components/Skeleton.vue";
import EmptyState from "../../components/EmptyState.vue";
import ErrorRetry from "../../components/ErrorRetry.vue";
import { getNotifications, markNotificationsRead, type Notification } from "../../services/im";

const conversations = ref<Notification[]>([]);
const loading = ref(true);
const error = ref(false);

async function loadMessages() {
  loading.value = true;
  error.value = false;
  try {
    const result = await getNotifications({ page: 1 });
    conversations.value = result.items;
  } catch {
    error.value = true;
  } finally { loading.value = false; }
}
onShow(() => loadMessages());

async function openNotification(item: Notification) {
  if (!item.isRead) {
    await markNotificationsRead([item.id]);
    item.isRead = true;
  }
  uni.showModal({ title: item.title, content: item.content, showCancel: false });
}
</script>

<style scoped lang="scss">
.conversation-list { padding: 0 28rpx; }
.conv-item { display: flex; align-items: center; gap: 20rpx; padding: 24rpx 0; border-bottom: 1rpx solid #f3f4f6; }
.conv-avatar { width: 88rpx; height: 88rpx; border-radius: 50%; background: #e5e7eb; flex-shrink: 0; }
.conv-info { flex: 1; min-width: 0; }
.conv-header { display: flex; justify-content: space-between; margin-bottom: 8rpx; }
.conv-name { font-size: 28rpx; font-weight: 600; color: #111827; }
.conv-time { font-size: 22rpx; color: #9ca3af; }
.conv-last { font-size: 24rpx; color: #6b7280; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.conv-badge { min-width: 36rpx; height: 36rpx; border-radius: 999rpx; background: #ef4444; color: #fff; font-size: 20rpx; display: flex; align-items: center; justify-content: center; padding: 0 8rpx; }
</style>
