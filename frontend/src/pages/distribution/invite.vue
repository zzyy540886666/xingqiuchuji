<template>
  <view class="page">
    <view class="card">
      <text class="title">我的邀请码</text>
      <text class="code">{{ inviteCode }}</text>
      <view class="actions">
        <view class="action-btn primary" @tap="copyLink">复制邀请链接</view>
        <view class="action-btn outline" @tap="copyCode">复制邀请码</view>
      </view>
    </view>
    <view class="card">
      <text class="hint">分享给好友，好友通过您的链接注册后自动绑定邀请关系。好友下单后您可获得佣金奖励。</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { getDistributionMe } from "../../services/distribution";

const inviteCode = ref("");
const inviteUrl = ref("");

onLoad(async () => {
  try { const info = await getDistributionMe(); inviteCode.value = info.inviteCode; inviteUrl.value = info.inviteUrl; } catch {}
});

function copyLink() {
  uni.setClipboardData({ data: inviteUrl.value, success: () => uni.showToast({ title: "链接已复制", icon: "none" }) });
}

function copyCode() {
  uni.setClipboardData({ data: inviteCode.value, success: () => uni.showToast({ title: "邀请码已复制", icon: "none" }) });
}
</script>

<style scoped lang="scss">
.card { background: #fff; margin: 28rpx; border-radius: 20rpx; padding: 32rpx; }
.title { display: block; font-size: 28rpx; font-weight: 700; margin-bottom: 20rpx; }
.code { display: block; font-size: 48rpx; font-weight: 900; color: #0a4bfe; text-align: center; padding: 24rpx; background: #f0f4ff; border-radius: 16rpx; letter-spacing: 4rpx; }
.actions { display: flex; gap: 16rpx; margin-top: 28rpx; }
.action-btn { flex: 1; height: 72rpx; border-radius: 999rpx; display: flex; align-items: center; justify-content: center; font-size: 26rpx; }
.action-btn.primary { background: #0a4bfe; color: #fff; }
.action-btn.outline { border: 2rpx solid #0a4bfe; color: #0a4bfe; }
.hint { font-size: 24rpx; color: #6b7280; line-height: 1.6; }
</style>
