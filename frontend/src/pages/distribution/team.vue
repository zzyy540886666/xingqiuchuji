<template>
  <view class="page">
    <view class="nav-bar">
      <view class="back-btn" @tap="goBack"><image class="back-icon" src="/static/icons/back-light.svg" mode="aspectFit" /></view>
      <text class="nav-title">我的团队</text>
      <view class="nav-placeholder"></view>
    </view>
    <template v-if="loading">
      <Skeleton variant="distribution-team" />
    </template>
    <ErrorRetry v-else-if="error" @retry="loadTeam" />
    <template v-else>
    <view class="summary">
      <view class="stat"><text class="stat-value">{{ team.level1Count }}</text><text class="stat-label">一级成员</text></view>
      <view class="stat"><text class="stat-value">{{ team.level2Count }}</text><text class="stat-label">二级成员</text></view>
    </view>
    <EmptyState v-if="team.members.length === 0" text="暂无团队成员" />
    <view v-else class="member-list">
      <view v-for="(m, idx) in team.members" :key="idx" class="member-item">
        <view class="member-avatar">
          <image v-if="m.avatarUrl" :src="m.avatarUrl" mode="aspectFill" class="avatar-img" />
        </view>
        <text class="member-name">{{ m.nickname }}</text>
        <text class="member-time">L{{ m.level }} 路 {{ m.joinedAt?.slice(0, 10) }}</text>
      </view>
    </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import Skeleton from "../../components/PageSkeleton.vue";
import EmptyState from "../../components/EmptyState.vue";
import ErrorRetry from "../../components/ErrorRetry.vue";
import { getTeam } from "../../services/distribution";

const team = ref<{ level1Count: number; level2Count: number; members: { userId: string; nickname: string; avatarUrl: string; level: number; joinedAt: string }[] }>({ level1Count: 0, level2Count: 0, members: [] });
const loading = ref(true);
const error = ref(false);

onShow(() => loadTeam());

async function loadTeam() {
  loading.value = true;
  error.value = false;
  try {
    team.value = await getTeam();
  } catch {
    team.value = { level1Count: 0, level2Count: 0, members: [] };
    error.value = true;
  } finally {
    loading.value = false;
  }
}

function goBack() { uni.navigateBack({ delta: 1 }); }
</script>

<style scoped lang="scss">
.nav-bar {
  height: 88rpx; padding: 0 28rpx;
  display: flex; align-items: center; justify-content: space-between;
  background: #fff; border-bottom: 1rpx solid #f3f4f6;
}
.back-btn { width: 56rpx; height: 56rpx; border-radius: 50%; background: #f3f4f6; display: flex; align-items: center; justify-content: center; }
.back-icon { width: 28rpx; height: 28rpx; display: block; }
.nav-title { font-size: 32rpx; font-weight: 700; color: #111827; }
.nav-placeholder { width: 56rpx; }
.summary { display: flex; justify-content: space-around; background: #fff; margin: 28rpx; border-radius: 20rpx; padding: 36rpx; }
.stat { text-align: center; }
.stat-value { display: block; font-size: 36rpx; font-weight: 800; color: #0a4bfe; }
.stat-label { display: block; font-size: 22rpx; color: #6b7280; margin-top: 6rpx; }
.member-list { background: #fff; margin: 0 28rpx; border-radius: 20rpx; padding: 20rpx 28rpx; }
.member-item { display: flex; align-items: center; gap: 16rpx; padding: 16rpx 0; border-bottom: 1rpx solid #f9fafb; }
.member-avatar { width: 64rpx; height: 64rpx; border-radius: 50%; background: #e5e7eb; overflow: hidden; }
.avatar-img { width: 100%; height: 100%; }
.member-name { flex: 1; font-size: 26rpx; color: #111827; }
.member-time { font-size: 22rpx; color: #9ca3af; }
</style>

