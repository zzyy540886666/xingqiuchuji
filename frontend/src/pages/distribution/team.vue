<template>
  <view class="page">
    <view class="summary">
      <view class="stat"><text class="stat-value">{{ team.level1Count }}</text><text class="stat-label">一级成员</text></view>
      <view class="stat"><text class="stat-value">{{ team.level2Count }}</text><text class="stat-label">二级成员</text></view>
    </view>
    <Skeleton v-if="loading" :rows="4" />
    <EmptyState v-else-if="team.members.length === 0" text="暂无团队成员" />
    <view v-else class="member-list">
      <view v-for="(m, idx) in team.members" :key="idx" class="member-item">
        <view class="member-avatar"></view>
        <text class="member-name">{{ m.nickname }}</text>
        <text class="member-time">L{{ m.level }} · {{ m.joinedAt?.slice(0, 10) }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import Skeleton from "../../components/Skeleton.vue";
import EmptyState from "../../components/EmptyState.vue";
import { getTeam } from "../../services/distribution";

const team = ref<{ level1Count: number; level2Count: number; members: { userId: string; nickname: string; avatarUrl: string; level: number; joinedAt: string }[] }>({ level1Count: 0, level2Count: 0, members: [] });
const loading = ref(true);

onShow(async () => {
  loading.value = true;
  try { team.value = await getTeam(); } catch {} finally { loading.value = false; }
});
</script>

<style scoped lang="scss">
.summary { display: flex; justify-content: space-around; background: #fff; margin: 28rpx; border-radius: 20rpx; padding: 36rpx; }
.stat { text-align: center; }
.stat-value { display: block; font-size: 36rpx; font-weight: 800; color: #0a4bfe; }
.stat-label { display: block; font-size: 22rpx; color: #6b7280; margin-top: 6rpx; }
.member-list { background: #fff; margin: 0 28rpx; border-radius: 20rpx; padding: 20rpx 28rpx; }
.member-item { display: flex; align-items: center; gap: 16rpx; padding: 16rpx 0; border-bottom: 1rpx solid #f9fafb; }
.member-avatar { width: 64rpx; height: 64rpx; border-radius: 50%; background: #e5e7eb; }
.member-name { flex: 1; font-size: 26rpx; color: #111827; }
.member-time { font-size: 22rpx; color: #9ca3af; }
</style>
