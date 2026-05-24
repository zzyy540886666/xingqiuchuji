<template>
  <view class="page chat-page">
    <scroll-view scroll-y class="message-list">
      <view v-if="message" class="message other">
        <view class="bubble">
          <text class="bubble-text">{{ message.content }}</text>
        </view>
        <text class="msg-time">{{ message.createdAt }}</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { getNotifications, markNotificationsRead, type Notification } from "../../services/im";

const message = ref<Notification | null>(null);

onLoad(async (options) => {
  const result = await getNotifications({ page: 1 });
  message.value = result.items.find((item) => item.id === options?.id) || null;
  if (message.value && !message.value.isRead) await markNotificationsRead([message.value.id]);
});
</script>

<style scoped lang="scss">
.chat-page { display: flex; flex-direction: column; height: 100vh; }
.message-list { flex: 1; padding: 28rpx; }
.message { margin-bottom: 24rpx; display: flex; flex-direction: column; }
.message.mine { align-items: flex-end; }
.message.other { align-items: flex-start; }
.bubble { max-width: 70%; padding: 20rpx 28rpx; border-radius: 20rpx; }
.mine .bubble { background: #0a4bfe; color: #fff; border-bottom-right-radius: 6rpx; }
.other .bubble { background: #f3f4f6; color: #111827; border-bottom-left-radius: 6rpx; }
.bubble-text { font-size: 28rpx; line-height: 1.5; }
.msg-time { font-size: 20rpx; color: #9ca3af; margin-top: 6rpx; }
.input-bar { display: flex; align-items: center; gap: 16rpx; padding: 16rpx 28rpx calc(16rpx + env(safe-area-inset-bottom)); background: #fff; border-top: 1rpx solid #f3f4f6; }
.msg-input { flex: 1; height: 72rpx; padding: 0 24rpx; background: #f3f4f6; border-radius: 999rpx; font-size: 28rpx; }
.send-btn { padding: 0 28rpx; height: 72rpx; border-radius: 999rpx; background: #0a4bfe; color: #fff; font-size: 26rpx; display: flex; align-items: center; justify-content: center; }
.send-btn.disabled { opacity: 0.5; }
</style>
