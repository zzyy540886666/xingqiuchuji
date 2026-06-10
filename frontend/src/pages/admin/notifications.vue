<template>
  <view class="page">
    <template v-if="loading">
      <Skeleton variant="admin-notifications" />
    </template>
    <EmptyState v-else-if="list.length === 0" text="暂无消息" />
    <view v-else class="notification-list">
      <view v-for="item in list" :key="item.id" class="notification-item" :class="{ unread: !item.read }" @tap="markRead(item.id)">
        <view class="notification-dot" v-if="!item.read"></view>
        <view class="notification-content">
          <text class="notification-title">{{ item.title }}</text>
          <text class="notification-body">{{ item.body }}</text>
          <text class="notification-time">{{ item.createdAt }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import Skeleton from "../../components/PageSkeleton.vue";
import EmptyState from "../../components/EmptyState.vue";
import { getNotifications, markNotificationsRead } from "../../services/repair";

const list = ref<{ id: string; title: string; body: string; read: boolean; createdAt: string }[]>([]);
const loading = ref(true);

onShow(async () => {
  loading.value = true;
  try { const res = await getNotifications({}); list.value = res.items; } catch {} finally { loading.value = false; }
});

async function markRead(id: string) {
  const item = list.value.find((n) => n.id === id);
  if (item && !item.read) {
    try { await markNotificationsRead([id]); item.read = true; } catch {}
  }
}
</script>

<style scoped lang="scss">
.notification-list { padding: 28rpx; }
.notification-item { display: flex; gap: 16rpx; background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; }
.notification-item.unread { background: #f0f4ff; }
.notification-dot { width: 16rpx; height: 16rpx; border-radius: 50%; background: #0a4bfe; margin-top: 8rpx; flex-shrink: 0; }
.notification-content { flex: 1; }
.notification-title { display: block; font-size: 28rpx; font-weight: 600; color: #111827; margin-bottom: 8rpx; }
.notification-body { display: block; font-size: 24rpx; color: #6b7280; margin-bottom: 8rpx; }
.notification-time { font-size: 22rpx; color: #9ca3af; }
</style>

